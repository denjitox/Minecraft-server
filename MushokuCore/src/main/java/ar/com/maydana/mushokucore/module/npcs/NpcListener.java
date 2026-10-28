package ar.com.maydana.mushokucore.module.npcs;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;

/**
 * Puente entre Bukkit y {@link NpcsService}: entidad (PDC), bloque ancla, GUI de
 * trueques, proteccion del NPC, precarga de reputacion y reconciliacion al cargar chunks.
 */
public final class NpcListener implements Listener {

    private final NpcsService servicio;

    public NpcListener(NpcsService servicio) {
        this.servicio = servicio;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent evento) {
        if (evento.getHand() != EquipmentSlot.HAND) {
            return;
        }
        String npcId = servicio.npcIdDe(evento.getRightClicked());
        if (npcId == null) {
            return;
        }
        evento.setCancelled(true);
        servicio.interactuar(evento.getPlayer(), npcId, evento.getRightClicked());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractBlock(PlayerInteractEvent evento) {
        if (evento.getAction() != Action.RIGHT_CLICK_BLOCK
                || evento.getHand() != EquipmentSlot.HAND
                || evento.getClickedBlock() == null) {
            return;
        }
        String npcId = servicio.npcEnBloque(evento.getClickedBlock().getLocation());
        if (npcId == null) {
            return;
        }
        evento.setCancelled(true);
        servicio.interactuar(evento.getPlayer(), npcId, null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent evento) {
        if (servicio.npcIdDe(evento.getEntity()) != null) {
            evento.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evento) {
        Inventory superior = evento.getView().getTopInventory();
        if (!(superior.getHolder() instanceof NpcHolder)) {
            return;
        }
        evento.setCancelled(true);
        if (evento.getClickedInventory() == null
                || !evento.getClickedInventory().equals(superior)) {
            return;
        }
        if (evento.getWhoClicked() instanceof Player jugador) {
            servicio.manejarClickGui(jugador, superior, evento.getSlot());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent evento) {
        if (evento.getView().getTopInventory().getHolder() instanceof NpcHolder) {
            evento.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evento) {
        servicio.precargarReputacion(evento.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent evento) {
        servicio.olvidarJugador(evento.getPlayer());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent evento) {
        servicio.reconciliar(evento.getChunk());
    }
}
