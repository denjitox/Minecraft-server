package ar.com.maydana.mushokucore.module.commands.stub;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;

/**
 * Stub de {@code /faccion} — agente D. Módulo {@code factions} es 1.1 (spec §3 L107-115);
 * en MVP solo informa ({@code messages.yml → stub.module-future}). DECISION D3: se mantiene en 1.0.
 */
public final class FaccionCommand extends StubCommand {

    public FaccionCommand(PluginContext ctx) {
        super(ctx, "factions");
    }
}
