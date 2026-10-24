package ar.com.maydana.mushokucore.module.diseases;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Tarea periódica liviana de expiración — agente C, §13 L692-693 y plan §5.3.
 *
 * <p><b>Una sola tarea</b> para todos los jugadores (nunca una por jugador por tick): recorre
 * la caché en memoria de {@link DiseasesService} y expira lo vencido en el hilo principal.
 * El barrido corre cada {@code diseases.tick-check-ticks} (default 100 ticks = 5 s).</p>
 *
 * <p>La purga de filas vencidas de jugadores offline (SQL) corre cada
 * {@value #CADA_PURGA} barridos <b>[PROPUESTA]</b> (~60 s con 100 ticks): es un DELETE liviano,
 * no una operación por tick ni por jugador.</p>
 */
public final class DiseaseExpiryTask extends BukkitRunnable {

    /** [PROPUESTA] cada cuántos barridos se purgan las filas vencidas de offline. */
    private static final int CADA_PURGA = 12;

    private final DiseasesService servicio;
    private final JavaPlugin plugin;
    private final int periodoTicks;
    private int barridos;

    public DiseaseExpiryTask(DiseasesService servicio, JavaPlugin plugin, int periodoTicks) {
        this.servicio = servicio;
        this.plugin = plugin;
        this.periodoTicks = Math.max(1, periodoTicks);
    }

    /** Arranca el barrido periódico en el hilo del servidor. */
    public BukkitTask iniciar() {
        return runTaskTimer(plugin, periodoTicks, periodoTicks);
    }

    @Override
    public void run() {
        servicio.procesarExpiraciones();
        if (++barridos % CADA_PURGA == 0) {
            servicio.purgarExpiradas();
        }
    }

    /** Cancela el barrido; idempotente (el módulo puede llamarlo tras un disable). */
    public void cancelar() {
        try {
            cancel();
        } catch (IllegalStateException ignorado) {
            // ya estaba cancelada (p. ej. disable tras un apagado): nada que hacer.
        }
    }
}
