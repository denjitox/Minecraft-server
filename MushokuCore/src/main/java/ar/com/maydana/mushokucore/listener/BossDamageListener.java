package ar.com.maydana.mushokucore.listener;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.module.ranks.ContributionTracker;
import ar.com.maydana.mushokucore.module.ranks.PotenciasService;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Contribución contra jefes — spec §11 L552-575 (Warden, Wither, Ender Dragon).
 * Dueño: agente B (plan §5.2). Registra el daño efectivo en el buffer en memoria y, al morir
 * el jefe, dispara la resolución de la Potencia.
 *
 * <p><b>Qué registra (§11 L553-561):</b> UUID del jefe, tipo, atacante real, daño efectivo
 * (<b>tope de overkill</b>: {@code min(dañoFinal, vida restante)}) e instante del último golpe.
 * Resuelve proyectiles y mascotas hasta el jugador responsable (vía
 * {@code DamageSource.getCausingEntity()} de Paper y, como respaldo,
 * {@code Projectile.getShooter()}/{@code Tameable}).</p>
 *
 * <p><b>Ender Dragon:</b> al golpear, el evento trae una <b>parte</b>
 * ({@code ComplexEntityPart}); se resuelve al padre (el dragón) para acumular bajo el mismo
 * UUID y leer la vida de la entidad real.</p>
 *
 * <p><b>Hilo (plan §6 regla 3, §26 L1039):</b> todo lo de este listener es memoria
 * ({@link ContributionTracker} es {@code synchronized}); el I/O de BD lo despacha
 * {@code PotenciasService} a un hilo asíncrono. La mutación de Bukkit de vuelta al hilo
 * principal la maneja el servicio.</p>
 *
 * <p><b>[PENDIENTE D10]</b> fuentes indirectas no resolubles a un jugador (p. ej. caída
 * provocada por un bloque, TNT sin dueño): no se acreditan. <b>DECISION D7</b> la validez
 * del mundo se decide recién en la muerte, con el mundo de la entidad (§11 L565).</p>
 */
public final class BossDamageListener implements Listener {

    /** Tipos de jefe del §11 L553, por nombre de {@code EntityType}. */
    private static final Set<String> JEFES = Set.of("WARDEN", "ENDER_DRAGON", "WITHER");

    private final PluginContext ctx;
    private final PotenciasService servicio;
    private final ContributionTracker tracker;

    public BossDamageListener(PluginContext ctx, PotenciasService servicio, ContributionTracker tracker) {
        this.ctx = ctx;
        this.servicio = servicio;
        this.tracker = tracker;
    }

    /** Acumula el daño efectivo (§11 L558). Prioridad MONITOR: lee el daño ya calculado. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void alDanarJefe(EntityDamageByEntityEvent evento) {
        Entity jefe = resolverJefe(evento.getEntity());
        if (jefe == null) {
            return;
        }
        String tipo = jefe.getType().name();
        if (!JEFES.contains(tipo)) {
            return;
        }
        UUID atacante = resolverAtacante(evento);
        if (atacante == null) {
            return;
        }
        double danio;
        if (jefe instanceof Damageable damageable) {
            double vida = damageable.getHealth();
            if (vida <= 0.0) {
                return; // la vida restante es 0: no hay daño efectivo que registrar
            }
            danio = Math.min(evento.getFinalDamage(), vida); // §11 L558: sin contar el exceso
        } else {
            danio = evento.getFinalDamage();
        }
        if (danio <= 0.0) {
            return;
        }
        tracker.registrarDanio(jefe.getUniqueId(), tipo, jefe.getWorld().getName(),
                atacante, danio, System.currentTimeMillis());
    }

    /** Resuelve la primera muerte válida (§11 L563). */
    @EventHandler(priority = EventPriority.MONITOR)
    public void alMorirJefe(EntityDeathEvent evento) {
        Entity jefe = resolverJefe(evento.getEntity());
        if (jefe == null) {
            return;
        }
        String tipo = jefe.getType().name();
        if (!JEFES.contains(tipo)) {
            return;
        }
        servicio.resolverMuerteDeJefe(jefe.getUniqueId(), tipo, jefe.getWorld().getName());
    }

    /** El Ender Dragon se golpea por partes; el padre es la entidad con vida y UUID estables. */
    private static Entity resolverJefe(Entity entidad) {
        if (entidad instanceof ComplexEntityPart parte) {
            return parte.getParent();
        }
        return entidad;
    }

    /**
     * Atacante real (§11 L561): primero el {@code DamageSource} de Paper (resuelve la cadena
     * proyectil/mascota hasta el responsable); si no, el damager directo.
     */
    private UUID resolverAtacante(EntityDamageByEntityEvent evento) {
        try {
            DamageSource fuente = evento.getDamageSource();
            if (fuente != null) {
                UUID deFuente = jugadorDe(fuente.getCausingEntity());
                if (deFuente != null) {
                    return deFuente;
                }
            }
        } catch (RuntimeException ignorada) {
            // La fuente puede no estar disponible en todos los eventos: se cae al damager.
            ctx.plugin().getLogger().log(Level.FINE,
                    "Potencias: DamageSource no disponible; se resuelve el damager directo.", ignorada);
        }
        Entity damager = evento.getDamager();
        UUID directo = jugadorDe(damager);
        if (directo != null) {
            return directo;
        }
        if (damager instanceof Projectile proyectil) {
            ProjectileSource tirador = proyectil.getShooter();
            if (tirador instanceof Entity entidad) {
                UUID deTirador = jugadorDe(entidad);
                if (deTirador != null) {
                    return deTirador;
                }
            }
        }
        if (damager instanceof Tameable domestico) {
            try {
                return domestico.getOwnerUniqueId(); // mascota domesticada por un jugador
            } catch (RuntimeException ignorada) {
                return null;
            }
        }
        return null; // [PENDIENTE D10]: fuente indirecta no resoluble
    }

    private static UUID jugadorDe(Entity entidad) {
        return entidad instanceof Player jugador ? jugador.getUniqueId() : null;
    }
}
