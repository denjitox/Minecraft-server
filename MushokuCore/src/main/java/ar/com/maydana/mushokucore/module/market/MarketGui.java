package ar.com.maydana.mushokucore.module.market;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.module.commands.Textos;
import ar.com.maydana.mushokucore.util.Componentes;
import ar.com.maydana.mushokucore.util.InventarioUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
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
 *
 * <p>Las ofertas se filtran por disponibilidad (bioma/rotacion) para el jugador que abre
 * la GUI; el click de compra vuelve a validar por si cambio de bioma o de periodo.</p>
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
        if (metaInfo != null) {
            metaInfo.displayName(Componentes.de("&b&lTus diamantes: &f" + InventarioUtil.contar(jugador, Material.DIAMOND)));
            metaInfo.lore(List.of(
                    Componentes.de("&7Bioma: &f" + nombreBioma(servicio.biomeActual(jugador))),
                    Componentes.de("&7Las ofertas dependen de tu bioma y de la rotacion.")));
            info.setItemMeta(metaInfo);
        }
        inventario.setItem(4, info);

        for (MarketCategory categoria : servicio.categoriasDisponibles(jugador)) {
            int slot = categoria.slot();
            if (slot < 0 || slot >= TAM_PRINCIPAL) {
                continue;
            }
            ItemStack icono = new ItemStack(categoria.icono() == null ? Material.CHEST : categoria.icono());
            ItemMeta meta = icono.getItemMeta();
            if (meta != null) {
                meta.displayName(Componentes.de(categoria.titulo()));
                int disp = servicio.itemsDisponibles(categoria, jugador).size();
                List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
                lore.add(Componentes.de("&7" + disp + " oferta(s) disponible(s) aqui"));
                lore.add(Componentes.de("&eClick para abrir"));
                meta.lore(lore);
                icono.setItemMeta(meta);
            }
            inventario.setItem(slot, icono);
        }

        ItemStack cerrar = boton(Material.BARRIER, "&cCerrar");
        inventario.setItem(SLOT_CERRAR, cerrar);

        jugador.openInventory(inventario);
    }

    public void abrirCategoria(Player jugador, String categoriaId) {
        MarketCategory categoria = servicio.categoria(categoriaId);
        if (categoria == null) {
            Textos.msg(ctx, jugador, "mercado.categoria-invalida", Map.of("categoria", String.valueOf(categoriaId)));
            return;
        }
        List<MarketItem> disponibles = servicio.itemsDisponibles(categoria, jugador);
        MarketHolder holder = new MarketHolder(categoria.id());
        Inventory inventario = Bukkit.createInventory(holder, TAM_CATEGORIA,
                Componentes.de(categoria.titulo()));
        holder.enlazar(inventario);
        rellenar(inventario);

        int slot = 0;
        for (MarketItem item : disponibles) {
            if (slot >= MAX_OFERTAS) {
                break;
            }
            inventario.setItem(slot++, iconoItem(item, jugador));
        }
        if (disponibles.isEmpty()) {
            ItemStack vacio = boton(Material.PAPER, "&7Sin ofertas disponibles ahora");
            ItemMeta mv = vacio.getItemMeta();
            if (mv != null) {
                mv.lore(List.of(
                        Componentes.de("&7Cambia de bioma o espera la rotacion."),
                        Componentes.de("&7Bioma actual: &f" + nombreBioma(servicio.biomeActual(jugador)))));
                vacio.setItemMeta(mv);
            }
            inventario.setItem(22, vacio);
        }

        inventario.setItem(SLOT_VOLVER, boton(Material.ARROW, "&eVolver"));
        inventario.setItem(53, boton(Material.BARRIER, "&cCerrar"));
        jugador.openInventory(inventario);
    }

    private ItemStack iconoItem(MarketItem item, Player jugador) {
        ItemStack stack = new ItemStack(item.material());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Componentes.de(item.nombreVisible()));
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            lore.add(Componentes.de("&7Cantidad: &f" + item.amount()));
            lore.add(Componentes.de("&7Precio: &b" + item.priceDiamonds() + " diamante(s)"));
            lore.add(Componentes.de("&7ID: &8" + item.id()));
            if (item.tieneFiltroBioma()) {
                String bs = item.biomes().stream().map(this::nombreBioma).collect(Collectors.joining(", "));
                lore.add(Componentes.de("&7Biomas: &d" + bs));
            }
            if (item.tieneRotacion()) {
                MarketRotation r = item.rotation();
                String periodo = r.period() == MarketRotation.Period.WEEKLY ? "semanal" : "diaria";
                if (r.every() <= 1) {
                    lore.add(Componentes.de("&7Rotacion: &asiempre"));
                } else {
                    lore.add(Componentes.de("&7Rotacion: &a" + periodo + " (1 de cada " + r.every() + ")"));
                }
            }
            lore.add(Componentes.de(""));
            lore.add(Componentes.de("&eClick para comprar"));
            if (item.lore() != null && !item.lore().isEmpty()) {
                lore.add(Componentes.de("&8---"));
                for (String linea : item.lore()) {
                    lore.add(Componentes.de(linea));
                }
            }
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public void manejarClick(Player jugador, Inventory inventario, int slot) {
        if (!(inventario.getHolder() instanceof MarketHolder holder)) {
            return;
        }
        if (holder.esPrincipal()) {
            if (slot == SLOT_CERRAR) {
                jugador.closeInventory();
                return;
            }
            // Buscar categoria por slot visible (el loader ya garantiza slots unicos)
            for (MarketCategory categoria : servicio.categoriasDisponibles(jugador)) {
                if (categoria.slot() == slot) {
                    abrirCategoria(jugador, categoria.id());
                    return;
                }
            }
            return;
        }

        if (slot == SLOT_VOLVER) {
            abrirPrincipal(jugador);
            return;
        }
        if (slot == 53) {
            jugador.closeInventory();
            return;
        }
        if (slot < 0 || slot >= MAX_OFERTAS) {
            return;
        }
        MarketCategory categoria = servicio.categoria(holder.categoryId());
        if (categoria == null) {
            return;
        }
        List<MarketItem> disponibles = servicio.itemsDisponibles(categoria, jugador);
        if (slot >= disponibles.size()) {
            return;
        }
        MarketItem item = disponibles.get(slot);
        MarketService.ResultadoCompra resultado = servicio.comprar(jugador, item.id());
        if (resultado == MarketService.ResultadoCompra.OK) {
            avisar(jugador, resultado.claveMensaje(), Map.of(
                    "item", item.nombreVisible(),
                    "cantidad", String.valueOf(item.amount()),
                    "precio", String.valueOf(item.priceDiamonds())));
            abrirCategoria(jugador, categoria.id()); // refresca diamantes y disponibilidad
        } else {
            avisar(jugador, resultado.claveMensaje(), Map.of(
                    "item", item.nombreVisible(),
                    "precio", String.valueOf(item.priceDiamonds()),
                    "bioma", nombreBioma(servicio.biomeActual(jugador))));
        }
    }

    private void avisar(Player jugador, String clave, Map<String, String> vars) {
        Textos.msg(ctx, jugador, clave, vars);
    }

    private void rellenar(Inventory inventario) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Componentes.de(" "));
            filler.setItemMeta(meta);
        }
        for (int i = 0; i < inventario.getSize(); i++) {
            inventario.setItem(i, filler);
        }
    }

    private ItemStack boton(Material material, String nombre) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Componentes.de(nombre));
            item.setItemMeta(meta);
        }
        return item;
    }

    private String nombreBioma(String clave) {
        if (clave == null || clave.isBlank()) {
            return "desconocido";
        }
        String base = clave.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }
}
