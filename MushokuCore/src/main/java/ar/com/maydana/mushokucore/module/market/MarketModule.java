package ar.com.maydana.mushokucore.module.market;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.database.repository.MarketRepository;
import ar.com.maydana.mushokucore.module.ModuleLifecycle;

/**
 * Modulo de economia por diamantes: mercado de administracion con precios en
 * {@code market.yml}. No usa Vault ni saldo virtual; cada compra se registra en
 * {@code market_purchases} (migracion V003).
 */
public final class MarketModule implements ModuleLifecycle {

    private MarketService servicio;

    @Override
    public String id() {
        return "market";
    }

    @Override
    public void enable(PluginContext ctx) throws Exception {
        if (!ctx.config().moduloMarket()) {
            ctx.plugin().getLogger().info("Modulo 'market' apagado en config.yml "
                    + "(modules.market=false): no se registra nada.");
            return;
        }
        servicio = new MarketService(ctx.plugin(), new MarketRepository(ctx.db()), ctx.audit(),
                ctx.plugin().getLogger(), ctx.config().mercadoMaxComprasPorMinuto());
        int items = servicio.cargarCatalogo();
        MarketGui gui = new MarketGui(ctx, servicio);
        ctx.registrarListener(new MarketListener(gui));
        MercadoCommand comando = new MercadoCommand(ctx, servicio, gui);
        ctx.registrarComando("mercado", comando, comando);
        if (items == 0) {
            ctx.plugin().getLogger().warning("Modulo 'market' habilitado sin ofertas: "
                    + "revisar market.yml antes de abrir el menu.");
        } else {
            ctx.plugin().getLogger().info("Modulo 'market' habilitado: "
                    + servicio.categorias().size() + " categoria(s), " + items + " item(s).");
        }
    }

    @Override
    public void disable() {
        servicio = null;
    }
}
