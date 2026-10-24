package ar.com.maydana.mushokucore.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Aplica las migraciones {@code VNNN__nombre.sql} en orden — agente A (infra-core),
 * spec §7 L306, §10 L535 ("nunca borrar migraciones ya publicadas"), §20 L889
 * (copia de BD antes de migrar: la copia la dispara {@link DatabaseManager} antes de
 * llamar a {@link #aplicar}; este runner solo calcula y aplica).
 *
 * <p>El listado de archivos los aporta el llamador (del JAR o del directorio en disco),
 * así la clase es testeable sin Bukkit. Cada migración se registra en
 * {@code schema_version} con su número y timestamp ISO-8601 UTC.</p>
 *
 * <p>Fallo seguro: una migración que revienta <b>no</b> se registra, se propaga la
 * excepción completa y {@code DatabaseManager} deja la base cerrada — el plugin no arranca
 * con el esquema a medias (spec §8: catch → disablePlugin).</p>
 */
public final class MigrationRunner {

    /** Una migración ya leída del classpath/disco. */
    public record Migracion(int version, String nombre, String sql) {

        /**
         * {@code V001__initial.sql} → 1. Formato: {@code V<3 dígitos>__<nombre>.sql}.
         *
         * <p><b>Tolerante al prefijo de carpeta</b> (arreglado 2026-10-18, integrador): el
         * listado del JAR entrega la ruta completa ({@code migrations/V001__initial.sql}).
         * Antes, ese camino lanzaba {@code IllegalArgumentException} y el plugin entero
         * quedaba deshabilitado al arrancar (medido en la prueba de arranque del 2026-10-18;
         * el test de A usaba el directorio en disco, sin prefijo, así que no lo cubría).</p>
         */
        public static int versionDeNombre(String nombreArchivo) {
            if (nombreArchivo == null) {
                throw new IllegalArgumentException("Nombre de migracion nulo");
            }
            String base = nombreArchivo.replace('\\', '/');
            int corte = base.lastIndexOf('/');
            if (corte >= 0) {
                base = base.substring(corte + 1);
            }
            if (!base.matches("V\\d{3}__.*\\.sql")) {
                throw new IllegalArgumentException(
                        "Nombre de migracion invalido (se espera VNNN__algo.sql): " + nombreArchivo);
            }
            return Integer.parseInt(base.substring(1, 4));
        }
    }

    private final Logger log;

    public MigrationRunner(Logger log) {
        this.log = log;
    }

    /**
     * Aplica todas las migraciones pendientes en orden numérico.
     *
     * @param conn        conexión ya abierta con los PRAGMAs aplicados.
     * @param disponibles todas las migraciones del proyecto (se filtran por las ya aplicadas).
     * @return cantidad de migraciones aplicadas en esta corrida.
     * @throws SQLException si alguna sentencia falla (no se registra esa versión).
     * @throws IllegalArgumentException si hay dos migraciones con la misma versión.
     */
    public int aplicar(Connection conn, List<Migracion> disponibles) throws SQLException {
        List<Migracion> ordenadas = new ArrayList<>(disponibles);
        ordenadas.sort(Comparator.comparingInt(Migracion::version));
        for (int i = 1; i < ordenadas.size(); i++) {
            if (ordenadas.get(i).version() == ordenadas.get(i - 1).version()) {
                throw new IllegalArgumentException("Version de migracion duplicada: "
                        + ordenadas.get(i).version() + " (" + ordenadas.get(i - 1).nombre()
                        + " y " + ordenadas.get(i).nombre() + ")");
            }
        }

        // NO se pre-crea schema_version: la propia V001 la crea (CREATE TABLE sin
        // IF NOT EXISTS) y duplicarla haría fallar la migración. Si no existe, la
        // lista de aplicadas viene vacía y V001 corre completa.
        List<Integer> aplicadas = versionesAplicadas(conn);
        int aplicadasAntes = aplicadas.size();

        for (Migracion migracion : ordenadas) {
            if (aplicadas.contains(migracion.version())) {
                continue;
            }
            log.info("Aplicando migracion " + migracion.nombre() + " ...");
            ejecutarSentencias(conn, migracion.sql());
            try (Statement registro = conn.createStatement()) {
                registro.executeUpdate("INSERT INTO schema_version (version, applied_at) VALUES ("
                        + migracion.version() + ", '"
                        + ar.com.maydana.mushokucore.util.TimeUtils.ahoraIso() + "')");
            }
            log.info("Migracion " + migracion.nombre() + " aplicada.");
        }
        return ordenadas.size() - aplicadasAntes;
    }

    /**
     * {@code schema_version} la crea la propia V001; antes de esa tabla (base nueva) la
     * lista de aplicadas es vacía. Si falta la tabla y no hay migraciones que aplicar,
     * no pasa nada: V001 la creará.
     */
    private static List<Integer> versionesAplicadas(Connection conn) throws SQLException {
        List<Integer> resultado = new ArrayList<>();
        try (Statement sentencia = conn.createStatement();
             java.sql.ResultSet filas = sentencia.executeQuery("SELECT version FROM schema_version")) {
            while (filas.next()) {
                resultado.add(filas.getInt(1));
            }
        } catch (SQLException excepcion) {
            String mensaje = excepcion.getMessage() == null ? "" : excepcion.getMessage().toLowerCase();
            if (mensaje.contains("no such table")) {
                return resultado; // base nueva: V001 (o la primera disponible) la crea.
            }
            throw excepcion;
        }
        return resultado;
    }

    /**
     * Divide el script en sentencias y las ejecuta una por una.
     * Strip de comentarios {@code --} (naive: el V001 no tiene literales con {@code --};
     * [A VERIFICAR] si una migración futura necesita {@code --} dentro de un string, hay
     * que acá un parser real). Una sentencia a medias NO se registra (arriba).
     */
    static void ejecutarSentencias(Connection conn, String sql) throws SQLException {
        String limpio = quitarComentarios(sql);
        String[] sentencias = limpio.split(";");
        try (Statement sentencia = conn.createStatement()) {
            for (String una : sentencias) {
                String texto = una.trim();
                if (texto.isEmpty()) {
                    continue;
                }
                sentencia.executeUpdate(texto);
            }
        }
    }

    /** Elimina comentarios de línea {@code -- ...} preservando saltos de línea. */
    static String quitarComentarios(String sql) {
        StringBuilder salida = new StringBuilder(sql.length());
        StringReader lector = new StringReader(sql);
        try {
            String linea;
            BufferedReader buffer = new BufferedReader(lector);
            while ((linea = buffer.readLine()) != null) {
                int comentario = linea.indexOf("--");
                String efectiva = comentario >= 0 ? linea.substring(0, comentario) : linea;
                salida.append(efectiva).append('\n');
            }
        } catch (IOException imposible) {
            // StringReader no lanza IOException: inalcanzable.
            throw new IllegalStateException(imposible);
        }
        return salida.toString();
    }
}
