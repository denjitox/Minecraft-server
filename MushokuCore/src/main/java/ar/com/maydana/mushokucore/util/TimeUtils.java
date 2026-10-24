package ar.com.maydana.mushokucore.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Timestamps — agente A (infra-core), plan §2 árbol {@code util/(TimeUtils, Uuids)}.
 *
 * <p>Formato de la base: texto ISO-8601 UTC (spec §10 usa {@code TEXT} sin fijar el
 * formato exacto; [PROPUESTA plan §3 / ambigüedad V17] = {@link Instant#toString()}).
 * Todos los repositorios de A escriben y leen con estos helpers para que el orden
 * lexicográfico coincida con el orden cronológico.</p>
 */
public final class TimeUtils {

    /** Formato de nombres de archivo de backup: {@code 20261017T153045Z}. */
    private static final DateTimeFormatter ARCHIVO =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private TimeUtils() {
    }

    /** Instante actual en ISO-8601 UTC (ej. {@code 2026-10-17T18:03:21.456Z}). */
    public static String ahoraIso() {
        return Instant.now().toString();
    }

    /** Instante dado en ISO-8601 UTC. */
    public static String iso(Instant instante) {
        return instante.toString();
    }

    /** Parsea ISO-8601; devuelve null si el texto es null o inválido (jamás lanza). */
    public static Instant parse(String iso) {
        if (iso == null || iso.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(iso);
        } catch (DateTimeParseException excepcion) {
            return null;
        }
    }

    /**
     * Marca de tiempo apta para nombre de archivo (sin caracteres prohibidos en Windows).
     * Usado por {@code DatabaseManager.backup()} para los nombres en {@code backups/}.
     */
    public static String marcaArchivo() {
        return ARCHIVO.format(Instant.now());
    }

    /** LocalDateTime UTC solo para logs legibles de consola. */
    public static String paraLog(Instant instante) {
        return LocalDateTime.ofInstant(instante, ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'"));
    }
}
