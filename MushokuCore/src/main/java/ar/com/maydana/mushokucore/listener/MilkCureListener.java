package ar.com.maydana.mushokucore.listener;

import ar.com.maydana.mushokucore.module.diseases.DiseasesService;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

/**
 * Curación con leche — agente C (enfermedades), plan §5.2, spec §13 L690.
 *
 * <p><b>[PENDIENTE V15]</b>: el spec dice "permitir curación con leche <i>si así se configura</i>";
 * el ejemplo solo la declara para {@code dryne}, mientras el Skript actual borra todo efecto con
 * leche. Se obedece la config (plan §4.4): solo se curan las enfermedades cuyo {@code cures}
 * incluya {@code MILK}. Si el usuario decide que la leche cure todo, se cambia solo
 * {@code diseases.yml} (no este archivo).</p>
 */
public final class MilkCureListener implements Listener {

    private final DiseasesService servicio;

    public MilkCureListener(DiseasesService servicio) {
        this.servicio = servicio;
    }

    @EventHandler(ignoreCancelled = true)
    public void alConsumir(PlayerItemConsumeEvent evento) {
        if (evento.getItem().getType() != Material.MILK_BUCKET) {
            return;
        }
        servicio.curarConLeche(evento.getPlayer());
    }
}
