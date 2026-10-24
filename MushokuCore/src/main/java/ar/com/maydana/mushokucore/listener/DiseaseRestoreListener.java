package ar.com.maydana.mushokucore.listener;

import ar.com.maydana.mushokucore.module.diseases.DiseasesService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Restaura/volca el estado de enfermedades — agente C (enfermedades), plan §5.2, spec §13 L689.
 *
 * <p>Al entrar: relee las filas de {@code diseases} (async) y reaplica sus efectos en el hilo
 * principal, para que la enfermedad sobreviva a una reconexión. Al salir: suelta la copia en
 * memoria (flush; el estado ya se persistió write-through en cada mutación).</p>
 *
 * <p>La lectura del join es de {@code DiseasesService#cargar(UUID)}, que nunca bloquea el hilo
 * del servidor (§26 L1039).</p>
 */
public final class DiseaseRestoreListener implements Listener {

    private final DiseasesService servicio;

    public DiseaseRestoreListener(DiseasesService servicio) {
        this.servicio = servicio;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void alEntrar(PlayerJoinEvent evento) {
        servicio.cargar(evento.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void alSalir(PlayerQuitEvent evento) {
        servicio.descargar(evento.getPlayer().getUniqueId());
    }
}
