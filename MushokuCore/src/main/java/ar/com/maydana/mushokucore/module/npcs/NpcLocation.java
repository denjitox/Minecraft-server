package ar.com.maydana.mushokucore.module.npcs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Ubicacion persistida de un vecino. Se guarda en {@code npcs-locations.yml} y no en
 * {@code npcs.yml} para que el catalogo se pueda editar sin tocar coordenadas del mundo.
 */
public record NpcLocation(String mundo, double x, double y, double z, float yaw, float pitch) {

    /** La ubicacion Bukkit, o null si el mundo no esta cargado. */
    public Location aLocation() {
        World world = Bukkit.getWorld(mundo);
        return world == null ? null : new Location(world, x, y, z, yaw, pitch);
    }

    /** true si la ubicacion cae en el mismo bloque que {@code otra}. */
    public boolean mismoBloque(Location otra) {
        if (otra == null || otra.getWorld() == null) {
            return false;
        }
        return otra.getWorld().getName().equals(mundo)
                && otra.getBlockX() == (int) Math.floor(x)
                && otra.getBlockY() == (int) Math.floor(y)
                && otra.getBlockZ() == (int) Math.floor(z);
    }

    /**
     * Ancla de interaccion: el bloque que el NPC tiene a los pies. Se usa para abrir el
     * dialogo desde clientes que no pueden clickear la entidad.
     */
    public boolean esAncla(Location otra) {
        if (otra == null || otra.getWorld() == null) {
            return false;
        }
        return otra.getWorld().getName().equals(mundo)
                && otra.getBlockX() == (int) Math.floor(x)
                && otra.getBlockY() == (int) Math.floor(y) - 1
                && otra.getBlockZ() == (int) Math.floor(z);
    }
}
