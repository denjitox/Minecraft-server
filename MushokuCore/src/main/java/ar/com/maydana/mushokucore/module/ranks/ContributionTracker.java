package ar.com.maydana.mushokucore.module.ranks;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Buffer en memoria del daño contra jefes — spec §11 L552-559 y §19 L863
 * ("no guardar a disco por cada punto de daño; acumular en memoria y realizar escrituras
 * agrupadas seguras"). Dueño: agente B (plan §6.3).
 *
 * <p>Todas las operaciones son de solo memoria y {@code synchronized}: se llama desde el hilo
 * principal (eventos) y desde tareas asíncronas (vuelco periódico). La escritura a
 * {@code boss_damage} la hace {@code task/BossBufferFlushTask}; la resolución final usa este
 * buffer (riesgo técnico del plan §11: la muerte del jefe flusha inmediatamente, §19 L864).</p>
 *
 * <p><b>Tope de overkill (§11 L558)</b>: el daño efectivo se calcula en el listener
 * ({@code min(dañoFinal, vidaPreDaño)}); este buffer solo acumula lo que recibe.</p>
 *
 * <p><b>Semántica del vuelco (decisión de B, 2026-10-18):</b>
 * {@code BossDamageRepository.sumarDanio} <b>suma</b> ({@code damage=damage+excluded.damage}),
 * así que {@link #drenarParaVuelco()} devuelve <b>deltas</b> desde el último vuelco, no los
 * totales: enviar totales dos veces duplicaría el daño en la tabla. Los totales completos solo
 * los usa {@link #cerrar(UUID)} para resolver la muerte (§11 L569).</p>
 */
public final class ContributionTracker {

    /**
     * Delta de un (jefe, jugador) listo para el upsert aditivo de {@code boss_damage}.
     *
     * @param bossUuid    UUID de la entidad jefe.
     * @param tipoJefe    "WARDEN" | "ENDER_DRAGON" | "WITHER".
     * @param jugador     UUID del atacante real.
     * @param danio       daño efectivo <b>no volcado todavía</b> (delta).
     * @param ultimoGolpeIso instante ISO-8601 UTC del último golpe (formato de las columnas TEXT).
     */
    public record GolpeAcumulado(UUID bossUuid, String tipoJefe, UUID jugador, double danio, String ultimoGolpeIso) {
    }

    /**
     * Fotografía de una pelea cerrada (el jefe murió). Lleva los totales completos.
     *
     * @param mundo nombre del mundo del jefe (§11 L565: mundo/evento permitido); vacío si la
     *              pelea se restauró desde disco (la tabla no guarda el mundo).
     */
    public record Pelea(UUID bossUuid, String tipoJefe, String mundo,
                        Map<UUID, Double> danioPorJugador, Map<UUID, Long> ultimoGolpeMs) {
    }

    private static final class PeleaInterna {
        private final String tipoJefe;
        private final String mundo;
        private final Map<UUID, Double> danio = new HashMap<>();
        private final Map<UUID, Double> volcado = new HashMap<>();
        private final Map<UUID, Long> ultimoGolpeMs = new HashMap<>();
        private long ultimaActividadMs;

        private PeleaInterna(String tipoJefe, String mundo, long ahoraMs) {
            this.tipoJefe = tipoJefe;
            this.mundo = mundo;
            this.ultimaActividadMs = ahoraMs;
        }
    }

    private final Map<UUID, PeleaInterna> peleas = new HashMap<>();

    /** Registra un golpe con su daño efectivo (ya con tope de overkill aplicado por el listener). */
    public synchronized void registrarDanio(UUID bossUuid, String tipoJefe, String mundo,
                                            UUID jugador, double danioEfectivo, long ahoraMs) {
        if (danioEfectivo <= 0.0) {
            return;
        }
        PeleaInterna pelea = peleas.get(bossUuid);
        if (pelea == null) {
            pelea = new PeleaInterna(tipoJefe, mundo, ahoraMs);
            peleas.put(bossUuid, pelea);
        }
        pelea.danio.merge(jugador, danioEfectivo, Double::sum);
        pelea.ultimoGolpeMs.put(jugador, ahoraMs);
        pelea.ultimaActividadMs = ahoraMs;
    }

    /**
     * Cierra y retira la pelea del jefe (murió: §19 L864 flush inmediato y resolución).
     * @return la pelea con los totales finales, o {@code null} si no había buffer para ese UUID.
     */
    public synchronized Pelea cerrar(UUID bossUuid) {
        PeleaInterna pelea = peleas.remove(bossUuid);
        if (pelea == null) {
            return null;
        }
        return new Pelea(bossUuid, pelea.tipoJefe, pelea.mundo,
                Map.copyOf(pelea.danio), Map.copyOf(pelea.ultimoGolpeMs));
    }

    /** Deltas pendientes de <b>todas</b> las peleas (vuelco periódico, §19 L863). */
    public synchronized List<GolpeAcumulado> drenarParaVuelco() {
        List<GolpeAcumulado> salida = new ArrayList<>();
        for (Map.Entry<UUID, PeleaInterna> entrada : peleas.entrySet()) {
            acumularDeltas(entrada.getKey(), entrada.getValue(), salida);
        }
        return salida;
    }

    /**
     * Deltas pendientes de un solo jefe (muerte del jefe: se vuelca antes de resolver, y lo que
     * ya se volcó no se repite).
     */
    public synchronized List<GolpeAcumulado> drenarParaVuelco(UUID bossUuid) {
        List<GolpeAcumulado> salida = new ArrayList<>();
        PeleaInterna pelea = peleas.get(bossUuid);
        if (pelea != null) {
            acumularDeltas(bossUuid, pelea, salida);
        }
        return salida;
    }

    private static void acumularDeltas(UUID bossUuid, PeleaInterna pelea, List<GolpeAcumulado> salida) {
        long ahora = System.currentTimeMillis();
        for (Map.Entry<UUID, Double> aporte : pelea.danio.entrySet()) {
            UUID jugador = aporte.getKey();
            double yaVolcado = pelea.volcado.getOrDefault(jugador, 0.0);
            double delta = aporte.getValue() - yaVolcado;
            if (delta <= 0.0) {
                continue;
            }
            Long ultimo = pelea.ultimoGolpeMs.get(jugador);
            String iso = Instant.ofEpochMilli(ultimo != null ? ultimo : ahora).toString();
            salida.add(new GolpeAcumulado(bossUuid, pelea.tipoJefe, jugador, delta, iso));
            pelea.volcado.put(jugador, aporte.getValue());
        }
    }

    /**
     * Restaura peleas desde {@code boss_damage} (recuperación tras reinicio, §26 L1038). Lo
     * restaurado ya está en disco: se marca como volcado para no reenviarlo.
     */
    public synchronized void restaurar(UUID bossUuid, String tipoJefe, String mundo,
                                       UUID jugador, double danio, long ultimoGolpeMs) {
        PeleaInterna pelea = peleas.get(bossUuid);
        if (pelea == null) {
            pelea = new PeleaInterna(tipoJefe, mundo, ultimoGolpeMs);
            peleas.put(bossUuid, pelea);
        }
        double total = pelea.danio.merge(jugador, danio, Double::sum);
        pelea.volcado.put(jugador, total);
        pelea.ultimoGolpeMs.put(jugador, ultimoGolpeMs);
        if (ultimoGolpeMs > pelea.ultimaActividadMs) {
            pelea.ultimaActividadMs = ultimoGolpeMs;
        }
    }

    /**
     * Descarta peleas sin actividad desde hace {@code vigencia} (el jefe ya no existe: su
     * evento de muerte se perdió, o la entidad se descargó). Equivale a la expiración de
     * contribución ({@code ranks.boss-contribution-expiry-minutes}, §9 L436).
     * @return UUIDs de los jefes descartados, para que la tarea borre sus filas de
     *         {@code boss_damage} (el repositorio llama a eso "si el jefe desaparece").
     */
    public synchronized List<UUID> expirarInactivas(Instant ahora, Duration vigencia) {
        long limite = ahora.toEpochMilli() - vigencia.toMillis();
        List<UUID> descartadas = new ArrayList<>();
        Iterator<Map.Entry<UUID, PeleaInterna>> it = peleas.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PeleaInterna> entrada = it.next();
            if (entrada.getValue().ultimaActividadMs < limite) {
                descartadas.add(entrada.getKey());
                it.remove();
            }
        }
        return descartadas;
    }

    /** @return cantidad de peleas abiertas en el buffer. */
    public synchronized int peleasAbiertas() {
        return peleas.size();
    }
}
