package ar.com.maydana.mushokucore.module.market;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.module.commands.Textos;
import ar.com.maydana.mushokucore.util.InventarioUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * {@code /mercado} — abre la GUI o ejecuta una compra sin GUI (util desde Bedrock y
 * desde consola con un jugador como objetivo).
 *
 * <pre>
 *   /mercado                       abre el menu (filtrado por bioma/rotacion)
 *   /mercado &lt;categoria&gt;           abre una categoria
 *   /mercado comprar &lt;item&gt;        compra directa (si esta disponible)
 *   /mercado lista [categoria]     lista categorias/ofertas disponibles
 *   /mercado recargar              relee market.yml (admin)
 * </pre>
 */
public final class MercadoCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISO_USO = "mushoku.mercado.use";
    private static final String PERMISO_ADMIN = "mushoku.admin.mercado";

    private final PluginContext ctx;
    private final MarketService servicio;
    private final MarketGui gui;

    public MercadoCommand(PluginContext ctx, MarketService servicio, MarketGui gui) {
        this.ctx = ctx;
        this.servicio = servicio;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender emisor, Command comando, String etiqueta, String[] args) {
        if (args.length == 0) {
            if (!(emisor instanceof Player jugador)) {
                Textos.msg(ctx, emisor, "mercado.solo-jugador");
                return true;
            }
            if (!tienePermiso(emisor, PERMISO_USO)) {
                Textos.msg(ctx, emisor, "generic.no-permission");
                return true;
            }
            gui.abrirPrincipal(jugador);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "comprar", "buy" -> {
                if (!(emisor instanceof Player jugador)) {
                    Textos.msg(ctx, emisor, "mercado.solo-jugador");
                    return true;
                }
                if (!tienePermiso(emisor, PERMISO_USO)) {
                    Textos.msg(ctx, emisor, "generic.no-permission");
                    return true;
                }
                if (args.length < 2) {
                    Textos.msg(ctx, emisor, "mercado.uso-comprar");
                    return true;
                }
                comprarDirecto(jugador, args[1]);
                return true;
            }
            case "lista", "list" -> {
                listar(emisor, args.length >= 2 ? args[1] : null);
                return true;
            }
            case "recargar", "reload" -> {
                if (!tienePermiso(emisor, PERMISO_ADMIN)) {
                    Textos.msg(ctx, emisor, "generic.no-permission");
                    return true;
                }
                servicio.recargar();
                Textos.msg(ctx, emisor, "mercado.recargado",
                        Map.of("categorias", String.valueOf(servicio.categorias().size())));
                return true;
            }
            default -> {
                if (!(emisor instanceof Player jugador)) {
                    Textos.msg(ctx, emisor, "mercado.solo-jugador");
                    return true;
                }
                if (!tienePermiso(emisor, PERMISO_USO)) {
                    Textos.msg(ctx, emisor, "generic.no-permission");
                    return true;
                }
                if (servicio.categoria(sub) != null) {
                    gui.abrirCategoria(jugador, sub);
                } else {
                    Textos.msg(ctx, emisor, "mercado.uso");
                }
                return true;
            }
        }
    }

    private void comprarDirecto(Player jugador, String itemId) {
        MarketItem item = servicio.item(itemId);
        if (item == null) {
            Textos.msg(ctx, jugador, "mercado.item-invalido", Map.of("item", itemId));
            return;
        }
        MarketService.ResultadoCompra resultado = servicio.comprar(jugador, itemId);
        if (resultado == MarketService.ResultadoCompra.OK) {
            Textos.msg(ctx, jugador, resultado.claveMensaje(), Map.of(
                    "item", item.nombreVisible(),
                    "cantidad", String.valueOf(item.amount()),
                    "precio", String.valueOf(item.priceDiamonds())));
        } else {
            Textos.msg(ctx, jugador, resultado.claveMensaje(), Map.of(
                    "item", item.nombreVisible(),
                    "precio", String.valueOf(item.priceDiamonds()),
                    "bioma", nombreBioma(servicio.biomeActual(jugador))));
        }
    }

    private void listar(CommandSender emisor, String categoriaId) {
        Player jugador = emisor instanceof Player p ? p : null;
        if (categoriaId != null) {
            MarketCategory cat = servicio.categoria(categoriaId);
            if (cat == null) {
                Textos.msg(ctx, emisor, "mercado.categoria-invalida", Map.of("categoria", categoriaId));
                return;
            }
            List<MarketItem> items = jugador != null ? servicio.itemsDisponibles(cat, jugador) : cat.items();
            Textos.msg(ctx, emisor, "mercado.lista-categoria", Map.of(
                    "categoria", cat.titulo(),
                    "cantidad", String.valueOf(items.size())));
            for (MarketItem item : items) {
                String extra = sufijoDisponibilidad(item);
                emisor.sendMessage(Textos.color("&7- &f" + item.id() + " &8(" + item.amount() + "x "
                        + item.material().name().toLowerCase(Locale.ROOT) + " = &b" + item.priceDiamonds() + " diamante(s)&8)" + extra));
            }
            if (items.isEmpty()) {
                emisor.sendMessage(Textos.color("&7  (sin ofertas disponibles ahora)"));
            }
            return;
        }

        Textos.msg(ctx, emisor, "mercado.lista", Map.of(
                "categorias", String.valueOf(servicio.categorias().size())));
        for (MarketCategory cat : servicio.categorias()) {
            int n = jugador != null ? servicio.itemsDisponibles(cat, jugador).size() : cat.items().size();
            String suf = jugador != null ? " &7(disponibles: &f" + n + "&7)" : " &7(&f" + n + "&7 items)";
            emisor.sendMessage(Textos.color("&7- &f" + cat.id() + " &8" + cat.titulo() + suf));
        }
        if (jugador != null) {
            emisor.sendMessage(Textos.color("&7Bioma actual: &f" + nombreBioma(servicio.biomeActual(jugador))));
        }
    }

    private String sufijoDisponibilidad(MarketItem item) {
        List<String> tags = new ArrayList<>();
        if (item.tieneFiltroBioma()) {
            tags.add("biomas: " + item.biomes().stream().limit(3).collect(Collectors.joining(","))
                    + (item.biomes().size() > 3 ? "..." : ""));
        }
        if (item.tieneRotacion()) {
            MarketRotation r = item.rotation();
            String p = r.period() == MarketRotation.Period.WEEKLY ? "sem" : "dia";
            tags.add(r.every() <= 1 ? "rot:siempre" : "rot:" + p + "/1de" + r.every());
        }
        if (tags.isEmpty()) return "";
        return Textos.color(" &8[" + String.join(", ", tags) + "]");
    }

    private String nombreBioma(String clave) {
        if (clave == null || clave.isBlank()) return "desconocido";
        String base = clave.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }

    private boolean tienePermiso(CommandSender emisor, String permiso) {
        return emisor.hasPermission(permiso) || emisor.hasPermission("mushoku.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender emisor, Command comando, String alias, String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<>(List.of("comprar", "lista", "recargar"));
            if (emisor instanceof Player p) {
                for (MarketCategory cat : servicio.categoriasDisponibles(p)) {
                    base.add(cat.id());
                }
            } else {
                for (MarketCategory cat : servicio.categorias()) {
                    base.add(cat.id());
                }
            }
            return filtrar(base, args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals("comprar") || sub.equals("buy")) {
                List<String> ids = new ArrayList<>();
                if (emisor instanceof Player p) {
                    for (MarketCategory cat : servicio.categorias()) {
                        for (MarketItem it : servicio.itemsDisponibles(cat, p)) ids.add(it.id());
                    }
                } else {
                    for (MarketCategory cat : servicio.categorias()) {
                        for (MarketItem it : cat.items()) ids.add(it.id());
                    }
                }
                return filtrar(ids, args[1]);
            }
            if (sub.equals("lista") || sub.equals("list")) {
                List<String> ids = new ArrayList<>();
                for (MarketCategory cat : servicio.categorias()) ids.add(cat.id());
                return filtrar(ids, args[1]);
            }
        }
        return List.of();
    }

    private List<String> filtrar(List<String> opciones, String prefijo) {
        String p = prefijo == null ? "" : prefijo.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : opciones) {
            if (o.toLowerCase(Locale.ROOT).startsWith(p)) out.add(o);
        }
        return out;
    }
}
