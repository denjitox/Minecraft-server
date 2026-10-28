package ar.com.maydana.mushokucore.module.market;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.module.commands.Textos;
import ar.com.maydana.mushokucore.util.InventarioUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
 *   /mercado                       abre el menu
 *   /mercado &lt;categoria&gt;           abre una categoria
 *   /mercado comprar &lt;item&gt;        compra directa
 *   /mercado lista                 lista categorias e items
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
        if (!emisor.hasPermission(PERMISO_USO)) {
            avisar(emisor, "generic.no-permission", Map.of());
            return true;
        }
        if (args.length == 0) {
            if (!(emisor instanceof Player jugador)) {
                avisar(emisor, "mercado.solo-jugadores", Map.of());
                return true;
            }
            if (servicio.sinOfertas()) {
                avisar(emisor, "mercado.vacio", Map.of());
                return true;
            }
            gui.abrirPrincipal(jugador);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "recargar" -> {
                if (!emisor.hasPermission(PERMISO_ADMIN)) {
                    avisar(emisor, "generic.no-permission", Map.of());
                    return true;
                }
                int items = servicio.cargarCatalogo();
                avisar(emisor, "mercado.recargado", Map.of(
                        "categorias", String.valueOf(servicio.categorias().size()),
                        "items", String.valueOf(items)));
                return true;
            }
            case "lista" -> {
                listar(emisor);
                return true;
            }
            case "comprar" -> {
                comprar(emisor, args);
                return true;
            }
            default -> {
                if (!(emisor instanceof Player jugador)) {
                    avisar(emisor, "mercado.solo-jugadores", Map.of());
                    return true;
                }
                MarketCategory categoria = servicio.categoria(sub);
                if (categoria == null) {
                    avisar(emisor, "mercado.categoria-desconocida", Map.of("categoria", args[0]));
                    return true;
                }
                gui.abrirCategoria(jugador, categoria);
                return true;
            }
        }
    }

    private void comprar(CommandSender emisor, String[] args) {
        if (!(emisor instanceof Player jugador)) {
            avisar(emisor, "mercado.solo-jugadores", Map.of());
            return;
        }
        if (args.length < 2) {
            avisar(emisor, "mercado.uso-comprar", Map.of());
            return;
        }
        MarketItem item = servicio.buscarItem(args[1]);
        if (item == null) {
            avisar(emisor, "mercado.desconocido", Map.of());
            return;
        }
        MarketService.ResultadoCompra resultado = servicio.comprar(jugador, item);
        avisar(emisor, resultado.claveMensaje(), Map.of(
                "cantidad", String.valueOf(item.amount()),
                "item", item.nombreVisible(),
                "precio", String.valueOf(item.priceDiamonds()),
                "tienes", String.valueOf(InventarioUtil.contar(jugador, Material.DIAMOND))));
    }

    private void listar(CommandSender emisor) {
        avisar(emisor, "mercado.lista-header", Map.of());
        if (servicio.sinOfertas()) {
            avisar(emisor, "mercado.vacio", Map.of());
            return;
        }
        for (MarketCategory categoria : servicio.categorias()) {
            avisar(emisor, "mercado.lista-categoria", Map.of(
                    "categoria", categoria.titulo(),
                    "items", String.valueOf(categoria.items().size())));
            for (MarketItem item : categoria.items()) {
                avisar(emisor, "mercado.lista-item", Map.of(
                        "item", item.id(),
                        "cantidad", String.valueOf(item.amount()),
                        "precio", String.valueOf(item.priceDiamonds())));
            }
        }
    }

    private void avisar(CommandSender emisor, String clave, Map<String, String> reemplazos) {
        String texto = reemplazos.isEmpty()
                ? Textos.msg(ctx, clave)
                : Textos.msg(ctx, clave, reemplazos);
        Textos.enviar(emisor, Textos.prefijo(ctx) + texto);
    }

    @Override
    public List<String> onTabComplete(CommandSender emisor, Command comando, String etiqueta,
                                      String[] args) {
        List<String> opciones = new ArrayList<>();
        if (args.length == 1) {
            if (emisor.hasPermission(PERMISO_ADMIN)) {
                opciones.add("recargar");
            }
            opciones.add("lista");
            opciones.add("comprar");
            for (MarketCategory categoria : servicio.categorias()) {
                opciones.add(categoria.id());
            }
            return filtrar(opciones, args[0]);
        }
        if (args.length == 2 && "comprar".equalsIgnoreCase(args[0])) {
            for (MarketCategory categoria : servicio.categorias()) {
                for (MarketItem item : categoria.items()) {
                    opciones.add(item.id());
                }
            }
            return filtrar(opciones, args[1]);
        }
        return List.of();
    }

    private static List<String> filtrar(List<String> opciones, String prefijo) {
        String inicio = prefijo.toLowerCase(Locale.ROOT);
        List<String> resultado = new ArrayList<>();
        for (String opcion : opciones) {
            if (opcion.toLowerCase(Locale.ROOT).startsWith(inicio)) {
                resultado.add(opcion);
            }
        }
        return resultado;
    }
}
