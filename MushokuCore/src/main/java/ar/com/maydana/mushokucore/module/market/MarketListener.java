package ar.com.maydana.mushokucore.module.market;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/**
 * Bloquea toda mutacion dentro de los inventarios del mercado. Sin esto, un shift-click
 * podria mover items del jugador al cofre virtual y perderlos.
 */
public final class MarketListener implements Listener {

    private final MarketGui gui;

    public MarketListener(MarketGui gui) {
        this.gui = gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent evento) {
        Inventory superior = evento.getView().getTopInventory();
        if (!(superior.getHolder() instanceof MarketHolder)) {
            return;
        }
        evento.setCancelled(true);
        if (evento.getClickedInventory() == null
                || !evento.getClickedInventory().equals(superior)) {
            return;
        }
        if (evento.getWhoClicked() instanceof Player jugador) {
            gui.manejarClick(jugador, superior, evento.getSlot());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent evento) {
        if (evento.getView().getTopInventory().getHolder() instanceof MarketHolder) {
            evento.setCancelled(true);
        }
    }
}
