package ar.com.maydana.mushokucore.module.npcs;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Marca el inventario de trueques de un vecino. */
public final class NpcHolder implements InventoryHolder {

    private final String npcId;
    private Inventory inventory;

    public NpcHolder(String npcId) {
        this.npcId = npcId;
    }

    public String npcId() {
        return npcId;
    }

    void enlazar(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
