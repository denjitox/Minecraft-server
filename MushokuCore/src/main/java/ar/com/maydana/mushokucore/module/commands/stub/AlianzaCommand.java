package ar.com.maydana.mushokucore.module.commands.stub;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;

/**
 * Stub de {@code /alianza} — agente D. Las alianzas son 1.1 (spec §3 L107-115) y hoy las
 * sigue manejando el Skript {@code mt.alianza.*} (spec §21 L893): en MVP solo informa.
 * DECISION D3: se mantiene en 1.0.
 */
public final class AlianzaCommand extends StubCommand {

    public AlianzaCommand(PluginContext ctx) {
        super(ctx, "factions");
    }
}
