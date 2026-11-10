package ar.com.maydana.mushokucore.module.market;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.bukkit.Material;

/**
 * Una oferta del mercado: se entrega {@code amount} del material a cambio de
 * {@code priceDiamonds} diamantes fisicos.
 *
 * <p>El precio es por la oferta completa, no por unidad. Los campos de presentacion
 * ({@code nombre}, {@code lore}) son opcionales: si faltan se usa el nombre del material.</p>
 *
 * <p>Disponibilidad opcional:
 * <ul>
 *   <li>{@code biomes}: si no esta vacio, el jugador debe estar en uno de esos biomas.</li>
 *   <li>{@code rotation}: si no es null, la oferta rota segun periodo/ciclo deterministico.</li>
 * </ul></p>
 */
public record MarketItem(String id, Material material, int amount, int priceDiamonds,
                         String nombre, List<String> lore,
                         Set<String> biomes, MarketRotation rotation) {

    public MarketItem {
        lore = lore == null ? List.of() : List.copyOf(lore);
        biomes = biomes == null ? Set.of() : Set.copyOf(biomes);
    }

    /** Compatibilidad con construccion sin filtros (tests/codigo legado). */
    public MarketItem(String id, Material material, int amount, int priceDiamonds,
                      String nombre, List<String> lore) {
        this(id, material, amount, priceDiamonds, nombre, lore, Set.of(), null);
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

    public boolean tieneFiltroBioma() {
        return biomes != null && !biomes.isEmpty();
    }

    public boolean tieneRotacion() {
        return rotation != null;
    }
}
