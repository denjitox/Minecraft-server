package ar.com.maydana.mushokucore.module.npcs;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * Rutina liviana: cada N segundos gira a los vecinos hacia el jugador mas cercano.
 * No mueve entidades, asi que funciona igual con mannequins inmoviles y con el
 * fallback de aldeano. Con {@code npcs.routine-seconds: 0} no se agenda.
 */
public final class NpcRoutineTask implements Runnable {

    private final NpcsService servicio;
    private BukkitTask tarea;

    public NpcRoutineTask(NpcsService servicio) {
        this.servicio = servicio;
    }

    public void iniciar(org.bukkit.plugin.java.JavaPlugin plugin, int segundos) {
        long periodo = Math.max(1, segundos) * 20L;
        tarea = Bukkit.getScheduler().runTaskTimer(plugin, this, periodo, periodo);
    }

    public void cancelar() {
        if (tarea != null) {
            tarea.cancel();
            tarea = null;
        }
    }

    @Override
    public void run() {
        servicio.mirarJugadores();
    }
}
