package ar.com.maydana.mushokucore.listener;

import ar.com.maydana.mushokucore.database.repository.PlayerRepository;
import ar.com.maydana.mushokucore.integration.IEstadoBedrock;
import ar.com.maydana.mushokucore.util.TimeUtils;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Join core — agente A (infra-core), plan §2/§5.2: refresca {@code last_name} en cada
 * join (spec §2.4 L75-77), persiste el flag Bedrock (detección §16) y siembra la fila
 * del jugador. Todo async vía {@code IDatabase}: cero SQL en el hilo principal (§26 L1039).
 *
 * <p>La caché de placeholders del §18 es de D (PlaceholderHook); este listener solo
 * garantiza que la fila de players exista y esté fresca cuando D la lea.</p>
 */
public final class PlayerCoreListener implements Listener {

    private final PlayerRepository jugadores;
    private final IEstadoBedrock bedrock;
    private final Logger log;
    private final JavaPlugin plugin;

    public PlayerCoreListener(JavaPlugin plugin, PlayerRepository jugadores, IEstadoBedrock bedrock, Logger log) {
        this.plugin = plugin;
        this.jugadores = jugadores;
        this.bedrock = bedrock;
        this.log = log;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void alEntrar(PlayerJoinEvent evento) {
        Player jugador = evento.getPlayer();
        UUID uuid = jugador.getUniqueId();
        String nombre = jugador.getName();
        boolean esBedrock = bedrock.esBedrock(uuid);
        String ahora = TimeUtils.ahoraIso();
        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskAsynchronously(plugin, () -> {
            jugadores.registrarEntrada(uuid, nombre, esBedrock, ahora)
                    .whenComplete((vacio, fallo) -> {
                        if (fallo != null) {
                            log.log(Level.WARNING,
                                    "No se pudo registrar el join de " + nombre + " en players", fallo);
                        }
                    });
        });
    }
}
