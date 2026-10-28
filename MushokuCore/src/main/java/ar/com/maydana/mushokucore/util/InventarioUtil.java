package ar.com.maydana.mushokucore.util;

import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Operaciones de inventario compartidas por el mercado y los vecinos.
 *
 * <p>Todo se ejecuta en el hilo principal: son mutaciones Bukkit. Las cantidades se
 * validan antes de llamar a {@link #quitar}; si se llama sin saldo, la funcion devuelve
 * false habiendo quitado lo que encontro (por eso los llamadores primero cuentan).</p>
 */
public final class InventarioUtil {

    private InventarioUtil() {
    }

    /** Cantidad total de un material en el inventario del jugador. */
    public static int contar(Player jugador, Material material) {
        return contar(jugador.getInventory(), material);
    }

    public static int contar(Inventory inventario, Material material) {
        int total = 0;
        for (ItemStack item : inventario.getContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        return total;
    }

    /**
     * Quita hasta {@code cantidad} del material indicado.
     * @return true si se quito la cantidad completa
     */
    public static boolean quitar(Player jugador, Material material, int cantidad) {
        if (cantidad <= 0) {
            return true;
        }
        PlayerInventory inventario = jugador.getInventory();
        int restante = cantidad;
        ItemStack[] contenidos = inventario.getContents();
        for (int indice = 0; indice < contenidos.length && restante > 0; indice++) {
            ItemStack item = contenidos[indice];
            if (item == null || item.getType() != material) {
                continue;
            }
            int quitados = Math.min(restante, item.getAmount());
            int nuevo = item.getAmount() - quitados;
            if (nuevo <= 0) {
                inventario.setItem(indice, null);
            } else {
                item.setAmount(nuevo);
                inventario.setItem(indice, item);
            }
            restante -= quitados;
        }
        return restante == 0;
    }

    /** Entrega el item; lo que no entra se deja caer a los pies del jugador. */
    public static void entregar(Player jugador, ItemStack item) {
        Map<Integer, ItemStack> sobrante = jugador.getInventory().addItem(item);
        for (ItemStack resto : sobrante.values()) {
            jugador.getWorld().dropItemNaturally(jugador.getLocation(), resto);
        }
    }

    /**
     * true si el inventario tiene lugar para el item completo, contando stacks
     * incompletos del mismo material.
     *
     * <p>Se mira {@code getStorageContents()} y no {@code getContents()}: los slots de
     * armadura y la mano secundaria no los usa {@link org.bukkit.inventory.PlayerInventory#addItem},
     * asi que contarlos como libres haria cobrar una compra que despues cae al piso.
     * No tiene en cuenta slots que se liberen despues.</p>
     */
    public static boolean puedeEntregar(Inventory inventario, ItemStack item) {
        int faltante = item.getAmount();
        int maximo = Math.max(1, item.getMaxStackSize());
        ItemStack[] contenidos = inventario.getStorageContents();
        for (ItemStack actual : contenidos) {
            if (actual != null && actual.isSimilar(item)) {
                faltante -= Math.max(0, maximo - actual.getAmount());
                if (faltante <= 0) {
                    return true;
                }
            }
        }
        int libres = 0;
        for (ItemStack actual : contenidos) {
            if (actual == null) {
                libres++;
            }
        }
        return libres * maximo >= faltante;
    }
}
