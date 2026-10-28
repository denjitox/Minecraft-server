package ar.com.maydana.mushokucore.module.market;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Marca los inventarios del mercado para que el listener sepa que tiene que cancelar
 * los clicks. {@code categoryId == null} identifica el menu principal.
 */
public final class MarketHolder implements InventoryHolder {

    private final String categoryId;
    private Inventory inventory;

    public MarketHolder(String categoryId) {
        this.categoryId = categoryId;
    }

    public String categoryId() {
        return categoryId;
    }

    public boolean esPrincipal() {
        return categoryId == null;
    }

    void enlazar(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
