package ar.com.maydana.mushokucore.listener;

import ar.com.maydana.mushokucore.module.diseases.DiseasesService;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Tirada de contagio por daño — agente C (enfermedades), plan §5.2, spec §13 L684-693.
 *
 * <p>Espeja la lógica del {@code enfermedades.sk} actual: la tirada ocurre al recibir daño de
 * una entidad cuya fuente figura en {@code diseases.yml}. El mapa tipo→fuente es el vínculo
 * entre las claves del YAML ({@code HUSK_DAMAGE}, …) y las entidades vanilla; solo se contagian
 * las enfermedades cuyos {@code sources} incluyan esa clave. La reducción Orsted y la
 * probabilidad las resuelve {@link DiseasesService#intentarContagio(Player, String)}.</p>
 *
 * <p>Si el dañador es un proyectil, se usa su tirador (mascota/flecha) — el resto de las
 * fuentes del catálogo son cuerpo a cuerpo. [PENDIENTE D10] el spec §11 L561 no define el
 * fallback para daño indirecto; acá se atribuye al tirador.</p>
 */
public final class DiseaseInfectionListener implements Listener {

    private static final Map<EntityType, String> FUENTES = Map.of(
            EntityType.HUSK, "HUSK_DAMAGE",
            EntityType.ZOMBIE, "ZOMBIE_DAMAGE",
            EntityType.DROWNED, "DROWNED_DAMAGE",
            EntityType.CAVE_SPIDER, "CAVE_SPIDER_DAMAGE",
            EntityType.SILVERFISH, "SILVERFISH_DAMAGE",
            EntityType.SPIDER, "SPIDER_DAMAGE",
            EntityType.ENDERMAN, "ENDERMAN_DAMAGE");

    private final DiseasesService servicio;

    public DiseaseInfectionListener(DiseasesService servicio) {
        this.servicio = servicio;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void alRecibirDano(EntityDamageByEntityEvent evento) {
        if (!(evento.getEntity() instanceof Player victima)) {
            return;
        }
        String fuente = fuenteDe(evento.getDamager());
        if (fuente != null) {
            servicio.intentarContagio(victima, fuente);
        }
    }

    private static String fuenteDe(Entity danador) {
        Entity atacante = danador;
        if (atacante instanceof Projectile proyectil && proyectil.getShooter() instanceof Entity tirador) {
            atacante = tirador;
        }
        return FUENTES.get(atacante.getType());
    }
}
