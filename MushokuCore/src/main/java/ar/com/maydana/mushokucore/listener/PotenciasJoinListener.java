package ar.com.maydana.mushokucore.listener;

import ar.com.maydana.mushokucore.module.ranks.PotenciasService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Cachea el nombre del jugador al entrar. La resolución async de {@code PotenciasService}
 * (recálculo del puesto 5) solo puede leer esa cache; sin este listener el primer dueño
 * asignado desde un hilo async se guarda con {@code owner_name} en null.
 */
public final class PotenciasJoinListener implements Listener {

    private final PotenciasService servicio;

    public PotenciasJoinListener(PotenciasService servicio) {
        this.servicio = servicio;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void alEntrar(PlayerJoinEvent evento) {
        servicio.recordarNombre(evento.getPlayer().getUniqueId(), evento.getPlayer().getName());
    }
}
