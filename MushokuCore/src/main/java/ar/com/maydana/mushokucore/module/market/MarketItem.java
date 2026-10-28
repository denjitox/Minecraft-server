package ar.com.maydana.mushokucore.module.market;

import java.util.List;
import java.util.Locale;
import org.bukkit.Material;

/**
 * Una oferta del mercado: se entrega {@code amount} del material a cambio de
 * {@code priceDiamonds} diamantes fisicos.
 *
 * <p>El precio es por la oferta completa, no por unidad. Los campos de presentacion
 * ({@code nombre}, {@code lore}) son opcionales: si faltan se usa el nombre del material.</p>
 */
public record MarketItem(String id, Material material, int amount, int priceDiamonds,
                         String nombre, List<String> lore) {

    public MarketItem {
        lore = lore == null ? List.of() : List.copyOf(lore);
    }

    public boolean valido() {
        return material != null && material.isItem() && amount > 0 && priceDiamonds > 0
                && amount <= Math.max(1, material.getMaxStackSize());
    }

    /** Nombre para mostrar: el del YAML si existe, si no el del material en minusculas. */
    public String nombreVisible() {
        if (nombre != null && !nombre.isBlank()) {
            return nombre;
        }
        String base = material == null ? "?" : material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return "&f" + Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }
}
