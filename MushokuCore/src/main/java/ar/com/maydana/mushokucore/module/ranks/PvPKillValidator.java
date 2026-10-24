package ar.com.maydana.mushokucore.module.ranks;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Reglas de validez de una baja PvP — spec §11 L579-586. Dueño: agente B (plan §6.3).
 * <p>Clase <b>pura y sin dependencias de Bukkit</b> (plan §7, criterio: "reglas §11 L579-586
 * testeables unitariamente"). El listener (o quien importe de Skript) arma el {@link Contexto}
 * y solo interpreta el resultado.</p>
 *
 * <p>Las seis exclusiones del spec, en orden de chequeo:</p>
 * <ol>
 *   <li>atacante y víctima son la misma persona (L581);</li>
 *   <li>comparten identidad vinculada conocida (L582) — [DECISIÓN D8 pendiente]: hoy el
 *       productor pasa {@code false}; el contrato congelado {@code IEstadoBedrock} no expone
 *       XUID, así que el detector queda cableado cuando el integrador lo resuelva;</li>
 *   <li>misma pareja ya puntuó en las últimas 12 h (L583, configurable §9 L435);</li>
 *   <li>mundo de prueba / arena no válida / evento excluido (L584) — DECISION D7 (2026-10-18):
 *       solo vale el mundo principal ({@code world.name}) más la lista {@code ranks.excluded-worlds}
 *       (hoy vacía); la lista {@code ranks.valid-boss-worlds} aplica a jefes, no a PvP;</li>
 *   <li>víctima conectada demasiado poco (L585) — DECISION D6 (2026-10-18): umbral configurable
 *       ({@code ranks.min-victim-online-seconds}, hoy 60). Valor {@code -1} = tiempo de
 *       conexión desconocido (p. ej. tras reinicio): <b>no bloquea</b> la baja (no fabricar
 *       rechazos sin dato);</li>
 *   <li>anulada por un administrador (L586) — sin mecanismo de anulación en MVP; el productor
 *       pasa {@code false} y queda registrado para cuando exista.</li>
 * </ol>
 */
public final class PvPKillValidator {

    private PvPKillValidator() {
    }

    /**
     * Estado de una baja PvP candidata. Todo lo necesario para decidir, ya resuelto por el
     * productor: sin lecturas de mundo, reloj ni BD dentro de la regla.
     *
     * @param atacante               UUID del atacante (jugador).
     * @param victima                UUID de la víctima (jugador).
     * @param mundo                  nombre del mundo donde ocurrió la baja.
     * @param mundosExcluidos        {@code ranks.excluded-worlds} (§9).
     * @param mundoPrincipal         {@code world.name} — único mundo válido por defecto (DECISION D7).
     * @param victimaConectadaSegundos tiempo conectada de la víctima; {@code -1} = desconocido.
     * @param victimaMinimaSegundos  {@code ranks.min-victim-online-seconds} (DECISION D6).
     * @param identidadVinculada     true si comparten identidad vinculada conocida [DECISIÓN D8].
     * @param anuladaPorAdmin        true si un administrador la anuló.
     * @param ultimaBajaValidaParMs  epoch ms de la última baja válida de esta pareja, o
     *                               {@code null} si nunca puntuaron contra sí mismos.
     * @param ahoraMs                epoch ms del instante de la baja.
     * @param cooldownParMs          {@code ranks.pvp-same-victim-cooldown-hours} en ms (§9 L435).
     */
    public record Contexto(
            UUID atacante,
            UUID victima,
            String mundo,
            Set<String> mundosExcluidos,
            String mundoPrincipal,
            long victimaConectadaSegundos,
            long victimaMinimaSegundos,
            boolean identidadVinculada,
            boolean anuladaPorAdmin,
            Long ultimaBajaValidaParMs,
            long ahoraMs,
            long cooldownParMs) {
    }

    /**
     * @return vacío si la baja <b>es válida</b>; si no, el motivo estable del rechazo
     *         (para auditoría y tests). El motivo es un identificador snake_case, no un
     *         texto para el jugador.
     */
    public static Optional<String> rechazo(Contexto c) {
        if (c.atacante().equals(c.victima())) {
            return Optional.of("atacante_y_victima_son_la_misma_persona");
        }
        if (c.identidadVinculada()) {
            return Optional.of("identidad_vinculada_conocida");
        }
        if (c.ultimaBajaValidaParMs() != null
                && c.ahoraMs() - c.ultimaBajaValidaParMs() < c.cooldownParMs()) {
            return Optional.of("cooldown_de_pareja_activo");
        }
        if (c.mundosExcluidos() != null && c.mundosExcluidos().contains(c.mundo())) {
            return Optional.of("mundo_excluido");
        }
        if (c.mundoPrincipal() != null && !c.mundoPrincipal().equals(c.mundo())) {
            return Optional.of("mundo_no_principal");
        }
        if (c.victimaConectadaSegundos() >= 0
                && c.victimaConectadaSegundos() < c.victimaMinimaSegundos()) {
            return Optional.of("victima_conectada_demasiado_poco");
        }
        if (c.anuladaPorAdmin()) {
            return Optional.of("anulada_por_administrador");
        }
        return Optional.empty();
    }
}
