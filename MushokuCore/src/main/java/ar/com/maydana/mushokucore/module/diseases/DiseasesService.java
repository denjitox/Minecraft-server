package ar.com.maydana.mushokucore.module.diseases;

import ar.com.maydana.mushokucore.audit.IAuditoria;
import ar.com.maydana.mushokucore.config.Messages;
import ar.com.maydana.mushokucore.database.IDatabase;
import ar.com.maydana.mushokucore.database.repository.DiseaseRepository;
import ar.com.maydana.mushokucore.integration.LuckPermsHook;
import ar.com.maydana.mushokucore.model.Disease;
import ar.com.maydana.mushokucore.util.TimeUtils;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Implementación de {@code IEnfermedadesService} — agente C (enfermedades), Fase 1.
 *
 * <p>Reglas duras que respeta:</p>
 * <ul>
 *   <li><b>Cero SQL en el hilo principal</b> (§26 L1039): toda lectura/escritura pasa por
 *       {@link DiseaseRepository} (async, sobre {@code IDatabase}); nunca {@code join()/get()}.
 *       La <i>invocación</i> se despacha con {@link #enHiloAsync(Runnable)} porque el guard de
 *       {@code DatabaseManager} rechaza que {@code read}/{@code write} <b>arranquen</b> en el hilo
 *       principal (medido: {@code IllegalStateException} real en el servidor del panel,
 *       2026-10-18, desde {@code DiseaseExpiryTask}).</li>
 *   <li><b>Mutaciones Bukkit en el hilo principal</b>: los efectos se aplican/limpian con
 *       {@code addPotionEffect}/{@code removePotionEffect} siempre vía {@link #enPrincipal(Runnable)}.</li>
 *   <li>Los efectos se reaplican al entrar ({@code DiseaseRestoreListener}) y se limpian al
 *       curar/expirar; el estado persiste en la tabla {@code diseases} (sobrevive reconexión).</li>
 *   <li>Sin tarea por jugador por tick (§13 L692): una sola tarea periódica
 *       ({@code DiseaseExpiryTask}) recorre la caché en memoria.</li>
 * </ul>
 *
 * <p><b>[PROPUESTA]</b> Se admite <b>más de una enfermedad activa a la vez</b> (el catálogo
 * tiene fuentes distintas e independientes). {@link #activa(UUID)} devuelve la de <b>expiración
 * más próxima</b>. El interface F0 dejaba "una sola" como propuesta; si el integrador confirma
 * "una sola", se revisa el contrato en el plan §6.1 (no se edita el interface acá).</p>
 */
public final class DiseasesService implements IEnfermedadesService {

    private static final int TICKS_POR_SEGUNDO = 20;
    /** Fila de enfermedad activa en memoria (caché del jugador online). */
    private record Activa(String diseaseId, Instant contratada, Instant expira, String fuente) {
    }

    private final JavaPlugin plugin;
    private final DiseaseRepository repositorio;
    private final IAuditoria auditoria;
    private final LuckPermsHook luckPerms;
    private final Messages mensajes;
    private final Logger log;
    private final double multiplicadorOrsted;
    private final String permisoOrsted;

    private final Map<String, DiseaseDefinition> catalogo = new LinkedHashMap<>();
    private final Map<String, List<DiseaseDefinition>> porFuente = new LinkedHashMap<>();
    private final Set<PotionEffectType> tiposGestionados = new LinkedHashSet<>();

    private final Map<UUID, Map<String, Activa>> cache = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Instant>> inmunidad = new ConcurrentHashMap<>();
    private final Random azar = new Random();

    public DiseasesService(JavaPlugin plugin, IDatabase db, IAuditoria auditoria, LuckPermsHook luckPerms,
                           Messages mensajes, Logger log,
                           double multiplicadorOrsted, String permisoOrsted) {
        this.plugin = plugin;
        this.repositorio = new DiseaseRepository(db);
        this.auditoria = auditoria;
        this.luckPerms = luckPerms;
        this.mensajes = mensajes;
        this.log = log;
        this.multiplicadorOrsted = multiplicadorOrsted;
        this.permisoOrsted = permisoOrsted;
    }

    // ------------------------------------------------------------------
    // Catálogo (diseases.yml)
    // ------------------------------------------------------------------

    /**
     * Carga el catálogo de {@code diseases.yml}. Prefiere la copia editable del data folder
     * ({@code plugins/MushokuCore/diseases.yml}) y cae al recurso del JAR si no existe
     * <b>[PROPUESTA]</b> — así el operador puede ajustar probabilidades sin recompilar.
     * @return cantidad de enfermedades cargadas
     */
    public int cargarCatalogo() {
        YamlConfiguration yaml = leerYaml();
        catalogo.clear();
        porFuente.clear();
        tiposGestionados.clear();
        if (yaml == null) {
            log.warning("diseases.yml no encontrado (ni en el data folder ni en el JAR). El modulo no aplicara enfermedades.");
            return 0;
        }
        for (String id : yaml.getKeys(false)) {
            if (!yaml.isConfigurationSection(id)) {
                continue;
            }
            DiseaseDefinition def = parsear(id, yaml.getConfigurationSection(id));
            catalogo.put(id, def);
            for (String fuente : def.fuentes()) {
                porFuente.computeIfAbsent(fuente, k -> new ArrayList<>()).add(def);
            }
            tiposGestionados.addAll(def.efectos().keySet());
        }
        log.info("diseases.yml: " + catalogo.size() + " enfermedad(es), " + tiposGestionados.size()
                + " tipo(s) de efecto gestionado(s).");
        return catalogo.size();
    }

    private YamlConfiguration leerYaml() {
        File editable = new File(plugin.getDataFolder(), "diseases.yml");
        if (editable.exists()) {
            return YamlConfiguration.loadConfiguration(editable);
        }
        try (InputStream recurso = plugin.getResource("diseases.yml")) {
            if (recurso == null) {
                return null;
            }
            return YamlConfiguration.loadConfiguration(
                    new InputStreamReader(recurso, StandardCharsets.UTF_8));
        } catch (Exception excepcion) {
            log.log(Level.SEVERE, "No se pudo leer diseases.yml del JAR.", excepcion);
            return null;
        }
    }

    private DiseaseDefinition parsear(String id, ConfigurationSection sec) {
        String display = sec.getString("display-name", id);
        Set<String> fuentes = new LinkedHashSet<>();
        for (String fuente : sec.getStringList("sources")) {
            if (fuente != null && !fuente.isBlank()) {
                fuentes.add(fuente.trim().toUpperCase(Locale.ROOT));
            }
        }
        double chance = sec.getDouble("base-chance-percent", 0.0);
        int duracion = sec.getInt("duration-seconds", 0);

        Map<PotionEffectType, Integer> efectos = new LinkedHashMap<>();
        ConfigurationSection efectosSec = sec.getConfigurationSection("effects");
        if (efectosSec != null) {
            for (Map.Entry<String, Object> entrada : efectosSec.getValues(false).entrySet()) {
                PotionEffectType tipo = PotionEffectType.getByName(entrada.getKey().toUpperCase(Locale.ROOT));
                if (tipo == null) {
                    log.warning("diseases.yml: efecto desconocido '" + entrada.getKey()
                            + "' en '" + id + "' (se omite).");
                    continue;
                }
                int amplificador = entrada.getValue() instanceof Number numero ? numero.intValue() : 0;
                efectos.put(tipo, amplificador);
            }
        }
        Set<String> curas = new LinkedHashSet<>();
        for (String cura : sec.getStringList("cures")) {
            if (cura != null && !cura.isBlank()) {
                curas.add(cura.trim().toUpperCase(Locale.ROOT));
            }
        }
        int inmunidad = sec.getInt("immunity-after-cure-seconds", 0);
        return new DiseaseDefinition(id, display, fuentes, chance, duracion, efectos, curas, inmunidad);
    }

    /** Ids del catálogo (para autocompletado y validación del comando). */
    public List<String> idsCatalogo() {
        return List.copyOf(catalogo.keySet());
    }

    /** Nombre visible de una enfermedad (o el id si no está). */
    public String nombreVisible(String id) {
        DiseaseDefinition def = catalogo.get(id);
        return def == null ? id : def.displayName();
    }

    // ------------------------------------------------------------------
    // IEnfermedadesService
    // ------------------------------------------------------------------

    @Override
    public Optional<Disease> activa(UUID jugador) {
        Map<String, Activa> activas = cache.get(jugador);
        if (activas == null || activas.isEmpty()) {
            return Optional.empty();
        }
        Activa elegida = null;
        for (Activa candidata : activas.values()) {
            if (elegida == null || expiraAntes(candidata, elegida)) {
                elegida = candidata;
            }
        }
        DiseaseDefinition def = catalogo.get(elegida.diseaseId());
        if (def == null) {
            return Optional.empty();
        }
        return Optional.of(new Disease(def.id(), def.displayName(), elegida.fuente(), elegida.expira()));
    }

    @Override
    public boolean aplicar(UUID jugador, String diseaseId, String source) {
        if (jugador == null || diseaseId == null) {
            return false;
        }
        DiseaseDefinition def = catalogo.get(diseaseId);
        if (def == null) {
            return false;
        }
        Map<String, Activa> activas = cache.computeIfAbsent(jugador, k -> new ConcurrentHashMap<>());
        if (activas.containsKey(diseaseId)) {
            return false; // sin reinfeccion de la misma enfermedad [PROPUESTA plan §4.4]
        }
        Instant ahora = Instant.now();
        if (enInmunidad(jugador, diseaseId, ahora)) {
            return false;
        }
        Instant expira = def.duracionSegundos() > 0 ? ahora.plusSeconds(def.duracionSegundos()) : null;
        activas.put(diseaseId, new Activa(diseaseId, ahora, expira, source));

        Player online = Bukkit.getPlayer(jugador);
        if (online != null) {
            recomputarEfectos(online);
            avisar(online, "enfermedad.infected", Map.of(
                    "display-name", def.displayName(),
                    "efectos-texto", textoEfectos(def),
                    "duracion", textoDuracion(def.duracionSegundos())));
        }
        enHiloAsync(() -> repositorio.aplicar(jugador, def.id(), TimeUtils.iso(ahora),
                expira == null ? null : TimeUtils.iso(expira), source)
                .whenComplete((vacio, fallo) -> {
                    if (fallo != null) {
                        log.log(Level.WARNING, "No se pudo persistir la enfermedad " + def.id()
                                + " de " + jugador, fallo);
                    }
                }));
        auditar("enfermedad.aplicar", jugador, def.id() + " fuente=" + source);
        return true;
    }

    @Override
    public int curar(UUID jugador, String diseaseId) {
        if (jugador == null) {
            return 0;
        }
        int curadas = 0;
        Map<String, Activa> activas = cache.get(jugador);
        if (activas != null) {
            List<String> objetivos = new ArrayList<>();
            if (diseaseId == null) {
                objetivos.addAll(activas.keySet());
            } else if (activas.containsKey(diseaseId)) {
                objetivos.add(diseaseId);
            }
            Instant ahora = Instant.now();
            for (String id : objetivos) {
                if (activas.remove(id) == null) {
                    continue;
                }
                DiseaseDefinition def = catalogo.get(id);
                if (def != null && def.inmunidadSegundos() > 0) {
                    marcarInmunidad(jugador, id, ahora.plusSeconds(def.inmunidadSegundos()));
                }
                curadas++;
                auditar("enfermedad.curar", jugador, id);
            }
            Player online = Bukkit.getPlayer(jugador);
            if (online != null) {
                recomputarEfectos(online);
            }
        }
        // Para un jugador offline no hay caché: se emite el DELETE igual (curadas queda en 0
        // porque el contrato pide un int síncrono y no se puede contar sin bloquear el hilo).
        persistirCurar(jugador, diseaseId);
        return curadas;
    }

    // ------------------------------------------------------------------
    // API extra del módulo (la consumen mis listeners y el comando)
    // ------------------------------------------------------------------

    /**
     * Tirada de contagio por daño con fuente de {@code diseases.yml} (§13 L684-693).
     * <p>[PENDIENTE D11] la reducción Orsted se toma de un <b>permiso LuckPerms</b>
     * ({@code mt.alianza.orsted}) — es la opción A del plan §9 D11; se aplica <b>antes</b> de
     * la tirada (§13 L687, §12 L620). Si LuckPerms no está, {@code tienePermiso} devuelve false.</p>
     */
    public boolean intentarContagio(Player victima, String fuente) {
        if (victima == null || fuente == null) {
            return false;
        }
        List<DiseaseDefinition> candidatas = porFuente.get(fuente.toUpperCase(Locale.ROOT));
        if (candidatas == null || candidatas.isEmpty()) {
            return false;
        }
        UUID uuid = victima.getUniqueId();
        boolean orsted = luckPerms != null && permisoOrsted != null && !permisoOrsted.isBlank()
                && luckPerms.tienePermiso(uuid, permisoOrsted);
        boolean aplicada = false;
        for (DiseaseDefinition def : candidatas) {
            if (def.chanceBasePorcentaje() <= 0) {
                continue;
            }
            double chance = def.chanceBasePorcentaje();
            if (orsted) {
                chance *= multiplicadorOrsted;
            }
            if (azar.nextDouble(100.0) >= chance) {
                continue;
            }
            if (aplicar(uuid, def.id(), fuente)) {
                aplicada = true;
            }
        }
        return aplicada;
    }

    /**
     * Cura las enfermedades activas del jugador que declaren {@code cures: [MILK]}
     * (§13 L690). <b>[PENDIENTE V15]</b>: el Skript actual borra <i>todo</i> efecto con leche;
     * acá se obedece la config (plan §4.4) y solo se cura lo marcado — decisión del usuario.
     * @return cantidad de enfermedades curadas
     */
    public int curarConLeche(Player jugador) {
        if (jugador == null) {
            return 0;
        }
        UUID uuid = jugador.getUniqueId();
        Map<String, Activa> activas = cache.get(uuid);
        if (activas == null) {
            return 0;
        }
        List<String> objetivos = new ArrayList<>();
        for (String id : activas.keySet()) {
            DiseaseDefinition def = catalogo.get(id);
            if (def != null && def.curas().contains("MILK")) {
                objetivos.add(id);
            }
        }
        if (objetivos.isEmpty()) {
            return 0;
        }
        Instant ahora = Instant.now();
        int curadas = 0;
        for (String id : objetivos) {
            activas.remove(id);
            DiseaseDefinition def = catalogo.get(id);
            if (def != null && def.inmunidadSegundos() > 0) {
                marcarInmunidad(uuid, id, ahora.plusSeconds(def.inmunidadSegundos()));
            }
            persistirCurar(uuid, id);
            auditar("enfermedad.curar", uuid, id + " (leche)");
            avisar(jugador, "enfermedad.cured", Map.of("display-name", nombreVisible(id)));
            curadas++;
        }
        recomputarEfectos(jugador);
        return curadas;
    }

    /**
     * Reaplica el estado persistido al entrar (§13 L689) — lectura async, efectos en el hilo
     * principal. También se llama al habilitar el módulo sobre los jugadores ya conectados
     * (cubre un {@code /reload} del plugin).
     */
    public void cargar(UUID jugador) {
        enHiloAsync(() -> repositorio.activas(jugador, TimeUtils.ahoraIso()).whenComplete((filas, fallo) -> {
            if (fallo != null) {
                log.log(Level.WARNING, "No se pudieron leer las enfermedades de " + jugador, fallo);
                return;
            }
            List<Activa> leidas = new ArrayList<>();
            for (DiseaseRepository.FilaEnfermedad fila : filas) {
                leidas.add(new Activa(fila.diseaseId(), TimeUtils.parse(fila.contratadaEn()),
                        TimeUtils.parse(fila.expiraEn()), fila.fuente()));
            }
            enPrincipal(() -> {
                Player online = Bukkit.getPlayer(jugador);
                if (online == null) {
                    // Se fue antes de que volviera la lectura: no se revive la caché de un offline.
                    return;
                }
                Map<String, Activa> mapa = new ConcurrentHashMap<>();
                for (Activa activa : leidas) {
                    if (catalogo.containsKey(activa.diseaseId())) {
                        mapa.put(activa.diseaseId(), activa);
                    }
                }
                cache.put(jugador, mapa);
                recomputarEfectos(online);
            });
        }));
    }

    /**
     * Flush al salir (§13 L689): suelta la caché y la inmunidad del jugador. El estado ya se
     * persistió <i>write-through</i> en cada mutación (la cola ordenada de {@code IDatabase}
     * completa los pendientes), así que "flush" acá es evacuar la copia en memoria para que
     * al reconectar se relea de la base y sobreviva a la reconexión.
     */
    public void descargar(UUID jugador) {
        cache.remove(jugador);
        inmunidad.remove(jugador);
    }

    /** Recorre la caché y expira lo vencido (llamado por {@link DiseaseExpiryTask} en el hilo principal). */
    void procesarExpiraciones() {
        Instant ahora = Instant.now();
        Set<UUID> afectados = new LinkedHashSet<>();
        for (Map.Entry<UUID, Map<String, Activa>> entrada : cache.entrySet()) {
            List<String> vencidas = new ArrayList<>();
            for (Activa activa : entrada.getValue().values()) {
                if (activa.expira() != null && !activa.expira().isAfter(ahora)) {
                    vencidas.add(activa.diseaseId());
                }
            }
            for (String id : vencidas) {
                if (entrada.getValue().remove(id) != null) {
                    persistirCurar(entrada.getKey(), id);
                    auditar("enfermedad.expirar", entrada.getKey(), id);
                    afectados.add(entrada.getKey());
                }
            }
        }
        for (UUID uuid : afectados) {
            Player online = Bukkit.getPlayer(uuid);
            if (online != null) {
                recomputarEfectos(online);
            }
        }
    }

    /** Purga las filas ya vencidas de jugadores offline (§13 L693, repo {@code expirar}). Housekeeping periódico. */
    void purgarExpiradas() {
        // La tarea que la llama es síncrona (hilo principal): la invocación de SQL se despacha async.
        enHiloAsync(() -> repositorio.expirar(TimeUtils.ahoraIso()).whenComplete((borradas, fallo) -> {
            if (fallo != null) {
                log.log(Level.WARNING, "No se pudieron purgar enfermedades expiradas.", fallo);
            }
        }));
    }

    /** Cierre del módulo: suelta el estado en memoria (las tareas las cancela el módulo). */
    public void detener() {
        cache.clear();
        inmunidad.clear();
    }

    // ------------------------------------------------------------------
    // Internos
    // ------------------------------------------------------------------

    private void recomputarEfectos(Player jugador) {
        Map<String, Activa> activas = cache.get(jugador.getUniqueId());
        Map<PotionEffectType, int[]> deseados = new LinkedHashMap<>(); // tipo -> {amplificador, ticks}
        if (activas != null) {
            Instant ahora = Instant.now();
            for (Activa activa : activas.values()) {
                DiseaseDefinition def = catalogo.get(activa.diseaseId());
                if (def == null) {
                    continue;
                }
                int ticks = activa.expira() == null
                        ? PotionEffect.INFINITE_DURATION
                        : Math.max(TICKS_POR_SEGUNDO,
                                (int) Duration.between(ahora, activa.expira()).getSeconds() * TICKS_POR_SEGUNDO);
                for (Map.Entry<PotionEffectType, Integer> efecto : def.efectos().entrySet()) {
                    int[] actual = deseados.get(efecto.getKey());
                    if (actual == null) {
                        deseados.put(efecto.getKey(), new int[]{efecto.getValue(), ticks});
                    } else {
                        actual[0] = Math.max(actual[0], efecto.getValue());
                        actual[1] = Math.max(actual[1], ticks);
                    }
                }
            }
        }
        // Solo se tocan los tipos que este módulo gestiona: no se pisa un efecto ajeno.
        for (PotionEffectType tipo : tiposGestionados) {
            if (!deseados.containsKey(tipo)) {
                jugador.removePotionEffect(tipo);
            }
        }
        for (Map.Entry<PotionEffectType, int[]> efecto : deseados.entrySet()) {
            jugador.addPotionEffect(new PotionEffect(efecto.getKey(), efecto.getValue()[1],
                    efecto.getValue()[0], false, true, true), true);
        }
    }

    private static boolean expiraAntes(Activa a, Activa b) {
        if (a.expira() == null) {
            return false;
        }
        if (b.expira() == null) {
            return true;
        }
        return a.expira().isBefore(b.expira());
    }

    private boolean enInmunidad(UUID jugador, String diseaseId, Instant ahora) {
        Map<String, Instant> mapa = inmunidad.get(jugador);
        if (mapa == null) {
            return false;
        }
        Instant hasta = mapa.get(diseaseId);
        if (hasta == null) {
            return false;
        }
        if (hasta.isAfter(ahora)) {
            return true;
        }
        mapa.remove(diseaseId);
        return false;
    }

    private void marcarInmunidad(UUID jugador, String diseaseId, Instant hasta) {
        inmunidad.computeIfAbsent(jugador, k -> new ConcurrentHashMap<>()).put(diseaseId, hasta);
    }

    private void persistirCurar(UUID jugador, String diseaseId) {
        enHiloAsync(() -> repositorio.curar(jugador, diseaseId).whenComplete((borradas, fallo) -> {
            if (fallo != null) {
                log.log(Level.WARNING, "No se pudo persistir la cura de enfermedad de " + jugador, fallo);
            }
        }));
    }

    private void auditar(String accion, UUID objetivo, String detalles) {
        if (auditoria != null) {
            auditoria.registrar(null, accion, objetivo == null ? null : objetivo.toString(), detalles);
        }
    }

    private void avisar(Player jugador, String clave, Map<String, String> reemplazos) {
        if (mensajes == null) {
            return;
        }
        jugador.sendMessage(mensajes.texto("prefix") + " " + mensajes.texto(clave, reemplazos));
    }

    private void enPrincipal(Runnable accion) {
        if (Bukkit.isPrimaryThread()) {
            accion.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, accion);
        }
    }

    /**
     * Despacha una <i>iniciación</i> de I/O al hilo asíncrono (plan §6 regla 3, §26 L1039): el
     * guard de {@code DatabaseManager.exigirNoHiloPrincipal} lanza {@link IllegalStateException}
     * si {@code read}/{@code write}/{@code transaction} <b>arrancan</b> en el hilo principal —
     * el repositorio es async en la ejecución pero no en la invocación. Mismo idioma que
     * {@code PotenciasService.enHiloAsync}.
     *
     * <p>Si ya estamos fuera del principal, corre en línea; si el plugin se está apagando, la
     * operación se descarta (housekeeping, no dato crítico: la caché en memoria ya se actualizó).</p>
     */
    private void enHiloAsync(Runnable tarea) {
        if (!Bukkit.isPrimaryThread()) {
            tarea.run();
            return;
        }
        try {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, tarea);
        } catch (RuntimeException apagando) {
            // En onDisable el scheduler lanza IllegalPluginAccessException (RuntimeException): se
            // descarta la operación, no se propaga (mismo criterio que PotenciasService).
            log.fine("Diseases: scheduler no disponible (apagado); se descarta la operacion asincrona.");
        }
    }

    /** [PROPUESTA] texto simple de efectos (claves de vanilla, no la prosa del Skript). */
    private static String textoEfectos(DiseaseDefinition def) {
        List<String> nombres = new ArrayList<>();
        for (PotionEffectType tipo : def.efectos().keySet()) {
            nombres.add(tipo.getKey().getKey());
        }
        return String.join(", ", nombres);
    }

    /** [PROPUESTA] duración legible. */
    private static String textoDuracion(int segundos) {
        if (segundos <= 0) {
            return "indefinido";
        }
        if (segundos % 60 == 0) {
            return (segundos / 60) + " minutos";
        }
        return segundos + " segundos";
    }
}
