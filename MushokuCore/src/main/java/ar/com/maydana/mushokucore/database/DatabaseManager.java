package ar.com.maydana.mushokucore.database;

import ar.com.maydana.mushokucore.config.PluginConfig;
import ar.com.maydana.mushokucore.util.TimeUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Implementación de {@link IDatabase} sobre SQLite embebido — agente A (infra-core),
 * spec §7 L305, §10 (todas las consultas fuera del hilo principal), §19 L860,
 * criterio duro §26 L1039 (cero SQL en hilo principal: el guard <b>lanza</b>).
 *
 * <p><b>Diseño de hilos:</b> un executor dedicado de UN hilo ("MushokuCore-DB") es el
 * único dueño de la {@link Connection}. {@code read}/{@code write}/{@code transaction}
 * se ejecutan en ese hilo; si el llamador está en el hilo principal de Bukkit, el guard
 * lanza {@link IllegalStateException} en el llamador (fácil de ver en desarrollo; no
 * fabrica un cuelgue silencioso). En {@code onDisable} se permite la espera: el hilo
 * principal ya está yendo abajo y hay que garantizar el vaciado (spec §8 L351-355).</p>
 *
 * <p><b>PRAGMAs</b> [PROPUESTA plan §3, V18]: {@code journal_mode=WAL} (lecturas
 * concurrentes con la caché de placeholders sin bloquear al escritor),
 * {@code busy_timeout} desde {@code storage.busy-timeout-ms} y {@code foreign_keys=ON}.</p>
 *
 * <p><b>Backup</b>: {@code PRAGMA wal_checkpoint(TRUNCATE)} + copia del archivo a
 * {@code backups/}. La copia ANTES de migrar (spec §20 L889) la dispara
 * {@link #abrir} cuando hay migraciones pendientes y el archivo ya existía.</p>
 */
public final class DatabaseManager implements IDatabase {

    /** Timeout del vaciado de colas en {@link #flushAll()} (onDisable). */
    private static final long FLUSH_TIMEOUT_SEGUNDOS = 10;
    /** Timeout de espera del backup invocado por comando (admin action puntual). */
    private static final long BACKUP_TIMEOUT_SEGUNDOS = 30;

    private final JavaPlugin plugin;
    private final Path archivo;
    private final int busyTimeoutMs;
    private final BooleanSupplier hiloPrincipal;
    private final ExecutorService executor;
    private final Connection conexion;
    private final AtomicBoolean cerrado = new AtomicBoolean(false);

    private DatabaseManager(JavaPlugin plugin, Path archivo, int busyTimeoutMs,
                            BooleanSupplier hiloPrincipal, Connection conexion) {
        this.plugin = plugin;
        this.archivo = archivo;
        this.busyTimeoutMs = busyTimeoutMs;
        this.hiloPrincipal = hiloPrincipal;
        this.conexion = conexion;
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread hilo = new Thread(runnable, "MushokuCore-DB");
            hilo.setDaemon(true);
            return hilo;
        });
    }

    // ------------------------------------------------------------------
    // Arranque (único momento donde se espera I/O de BD; startup, §8 pasos 3-4)
    // ------------------------------------------------------------------

    /**
     * Abre la base, aplica PRAGMAs, carga y aplica migraciones pendientes. Bloquea como
     * máximo {@code timeoutSegundos}; pensado para {@code onEnable} (el único punto donde
     * esperar es aceptable y lo prescribe §8). Si algo falla, deja la base cerrada y lanza
     * la excepción COMPLETA (no se fabrica un arranque con esquema a medias).
     *
     * <p>Copia la BD a {@code backups/} antes de aplicar migraciones cuando el archivo ya
     * existía (spec §20 L889).</p>
     */
    public static DatabaseManager abrir(JavaPlugin plugin, PluginConfig config,
                                        BooleanSupplier hiloPrincipal, long timeoutSegundos)
            throws Exception {
        if (!"sqlite".equalsIgnoreCase(config.storageType())) {
            throw new IllegalStateException("storage.type no soportado: " + config.storageType()
                    + " (MVP: solo sqlite; fallo seguro §2.5).");
        }
        // getDataFolder() devuelve java.io.File (no Path): hay que convertir con toPath().
        // Este era el error "cannot find symbol" de la línea 106 que rompía a todos los agentes.
        Path carpetaDatos = plugin.getDataFolder().toPath();
        Path archivo = carpetaDatos.resolve(config.storageFile());
        Files.createDirectories(carpetaDatos);
        boolean existia = Files.exists(archivo);

        DatabaseManager manager = new DatabaseManager(
                plugin, archivo, config.busyTimeoutMs(), hiloPrincipal, null);
        CompletableFuture<Void> arranque = CompletableFuture.runAsync(() -> {
            try {
                manager.abrirConexionEjecutarMigraciones(existia);
            } catch (Exception excepcion) {
                throw new java.util.concurrent.CompletionException(excepcion);
            }
        }, manager.executor);

        try {
            arranque.get(timeoutSegundos, TimeUnit.SECONDS);
        } catch (TimeoutException excepcion) {
            manager.executor.shutdownNow();
            throw new IOException("SQLite no respondio en " + timeoutSegundos
                    + " s (base: " + archivo + ")", excepcion);
        } catch (java.util.concurrent.ExecutionException excepcion) {
            manager.executor.shutdownNow();
            try {
                if (manager.conexionReal != null) {
                    manager.conexionReal.close();
                }
            } catch (SQLException ignorada) {
                // el error original manda; el cierre best-effort se loguea abajo.
                plugin.getLogger().log(Level.FINE, "Fallo al cerrar tras arranque fallido", ignorada);
            }
            Throwable causa = excepcion.getCause() == null ? excepcion : excepcion.getCause();
            if (causa instanceof Exception excepcionDeCausa) {
                throw excepcionDeCausa;
            }
            throw new IOException("Arranque de SQLite fallo", causa);
        }
        return manager;
    }

    /** La conexión real se asigna dentro del hilo del executor en el arranque. */
    private Connection conexionReal;

    private void abrirConexionEjecutarMigraciones(boolean existia) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection conexionNueva = DriverManager.getConnection("jdbc:sqlite:" + archivo);
        this.conexionReal = conexionNueva;
        try (Statement sentencia = conexionNueva.createStatement()) {
            sentencia.execute("PRAGMA journal_mode=WAL");
            sentencia.execute("PRAGMA busy_timeout=" + busyTimeoutMs);
            sentencia.execute("PRAGMA foreign_keys=ON");
        }

        List<MigrationRunner.Migracion> migraciones = cargarMigracionesDelJar();
        if (migraciones.isEmpty()) {
            throw new IllegalStateException("No se encontro NINGUNA migracion en el JAR "
                    + "(carpeta migrations/). El paquete esta incompleto.");
        }
        int aplicadasAntes = contarVersionesAplicadas(conexionNueva);
        if (aplicadasAntes > 0 && !migracionesPendientes(conexionNueva, migraciones)) {
            plugin.getLogger().info("SQLite listo: " + archivo.getFileName()
                    + " (esquema al dia, " + migraciones.size() + " migraciones en el JAR).");
            return;
        }
        if (existia && aplicadasAntes > 0) {
            // spec §20 L889: copiar la base ANTES de migrar.
            Path backup = copiaInterna(conexionNueva, "pre-migracion");
            plugin.getLogger().info("Backup pre-migracion creado: " + backup);
        }
        MigrationRunner runner = new MigrationRunner(plugin.getLogger());
        int aplicadas = runner.aplicar(conexionNueva, migraciones);
        plugin.getLogger().info("SQLite migrado: " + aplicadas + " migracion(es) aplicada(s) en "
                + archivo.getFileName() + ".");
    }

    private boolean migracionesPendientes(Connection conexion, List<MigrationRunner.Migracion> migraciones)
            throws SQLException {
        List<Integer> aplicadas = new ArrayList<>();
        try (Statement sentencia = conexion.createStatement();
             java.sql.ResultSet filas = sentencia.executeQuery("SELECT version FROM schema_version")) {
            while (filas.next()) {
                aplicadas.add(filas.getInt(1));
            }
        } catch (SQLException excepcion) {
            String mensaje = excepcion.getMessage() == null ? "" : excepcion.getMessage().toLowerCase();
            if (!mensaje.contains("no such table")) {
                throw excepcion;
            }
        }
        for (MigrationRunner.Migracion migracion : migraciones) {
            if (!aplicadas.contains(migracion.version())) {
                return true;
            }
        }
        return false;
    }

    private static int contarVersionesAplicadas(Connection conexion) throws SQLException {
        try (Statement sentencia = conexion.createStatement();
             java.sql.ResultSet filas = sentencia.executeQuery("SELECT COUNT(*) FROM schema_version")) {
            return filas.next() ? filas.getInt(1) : 0;
        } catch (SQLException excepcion) {
            String mensaje = excepcion.getMessage() == null ? "" : excepcion.getMessage().toLowerCase();
            if (mensaje.contains("no such table")) {
                return 0;
            }
            throw excepcion;
        }
    }

    /**
     * Carga {@code migrations/VNNN__*.sql} del JAR. Primero intenta listar la carpeta
     * (jar: o file:); si el directorio no está indexado, escanea V001..V099 por nombre
     * con {@code getResource} (fallback determinístico, offline).
     */
    private List<MigrationRunner.Migracion> cargarMigracionesDelJar() throws IOException {
        List<String> nombres = new ArrayList<>();
        ClassLoader loader = plugin.getClass().getClassLoader();
        Enumeration<URL> urls = loader.getResources("migrations/");
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if ("jar".equals(url.getProtocol())) {
                JarURLConnection conexion = (JarURLConnection) url.openConnection();
                JarFile jar = conexion.getJarFile();
                Enumeration<JarEntry> entradas = jar.entries();
                while (entradas.hasMoreElements()) {
                    String nombre = entradas.nextElement().getName();
                    if (nombre.matches("migrations/V\\d{3}__.*\\.sql") && !nombres.contains(nombre)) {
                        nombres.add(nombre);
                    }
                }
            } else if ("file".equals(url.getProtocol())) {
                try {
                    Path carpeta = Path.of(url.toURI());
                    try (var flujo = Files.list(carpeta)) {
                        flujo.map(archivoLocal -> "migrations/" + archivoLocal.getFileName().toString())
                                .filter(nombre -> nombre.matches("migrations/V\\d{3}__.*\\.sql"))
                                .filter(nombre -> !nombres.contains(nombre))
                                .forEach(nombres::add);
                    }
                } catch (java.net.URISyntaxException excepcion) {
                    throw new IOException("URL de migrations/ con sintaxis invalida: " + url, excepcion);
                }
            }
        }
        if (nombres.isEmpty()) {
            for (int version = 1; version <= 99; version++) {
                String nombre = String.format("migrations/V%03d__", version);
                // escaneo por prefijo no es posible con getResource; probar el nombre V001 completo
                // se hace abajo solo para la V001 conocida; más versiones requieren el listado.
                if (version == 1 && plugin.getResource(nombre + "initial.sql") != null) {
                    nombres.add(nombre + "initial.sql");
                }
            }
        }
        List<MigrationRunner.Migracion> resultado = new ArrayList<>();
        for (String nombre : nombres) {
            try (InputStream flujo = plugin.getResource(nombre)) {
                if (flujo == null) {
                    throw new IOException("No se pudo leer la migracion del JAR: " + nombre);
                }
                String sql = new String(flujo.readAllBytes(), StandardCharsets.UTF_8);
                resultado.add(new MigrationRunner.Migracion(
                        MigrationRunner.Migracion.versionDeNombre(nombre), nombre, sql));
            }
        }
        resultado.sort(java.util.Comparator.comparingInt(MigrationRunner.Migracion::version));
        return resultado;
    }

    // ------------------------------------------------------------------
    // IDatabase — guard de hilo en read/write/transaction (§26 L1039)
    // ------------------------------------------------------------------

    @Override
    public <T> CompletableFuture<T> read(Function<Connection, T> op) {
        exigirNoHiloPrincipal("read");
        Objects.requireNonNull(op, "op");
        return CompletableFuture.supplyAsync(() -> op.apply(conexionSegura()), executor);
    }

    @Override
    public CompletableFuture<Void> write(Consumer<Connection> op) {
        exigirNoHiloPrincipal("write");
        Objects.requireNonNull(op, "op");
        return CompletableFuture.runAsync(() -> op.accept(conexionSegura()), executor);
    }

    @Override
    public CompletableFuture<Void> transaction(BiConsumer<Connection, Connection> op) {
        exigirNoHiloPrincipal("transaction");
        Objects.requireNonNull(op, "op");
        return CompletableFuture.runAsync(() -> {
            Connection conexion = conexionSegura();
            boolean autocommitAnterior = true;
            try {
                autocommitAnterior = conexion.getAutoCommit();
                conexion.setAutoCommit(false);
                // Firma congelada del plan §6.1: ambas Connection son la misma; la segunda
                // era "noop reserva" en el plan. [A VERIFICAR — plan §6.1] semántica final.
                op.accept(conexion, conexion);
                conexion.commit();
            } catch (RuntimeException | SQLException excepcion) {
                try {
                    conexion.rollback();
                } catch (SQLException falloRollback) {
                    excepcion.addSuppressed(falloRollback);
                }
                // SQLException no puede propagarse desde un Runnable: se envuelve para que
                // el futuro termine en estado fallido con el error COMPLETO (sin perderlo).
                throw new java.util.concurrent.CompletionException(excepcion);
            } finally {
                try {
                    conexion.setAutoCommit(autocommitAnterior);
                } catch (SQLException falloRestore) {
                    plugin.getLogger().log(Level.WARNING,
                            "No se pudo restaurar autoCommit tras transaccion", falloRestore);
                }
            }
        }, executor);
    }

    @Override
    public Path backup(String motivo) throws IOException {
        // Firma congelada síncrona (plan §6.1). La copia corre en el hilo de BD; el
        // llamador espera con tope de 30 s. Los comandos admin (/mushoku backupdb) son
        // puntuales; para flujo no bloqueante existe backupAsync().
        try {
            return backupAsync(motivo).get(BACKUP_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            throw new IOException("Backup interrumpido", excepcion);
        } catch (TimeoutException excepcion) {
            throw new IOException("Backup no termino en " + BACKUP_TIMEOUT_SEGUNDOS + " s", excepcion);
        } catch (java.util.concurrent.ExecutionException excepcion) {
            Throwable causa = excepcion.getCause();
            if (causa instanceof IOException excepcionDeCausa) {
                throw excepcionDeCausa;
            }
            throw new IOException("Backup fallo", causa);
        }
    }

    /** Versión no bloqueante preferible para flujos que no sean comando admin puntual. */
    public CompletableFuture<Path> backupAsync(String motivo) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return copiaInterna(conexionSegura(), motivo == null ? "manual" : motivo);
            } catch (IOException excepcion) {
                throw new java.util.concurrent.CompletionException(excepcion);
            }
        }, executor);
    }

    private Path copiaInterna(Connection conexion, String motivo) throws IOException {
        String sufijo = motivo.replaceAll("[^A-Za-z0-9_.-]", "_");
        Path destinoDir = plugin.getDataFolder().toPath().resolve("backups"); // File→Path: toPath() (error "cannot find symbol" original de :355)
        Files.createDirectories(destinoDir);
        Path destino = destinoDir.resolve("mushoku_" + TimeUtils.marcaArchivo() + "_" + sufijo + ".db");
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA wal_checkpoint(TRUNCATE)");
        } catch (SQLException excepcion) {
            throw new IOException("No se pudo hacer checkpoint WAL antes del backup", excepcion);
        }
        try {
            Files.copy(archivo, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException excepcion) {
            throw new IOException("No se pudo copiar " + archivo + " a " + destino, excepcion);
        }
        plugin.getLogger().info("Backup creado: " + destino + " (motivo: " + motivo + ")");
        return destino;
    }

    @Override
    public boolean isHealthy() {
        if (cerrado.get()) {
            return false;
        }
        try {
            return read(conexion -> {
                try (Statement sentencia = conexion.createStatement();
                     java.sql.ResultSet filas = sentencia.executeQuery("SELECT 1")) {
                    return filas.next();
                } catch (SQLException excepcion) {
                    throw new java.util.concurrent.CompletionException(excepcion);
                }
            }).get(2, TimeUnit.SECONDS);
        } catch (Exception excepcion) {
            return false;
        }
    }

    /**
     * Paso 3 de onDisable (§8 L353): espera a que el executor vacíe todo lo encolado.
     * Se llama DESDE el hilo principal durante el apagado (permitido: es shutdown, §8).
     */
    @Override
    public void flushAll() throws Exception {
        if (cerrado.get()) {
            return;
        }
        CompletableFuture<Void> barrera = CompletableFuture.runAsync(() -> { }, executor);
        barrera.get(FLUSH_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
        plugin.getLogger().info("Colas de escritura vaciadas (flushAll).");
    }

    /**
     * Paso 4 de onDisable (§8 L353): vacía colas (si nadie lo hizo) y cierra la conexión.
     * Idempotente: cerrar dos veces no lanza.
     */
    public void cerrar() {
        if (!cerrado.compareAndSet(false, true)) {
            return;
        }
        try {
            CompletableFuture<Void> barrera = CompletableFuture.runAsync(() -> { }, executor);
            barrera.get(FLUSH_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
        } catch (Exception excepcion) {
            plugin.getLogger().log(Level.WARNING,
                    "flushAll durante cierre fallo; se cierra igual (mejor perder cola que colgar)", excepcion);
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(FLUSH_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
        try {
            if (conexionReal != null) {
                conexionReal.close();
            }
        } catch (SQLException excepcion) {
            plugin.getLogger().log(Level.WARNING, "Fallo al cerrar la conexion SQLite", excepcion);
        }
        plugin.getLogger().info("SQLite cerrado (" + archivo.getFileName() + ").");
    }

    /** @return ruta del archivo de base (para diagnósticos de /mushoku diagnostico). */
    public Path archivo() {
        return archivo;
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private void exigirNoHiloPrincipal(String operacion) {
        if (cerrado.get()) {
            throw new IllegalStateException("IDatabase." + operacion
                    + ": la base ya fue cerrada (onDisable).");
        }
        if (hiloPrincipal.getAsBoolean()) {
            // Criterio duro §26 L1039 / plan §6 regla 3: fallar ruidoso en desarrollo
            // es mejor que un cuelgue silencioso en producción.
            throw new IllegalStateException("IDatabase." + operacion
                    + " invocado desde el hilo principal de Bukkit. Prohibido (§26 L1039):"
                    + " componer con thenApply/thenAccept desde un hilo async.");
        }
    }

    private Connection conexionSegura() {
        if (conexionReal == null) {
            throw new IllegalStateException("Conexion SQLite sin abrir (arranque fallido).");
        }
        return conexionReal;
    }
}
