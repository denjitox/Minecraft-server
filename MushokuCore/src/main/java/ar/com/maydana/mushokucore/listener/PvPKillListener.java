package ar.com.maydana.mushokucore.listener;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.database.repository.PvpRepository;
import ar.com.maydana.mushokucore.module.ranks.PotenciasService;
import ar.com.maydana.mushokucore.module.ranks.PvPKillValidator;
import ar.com.maydana.mushokucore.util.TimeUtils;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Bajas PvP válidas — spec §11 L577-588. Dueño: agente B (plan §5.2).
 *
 * <p>El handler solo captura datos del evento en el hilo principal (UUIDs, mundo, tiempos) y
 * despacha a un hilo asíncrono la lectura del cooldown de la pareja; la decisión la toma la
 * regla pura {@link PvPKillValidator} (testeable, §26). Si la baja es válida, llama a
 * {@link PotenciasService#registrarBajaValida}, que suma el punto y recalcula el puesto 5
 * (§11 L588).</p>
 *
 * <p><b>Hilo (plan §6 regla 3, §26 L1039):</b> cero SQL en el hilo principal. El scheduler
 * asíncrono ejecuta la lectura; la mutación de Bukkit de vuelta al principal la maneja el
 * servicio.</p>
 *
 * <p><b>Decisiones abiertas:</b> <b>[PENDIENTE D8]</b> identidad vinculada conocida → hoy
 * {@code false} ({@code IEstadoBedrock} no expone XUID). <b>DECISION D6 (2026-10-18):</b> los
 * segundos conectada de la víctima se aproximan con {@code Player.getLastLogin()} — la última
 * <i>entrada</i>, no el inicio de la sesión actual [A VERIFICAR]; valor {@code -1} = sin dato
 * fiable, que no bloquea la baja (no se fabrican rechazos sin dato).</p>
 */
public final class PvPKillListener implements Listener {

    private final PluginContext ctx;
    private final PotenciasService servicio;
    private final PvpRepository pvp;

    public PvPKillListener(PluginContext ctx, PotenciasService servicio, PvpRepository pvp) {
        this.ctx = ctx;
        this.servicio = servicio;
        this.pvp = pvp;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void alMorirJugador(PlayerDeathEvent evento) {
        Player victima = evento.getEntity();
        Player asesino = victima.getKiller();
        if (asesino == null || asesino.getUniqueId().equals(victima.getUniqueId())) {
            return; // muerte sin asesino (misma persona incluida, §11 L581)
        }
        UUID atacante = asesino.getUniqueId();
        UUID victimaId = victima.getUniqueId();
        String mundo = victima.getWorld().getName();
        long ahoraMs = System.currentTimeMillis();
        long conectadaSegundos = segundosConectada(victima, ahoraMs); // solo API Bukkit, en el hilo principal

        Set<String> excluidos;
        String mundoPrincipal;
        long victimaMinima;
        long cooldownMs;
        try {
            excluidos = ctx.config().mundosExcluidosPvp();
            mundoPrincipal = ctx.config().mundoNombre();
            victimaMinima = ctx.config().victimaMinimaConectadaSegundos();
            cooldownMs = ctx.config().pvpCooldownMismaVictimaHoras() * 3_600_000L;
        } catch (IllegalStateException sinConfig) {
            ctx.plugin().getLogger().log(Level.WARNING,
                    "Potencias: config no disponible; no se evalua la baja PvP " + atacante
                            + " -> " + victimaId + ".", sinConfig);
            return;
        }

        enAsync(() -> pvp.ultimoAsesinatoValido(atacante, victimaId).thenAccept(ultimo -> {
            Long ultimaBajaMs = ultimo.map(PvPKillListener::aEpochMs).orElse(null);
            PvPKillValidator.Contexto contexto = new PvPKillValidator.Contexto(
                    atacante, victimaId, mundo, excluidos, mundoPrincipal,
                    conectadaSegundos, victimaMinima,
                    false,   // [PENDIENTE D8] identidad vinculada conocida
                    false,   // sin mecanismo de anulación admin en MVP (§11 L586)
                    ultimaBajaMs, ahoraMs, cooldownMs);
            Optional<String> rechazo = PvPKillValidator.rechazo(contexto);
            if (rechazo.isEmpty()) {
                servicio.registrarBajaValida(atacante, victimaId, mundo, ahoraMs);
            } else {
                ctx.audit().registrar(atacante, "pvp.baja_rechazada", victimaId.toString(),
                        "mundo=" + mundo + "; motivo=" + rechazo.get());
            }
        }).whenComplete((vacio, fallo) -> {
            if (fallo != null) {
                ctx.plugin().getLogger().log(Level.WARNING,
                        "Potencias: no se pudo evaluar la baja PvP " + atacante + " -> " + victimaId, fallo);
            }
        }));
    }

    /** Despacha a un hilo asíncrono; si el plugin se está apagando, no hay scheduler: se descarta. */
    private void enAsync(Runnable tarea) {
        try {
            Bukkit.getScheduler().runTaskAsynchronously(ctx.plugin(), tarea);
        } catch (IllegalStateException apagando) {
            ctx.plugin().getLogger().fine(
                    "Potencias: scheduler no disponible (apagado); se descarta la evaluacion PvP.");
        }
    }

    private static Long aEpochMs(String iso) {
        Instant instante = TimeUtils.parse(iso);
        return instante == null ? null : instante.toEpochMilli();
    }

    /**
     * Segundos desde la última entrada de la víctima (§11 L585). {@code -1} = desconocido
     * (no bloquea la baja). [A VERIFICAR D6: {@code getLastLogin()} no es el inicio de la
     * sesión actual, sino la última conexión].
     */
    private static long segundosConectada(Player victima, long ahoraMs) {
        long ultimoIngreso = victima.getLastLogin();
        if (ultimoIngreso <= 0L || ultimoIngreso > ahoraMs) {
            return -1L;
        }
        return (ahoraMs - ultimoIngreso) / 1000L;
    }
}
