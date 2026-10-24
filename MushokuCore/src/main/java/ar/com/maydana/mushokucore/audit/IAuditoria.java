package ar.com.maydana.mushokucore.audit;

import java.util.UUID;

/**
 * CONTRATO CONGELADO — Fase 0 (agente F0), plan §6.1.
 * <p>Implementación: agente <b>A (infra-core)</b> en {@code AuditService.java} sobre la tabla
 * {@code audit_log} (spec §10 L525-532). Este interface no se edita sin actualizar el plan
 * (plan §6, regla 2).</p>
 *
 * <p>Toda operación que cambie datos debe auditar (spec §17 L829-836). El registro es
 * asíncrono y <b>jamás lanza excepciones al llamador</b>. {@code audit_log} no se borra al
 * vuelo: la retención (DECISION D17-D20, 2026-10-18) es de <b>7 días</b> — la tarea diaria
 * {@code AuditArchiveTask} archiva a CSV y recién entonces borra lo vencido.</p>
 */
public interface IAuditoria {

    /**
     * Registra una operación en {@code audit_log}.
     *
     * @param actor   UUID del jugador que la originó, o {@code null} cuando es consola/sistema.
     *                (El plan los marcaba con {@code @Nullable}; la anotación no existe en el
     *                classpath offline de Fase 0 y la nulabilidad se documenta en el javadoc.)
     * @param accion  identificador estable de la acción, p. ej. {@code "potencia.asignar"},
     *                {@code "enfermedad.aplicar"}, {@code "db.backup"}.
     * @param target  objetivo de la acción (UUID o etiqueta como {@code "puesto=5"}); puede ser {@code null}.
     * @param detalles detalle legible para un administrador; puede ser {@code null}.
     */
    void registrar(UUID actor, String accion, String target, String detalles);
}
