package ar.com.maydana.mushokucore.module.npcs;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.module.commands.Textos;
import ar.com.maydana.mushokucore.util.Componentes;
import ar.com.maydana.mushokucore.util.InventarioUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Pantalla de trueques. Se abre con shift-click sobre el vecino (o con
 * {@code /vecino trueques <id>} en Bedrock) para no mezclar comercio con el dialogo.
 */
public final class NpcGui {

    private static final int TAM = 27;

    private final PluginContext ctx;
    private final NpcsService servicio;

    public NpcGui(PluginContext ctx, NpcsService servicio) {
        this.ctx = ctx;
        this.servicio = servicio;
    }

    public void abrir(Player jugador, NpcDefinition definicion) {
        if (definicion.trueques().isEmpty()) {
            avisar(jugador, "vecinos.trueque-vacio", Map.of("npc", definicion.nombre()));
            return;
        }
        NpcHolder holder = new NpcHolder(definicion.id());
        Inventory inventario = Bukkit.createInventory(holder, TAM,
                Componentes.de(definicion.nombre()));
        holder.enlazar(inventario);

        ItemStack relleno = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta metaRelleno = relleno.getItemMeta();
        metaRelleno.displayName(Componentes.de(" "));
        relleno.setItemMeta(metaRelleno);
        for (int indice = 0; indice < TAM; indice++) {
            inventario.setItem(indice, relleno);
        }

        List<NpcDefinition.Trueque> trueques = definicion.trueques();
        int limite = Math.min(trueques.size(), TAM);
        for (int indice = 0; indice < limite; indice++) {
            inventario.setItem(indice, mostrar(jugador, trueques.get(indice)));
        }
        if (trueques.size() > TAM) {
            ctx.plugin().getLogger().warning("npcs.yml: '" + definicion.id() + "' tiene "
                    + trueques.size() + " trueques y solo se muestran " + TAM + ".");
        }
        jugador.openInventory(inventario);
    }

    public void manejarClick(Player jugador, Inventory superior, int slot) {
        if (!(superior.getHolder() instanceof NpcHolder holder)) {
            return;
        }
        NpcDefinition definicion = servicio.definicion(holder.npcId());
        if (definicion == null) {
            jugador.closeInventory();
            return;
        }
        if (servicio.trueque(jugador, definicion, slot)) {
            abrir(jugador, definicion);
        }
    }

    private static ItemStack mostrar(Player jugador, NpcDefinition.Trueque trueque) {
        int visible = Math.max(1, Math.min(trueque.entregaCantidad(),
                trueque.entrega().getMaxStackSize()));
        ItemStack item = new ItemStack(trueque.entrega(), visible);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Componentes.de(nombre(trueque.entrega())));
        List<String> lore = new ArrayList<>();
        lore.add("&7Entregás: &f" + trueque.costoCantidad() + "x "
                + nombrePlano(trueque.costo()));
        lore.add("&7Recibís: &f" + trueque.entregaCantidad() + "x "
                + nombrePlano(trueque.entrega()));
        int tiene = InventarioUtil.contar(jugador, trueque.costo());
        if (tiene >= trueque.costoCantidad()) {
            lore.add("&aClick para intercambiar");
        } else {
            lore.add("&cTe falta " + (trueque.costoCantidad() - tiene) + "x "
                    + nombrePlano(trueque.costo()));
        }
        meta.lore(Componentes.lista(lore));
        item.setItemMeta(meta);
        return item;
    }

    private static String nombre(Material material) {
        return "&f" + nombrePlano(material);
    }

    private static String nombrePlano(Material material) {
        String base = material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }

    private void avisar(Player jugador, String clave, Map<String, String> reemplazos) {
        Textos.enviar(jugador, Textos.prefijo(ctx) + Textos.msg(ctx, clave, reemplazos));
    }
}
