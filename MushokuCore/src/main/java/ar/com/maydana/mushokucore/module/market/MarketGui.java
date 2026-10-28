package ar.com.maydana.mushokucore.module.market;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.module.commands.Textos;
import ar.com.maydana.mushokucore.util.Componentes;
import ar.com.maydana.mushokucore.util.InventarioUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Arma y maneja las dos pantallas del mercado: menu de categorias y lista de ofertas.
 * Todos los clicks sobre el inventario del mercado se cancelan en {@link MarketListener};
 * aca solo se decide que hacer con el slot.
 */
public final class MarketGui {

    private static final int TAM_PRINCIPAL = 27;
    private static final int TAM_CATEGORIA = 54;
    private static final int SLOT_CERRAR = 22;
    private static final int SLOT_VOLVER = 49;
    private static final int MAX_OFERTAS = 45;

    private final PluginContext ctx;
    private final MarketService servicio;

    public MarketGui(PluginContext ctx, MarketService servicio) {
        this.ctx = ctx;
        this.servicio = servicio;
    }

    public void abrirPrincipal(Player jugador) {
        MarketHolder holder = new MarketHolder(null);
        Inventory inventario = Bukkit.createInventory(holder, TAM_PRINCIPAL,
                Componentes.de(servicio.titulo()));
        holder.enlazar(inventario);
        rellenar(inventario);

        ItemStack info = new ItemStack(Material.DIAMOND);
        ItemMeta metaInfo = info.getItemMeta();
        metaInfo.displayName(Componentes.de("&bDiamantes en inventario"));
        metaInfo.lore(Componentes.lista(List.of(
                "&7El mercado cobra diamantes fisicos.",
                "&7Tenés: &f" + InventarioUtil.contar(jugador, Material.DIAMOND))));
        info.setItemMeta(metaInfo);
        inventario.setItem(4, info);

        for (MarketCategory categoria : servicio.categorias()) {
            inventario.setItem(categoria.slot(), icono(categoria));
        }

        ItemStack cerrar = new ItemStack(Material.BARRIER);
        ItemMeta metaCerrar = cerrar.getItemMeta();
        metaCerrar.displayName(Componentes.de("&cCerrar"));
        cerrar.setItemMeta(metaCerrar);
        inventario.setItem(SLOT_CERRAR, cerrar);

        jugador.openInventory(inventario);
    }

    public void abrirCategoria(Player jugador, MarketCategory categoria) {
        MarketHolder holder = new MarketHolder(categoria.id());
        Inventory inventario = Bukkit.createInventory(holder, TAM_CATEGORIA,
                Componentes.de(categoria.titulo()));
        holder.enlazar(inventario);
        rellenar(inventario);

        int diamantes = InventarioUtil.contar(jugador, Material.DIAMOND);
        List<MarketItem> ofertas = categoria.items();
        int limite = Math.min(ofertas.size(), MAX_OFERTAS);
        for (int indice = 0; indice < limite; indice++) {
            inventario.setItem(indice, mostrar(ofertas.get(indice), diamantes));
        }
        if (ofertas.size() > MAX_OFERTAS) {
            ctx.plugin().getLogger().warning("market.yml: la categoria '" + categoria.id()
                    + "' tiene " + ofertas.size() + " items y solo se muestran " + MAX_OFERTAS
                    + ". Dividila en mas categorias.");
        }

        ItemStack volver = new ItemStack(Material.ARROW);
        ItemMeta metaVolver = volver.getItemMeta();
        metaVolver.displayName(Componentes.de("&eVolver al mercado"));
        volver.setItemMeta(metaVolver);
        inventario.setItem(SLOT_VOLVER, volver);

        jugador.openInventory(inventario);
    }

    /** Procesa un click sobre el inventario superior del mercado. */
    public void manejarClick(Player jugador, Inventory superior, int slot) {
        if (!(superior.getHolder() instanceof MarketHolder holder)) {
            return;
        }
        if (holder.esPrincipal()) {
            MarketCategory categoria = servicio.categoriaPorSlot(slot);
            if (categoria != null) {
                abrirCategoria(jugador, categoria);
            } else if (slot == SLOT_CERRAR) {
                jugador.closeInventory();
            }
            return;
        }
        if (slot == SLOT_VOLVER) {
            abrirPrincipal(jugador);
            return;
        }
        MarketItem item = servicio.itemPorSlot(holder.categoryId(), slot);
        if (item == null) {
            return;
        }
        MarketService.ResultadoCompra resultado = servicio.comprar(jugador, item);
        if (resultado == MarketService.ResultadoCompra.OK) {
            avisar(jugador, resultado.claveMensaje(), Map.of(
                    "cantidad", String.valueOf(item.amount()),
                    "item", item.nombreVisible(),
                    "precio", String.valueOf(item.priceDiamonds())));
            MarketCategory categoria = servicio.categoria(holder.categoryId());
            if (categoria != null) {
                abrirCategoria(jugador, categoria);
            }
            return;
        }
        avisar(jugador, resultado.claveMensaje(), Map.of(
                "precio", String.valueOf(item.priceDiamonds()),
                "tienes", String.valueOf(InventarioUtil.contar(jugador, Material.DIAMOND))));
    }

    // ------------------------------------------------------------------ helpers

    private static ItemStack icono(MarketCategory categoria) {
        ItemStack item = new ItemStack(categoria.icono());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Componentes.de(categoria.titulo()));
        List<String> lore = new ArrayList<>();
        lore.add("&7Ofertas: &f" + categoria.items().size());
        lore.add("&aClick para abrir");
        meta.lore(Componentes.lista(lore));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack mostrar(MarketItem oferta, int diamantes) {
        int visible = Math.max(1, Math.min(oferta.amount(), oferta.material().getMaxStackSize()));
        ItemStack item = new ItemStack(oferta.material(), visible);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Componentes.de(oferta.nombreVisible()));

        List<String> lore = new ArrayList<>(oferta.lore());
        if (!lore.isEmpty()) {
            lore.add("");
        }
        lore.add("&7Recibís: &f" + oferta.amount());
        lore.add("&7Precio: &b" + oferta.priceDiamonds() + " diamantes");
        if (diamantes >= oferta.priceDiamonds()) {
            lore.add("&aClick para comprar");
        } else {
            lore.add("&cTe faltan " + (oferta.priceDiamonds() - diamantes) + " diamantes");
        }
        meta.lore(Componentes.lista(lore));
        item.setItemMeta(meta);
        return item;
    }

    private static void rellenar(Inventory inventario) {
        ItemStack relleno = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = relleno.getItemMeta();
        meta.displayName(Componentes.de(" "));
        relleno.setItemMeta(meta);
        for (int indice = 0; indice < inventario.getSize(); indice++) {
            if (inventario.getItem(indice) == null) {
                inventario.setItem(indice, relleno);
            }
        }
    }

    private void avisar(Player jugador, String clave, Map<String, String> reemplazos) {
        Textos.enviar(jugador, Textos.prefijo(ctx) + Textos.msg(ctx, clave, reemplazos));
    }
}
