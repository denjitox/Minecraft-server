package ar.com.maydana.mushokucore.module.market;

import java.util.List;
import org.bukkit.Material;

/**
 * Categoria del mercado. {@code slot} es la posicion del icono en el menu principal
 * (0..26); el loader la corrige si el YAML trae un valor fuera de rango o repetido.
 */
public record MarketCategory(String id, String titulo, Material icono, int slot,
                             List<MarketItem> items) {

    public MarketCategory {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public MarketItem item(String itemId) {
        if (itemId == null) {
            return null;
        }
        for (MarketItem item : items) {
            if (item.id().equalsIgnoreCase(itemId)) {
                return item;
            }
        }
        return null;
    }
}
