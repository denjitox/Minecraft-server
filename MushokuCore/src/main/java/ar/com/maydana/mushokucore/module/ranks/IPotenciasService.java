package ar.com.maydana.mushokucore.module.ranks;

import ar.com.maydana.mushokucore.model.DueñoPuesto;
import java.util.Optional;
import java.util.UUID;

/**
 * CONTRATO CONGELADO — Fase 0 (agente F0) lo declara; <b>el agente B (potencias) lo
 * implementa</b> (p. ej. en {@code PotenciasService.java}) y lo publica en su
 * {@code enable()} con {@code ctx.registrarServicio(IPotenciasService.class, impl)}.
 * <p>Este archivo no se edita sin actualizar el plan §6.1 y avisar al integrador (regla 2).</p>
 *
 * <p>Consumidores: {@code /potencias} (B), {@code /mtestado} (D), placeholders (D).
 * Reglas de dominio: spec §11 (títulos L541-548, contribución L552-575, PvP L577-588).</p>
 */
public interface IPotenciasService {

    /**
     * Lectura de <b>caché</b> — no consultar la BD por render (§19 L874).
     * @param puesto 1–7.
     * @return dueño del puesto; [A VERIFICAR §6.1] vacío cuando el puesto es inválido o está
     *         libre — la semántica de "libre" (vacío vs {@code dueno == null}) la fija B
     *         al implementar; ver javadoc de {@code DueñoPuesto}.
     */
    Optional<DueñoPuesto> propietario(int puesto);

    /**
     * @return puesto del jugador (1–7) o <b>0</b> si no tiene puesto (para
     *         {@code %mushoku_player_power_position%}, §18 L847).
     */
    int posicionDelJugador(UUID jugador);

    /** @return título del puesto (§11 L541-548); cadena vacía o de error si el puesto no existe — lo decide B. */
    String tituloDe(int puesto);

    /**
     * Asigna un puesto. <b>Idempotente</b>: transacción
     * {@code UPDATE world_powers … WHERE position=? AND (owner_uuid IS NULL OR revision=?)} —
     * si afecta 0 filas, otro proceso ganó y se aborta sin duplicar (spec §11 L575, plan §6 regla 5).
     * @param actorAdmin UUID del admin que lo ordena, o {@code null} si es sistema/importación.
     * @return true si esta llamada fue la que asignó.
     */
    boolean asignar(int puesto, UUID jugador, String motivo, UUID actorAdmin);

    /**
     * Libera un puesto (mismo esquema idempotente de revisión).
     * @param actorAdmin UUID del admin o {@code null} (sistema).
     * @return true si esta llamada fue la que liberó.
     */
    boolean liberar(int puesto, String motivo, UUID actorAdmin);

    /**
     * @return bajas PvP válidas del jugador — placeholder
     *         {@code %mushoku_player_valid_kills%} (§18 L851) y dato de {@code /mtestado}.
     */
    int bajasValidas(UUID jugador);
}
