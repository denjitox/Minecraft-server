package ar.com.maydana.mushokucore.module.commands.stub;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;

/**
 * Stub de {@code /reliquia} — agente D. Las reliquias con PDC son 1.2 (spec §3 L117-124) y
 * mientras {@code modules.relics=false} las sigue entregando el datapack {@code mt_core}
 * (spec §21 L910-911): en MVP solo informa. DECISION D3: se mantiene en 1.0.
 */
public final class ReliquiaCommand extends StubCommand {

    public ReliquiaCommand(PluginContext ctx) {
        super(ctx, "relics");
    }
}
