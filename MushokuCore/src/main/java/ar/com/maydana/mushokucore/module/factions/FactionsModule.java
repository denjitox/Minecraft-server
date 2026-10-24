package ar.com.maydana.mushokucore.module.factions;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.database.repository.TownAllegianceRepository;
import ar.com.maydana.mushokucore.integration.TownyHook;
import ar.com.maydana.mushokucore.module.ModuleLifecycle;

public final class FactionsModule implements ModuleLifecycle {

    private FactionsService service;

    @Override
    public String id() {
        return "factions";
    }

    @Override
    public void enable(PluginContext ctx) {
        if (!ctx.config().moduloFactions()) {
            return;
        }
        TownyHook towny = ctx.towny();
        if (!towny.isPresent()) {
            throw new IllegalStateException("modules.factions=true pero Towny no está habilitado.");
        }
        service = new FactionsService(ctx.plugin(), ctx.config(), towny,
                new TownAllegianceRepository(ctx.db()), ctx.audit(), ctx.plugin().getLogger());
        ctx.registrarServicio(FactionsService.class, service);
        ctx.registrarListener(new FactionClaimsListener(service));
        ctx.registrarComando("faccion", new FaccionCommand(service, ctx.messages()), null);
        AlianzaCommand alliance = new AlianzaCommand(service, ctx.messages());
        ctx.registrarComando("alianza", alliance, alliance);
        service.start();
    }

    @Override
    public void disable() {
        if (service != null) {
            service.stop();
            service = null;
        }
    }
}
