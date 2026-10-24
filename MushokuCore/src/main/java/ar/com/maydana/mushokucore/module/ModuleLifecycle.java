package ar.com.maydana.mushokucore.module;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;

/**
 * CONTRATO CONGELADO — Fase 0 (agente F0), plan §6.1 (spec §7 L322: cada módulo tiene
 * "método de inicio y cierre").
 * <p>El registro de listeners/comandos/tareas se hace desde {@link #enable(PluginContext)}
 * vía {@link PluginContext}; así el orden de enable/disable vive en un solo lugar
 * ({@code PluginBootstrap}/{@code ModuleRegistry}) y ningún agente toca archivos ajenos
 * (plan §6.3).</p>
 */
public interface ModuleLifecycle {

    /**
     * Identificador estable, coincide con la clave de {@code modules.*} de config.yml
     * (salvo "commands", que no tiene clave propia: se habilita si {@code enabled: true}).
     * Valores: {@code "ranks"} | {@code "diseases"} | {@code "commands"}.
     */
    String id();

    /**
     * Arranque del módulo: registrar listeners, comandos, tareas y publicar su servicio.
     * El módulo DEBE chequear su flag en {@code ctx.config()} y, si está apagado, no
     * registrar nada (spec §8 L351: informar módulos activos e inactivos).
     * @throws Exception si el módulo no puede iniciar; el {@code ModuleRegistry} la
     *                   registra y sigue con los demás (fallo seguro §2.5).
     */
    void enable(PluginContext ctx) throws Exception;

    /**
     * Cierre del módulo: cancelar tareas y volcar buffers (spec §8 L351-355;
     * flush inmediato si hay escrituras críticas pendientes, §19 L864).
     */
    void disable();
}
