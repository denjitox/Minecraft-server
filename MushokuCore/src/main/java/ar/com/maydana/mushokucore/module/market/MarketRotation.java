package ar.com.maydana.mushokucore.module.market;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.WeekFields;

/**
 * Rotacion deterministica de una oferta del mercado.
 *
 * <p>No usa estado en base de datos: el periodo actual (dia o semana, UTC) y un offset
 * estable derivado del id de la oferta determinan si la oferta esta activa.</p>
 *
 * <p>Configuracion YAML tipica:
 * <pre>
 *   rotation:
 *     period: daily   # daily | weekly
 *     every: 3        # activa 1 de cada N periodos (>=1)
 *     offset: 0       # opcional; si falta se deriva del id del item
 * </pre></p>
 */
public record MarketRotation(Period period, int every, int offset) {

    public enum Period {
        DAILY, WEEKLY;

        public static Period from(String raw) {
            if (raw == null || raw.isBlank()) {
                return DAILY;
            }
            return switch (raw.trim().toLowerCase(java.util.Locale.ROOT)) {
                case "weekly", "week", "semanal" -> WEEKLY;
                case "daily", "day", "diario", "diaria" -> DAILY;
                default -> DAILY;
            };
        }
    }

    public MarketRotation {
        if (period == null) {
            period = Period.DAILY;
        }
        if (every < 1) {
            every = 1;
        }
        offset = Math.floorMod(offset, every);
    }

    public boolean estaActiva(String itemId, long nowMillis) {
        if (every <= 1) {
            return true;
        }
        long bucket = bucketActual(nowMillis);
        int slot = Math.floorMod(bucket + (long) offset, every);
        return slot == 0;
    }

    long bucketActual(long nowMillis) {
        Instant instant = Instant.ofEpochMilli(nowMillis);
        if (period == Period.WEEKLY) {
            var date = instant.atZone(ZoneId.of("UTC")).toLocalDate();
            WeekFields wf = WeekFields.ISO;
            int week = date.get(wf.weekOfWeekBasedYear());
            int year = date.get(wf.weekBasedYear());
            // indice de semana estable (aprox): año*53 + semana
            return (long) year * 53L + week;
        }
        // DAILY
        return instant.atZone(ZoneId.of("UTC")).toLocalDate().toEpochDay();
    }

    public static int offsetEstable(String itemId, int every) {
        if (every <= 1) {
            return 0;
        }
        String base = itemId == null ? "" : itemId.toLowerCase(java.util.Locale.ROOT);
        return Math.floorMod(base.hashCode(), every);
    }
}
