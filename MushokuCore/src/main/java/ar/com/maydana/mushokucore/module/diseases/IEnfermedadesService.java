package ar.com.maydana.mushokucore.module.diseases;

import ar.com.maydana.mushokucore.model.Disease;
import java.util.Optional;
import java.util.UUID;

/**
 * CONTRATO CONGELADO — Fase 0 (agente F0) lo declara; <b>el agente C (enfermedades) lo
 * implementa</b> (p. ej. en {@code DiseasesService.java}) y lo publica en su {@code enable()}
 * con {@code ctx.registrarServicio(IEnfermedadesService.class, impl)}.
 * <p>Este archivo no se edita sin actualizar el plan §6.1 y avisar al integrador (regla 2).</p>
 *
 * <p>Consumidores: {@code /enfermedad} (C), {@code /mtestado} (D),
 * {@code %mushoku_player_disease%} (D). Reglas: spec §13 (catálogo L648-682,
 * comportamiento L684-693); tirada con reducción Orsted antes del roll (§12 L620),
 * chance 4 % por defecto, sin acumulación infinita, persistencia en {@code diseases}.</p>
 */
public interface IEnfermedadesService {

    /**
     * Enfermedad activa del jugador (para {@code /mtestado} y placeholders; lectura de caché
     * o de la fila persistida — sin SQL en hilo principal, §10 L449).
     * @return la enfermedad vigente; vacío si no tiene ninguna. [PROPUESTA F0] una sola
     *         activa a la vez: si C admite varias, se revisa este contrato en el plan.
     */
    Optional<Disease> activa(UUID jugador);

    /**
     * Aplica una enfermedad del catálogo ({@code diseases.yml}).
     * @param source origen declarado (p. ej. {@code "HUSK_DAMAGE"}, {@code "admin"}); puede ser null.
     * @return true si quedó aplicada; false si no aplica (enfermedad desconocida, módulo
     *         apagado, inmune, ya activa — §13: sin reinfección de la misma enfermedad).
     */
    boolean aplicar(UUID jugador, String diseaseId, String source);

    /**
     * Cura enfermedades.
     * @param diseaseId id específico, o {@code null} para <b>todas</b> (sintaxis
     *                  {@code /enfermedad curar <jugador> [id|todas]}, spec §17 L819).
     * @return cantidad de enfermedades curadas (0 si no había ninguna).
     */
    int curar(UUID jugador, String diseaseId);
}
