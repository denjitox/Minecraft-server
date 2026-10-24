package ar.com.maydana.mushokucore.module.commands;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.config.PluginConfig;
import ar.com.maydana.mushokucore.integration.PlaceholderHook;
import ar.com.maydana.mushokucore.module.ModuleLifecycle;
import ar.com.maydana.mushokucore.module.PlaceholderFuente;
import ar.com.maydana.mushokucore.module.commands.stub.AlianzaCommand;
import ar.com.maydana.mushokucore.module.commands.stub.FaccionCommand;
import ar.com.maydana.mushokucore.module.commands.stub.ReliquiaCommand;
import ar.com.maydana.mushokucore.ui.JavaChatUi;
import ar.com.maydana.mushokucore.module.factions.FactionsService;

/**
 * Módulo de comandos y placeholders — propiedad transferida de F0 al agente D
 * (ui-comandos-import) en Fase 1 (plan §7, tabla §6.3). Va <b>último</b> en el orden de enable
 * (§6.3) para poder consumir {@code IPotenciasService} (B) e {@code IEnfermedadesService} (C)
 * desde el contexto.
 *
 * <p><b>Qué hace {@link #enable(PluginContext)} (implementado por D):</b></p>
 * <ul>
 *   <li>Habilita los comandos salvo que {@code config.enabled()} sea {@code false} (no existe la
 *       clave {@code modules.commands} en §9: nota para el integrador). Si el config aún no está
 *       provisto, igual registra — los comandos degradan solos (fallo seguro §2.5).</li>
 *   <li>{@code /mushoku} → {@link MushokuCommand} (executor + tab) — <b>pisa</b> el executor
 *       provisional que {@code PluginBootstrap} puso antes de habilitar módulos.</li>
 *   <li>{@code /mtestado} → {@link MtestadoCommand} sobre {@link JavaChatUi}.</li>
 *   <li>Stubs {@code /faccion}, {@code /alianza}, {@code /reliquia} (DECISION D3 2026-10-18:
 *       se mantienen en 1.0 como respuesta informativa).</li>
 *   <li>Publica las 9 fuentes del §18 ({@link PlaceholderFuenteMushoku#todas}) vía
 *       {@code ctx.registrarPlaceholderFuente(...)} — <b>sin SQL</b> (criterio §26/§22).</li>
 *   <li>Detecta PlaceholderAPI con {@link PlaceholderHook}; el registro de la expansión queda
 *       para el integrador ([A VERIFICAR — V3], ver el javadoc de {@code PlaceholderHook}).</li>
 * </ul>
 *
 * <p>{@link #disable()} no deshace registros de Bukkit (no hay API para "desregistrar" un
 * executor sin editar {@code plugin.yml}): el plugin entero se apaga en ese punto.</p>
 */
public final class CommandsModule implements ModuleLifecycle {

    @Override
    public String id() {
        return "commands";
    }

    @Override
    public void enable(PluginContext ctx) {
        PluginConfig config = Textos.opcional(ctx::config);
        if (config != null && !config.enabled()) {
            ctx.plugin().getLogger().info("Modulo commands: config.enabled=false; no se registran "
                    + "comandos ni placeholders.");
            return;
        }

        // ---- Comandos -----------------------------------------------------
        // /mushoku: la misma instancia es executor y tab-completer (pisa el provisional de Fase 0).
        MushokuCommand mushoku = new MushokuCommand(ctx);
        ctx.registrarComando("mushoku", mushoku, mushoku);

        // /mtestado sobre la UI de chat (consume B y C por el contexto; degrada si faltan).
        JavaChatUi ui = new JavaChatUi(ctx);
        ctx.registrarComando("mtestado", new MtestadoCommand(ctx, ui), null);

        if (ctx.obtener(FactionsService.class) == null) {
            ctx.registrarComando("faccion", new FaccionCommand(ctx), null);
            ctx.registrarComando("alianza", new AlianzaCommand(ctx), null);
        }
        ctx.registrarComando("reliquia", new ReliquiaCommand(ctx), null);

        // /visionapostol: visión nocturna de los Apóstoles (permiso mt.alianza.apostoles).
        ctx.registrarComando("visionapostol", new VisionApostolCommand(ctx), null);

        // ---- Placeholders §18 (solo caché, cero SQL) ----------------------
        for (PlaceholderFuente fuente : PlaceholderFuenteMushoku.todas(ctx)) {
            ctx.registrarPlaceholderFuente(fuente);
        }

        // ---- PlaceholderAPI (§7 L306, §18) --------------------------------
        new PlaceholderHook(ctx).registrar();
    }

    @Override
    public void disable() {
        // Los registros de Bukkit (executors, listeners, placeholders) no se deshacen por API;
        // el apagado del plugin los retira enteros (spec §8). Nada que liberar aquí.
    }
}
