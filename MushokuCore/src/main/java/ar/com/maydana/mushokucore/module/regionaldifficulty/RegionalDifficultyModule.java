package ar.com.maydana.mushokucore.module.regionaldifficulty;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.config.PluginConfig;
import ar.com.maydana.mushokucore.module.ModuleLifecycle;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Enemy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Módulo de escalado regional de dificultad — aplica modificadores de vida y daño a mobs
 * según la distancia al spawn (0,0) y la dimensión.
 *
 * <p>Escucha {@link CreatureSpawnEvent} y aplica {@link AttributeModifier} a
 * {@code generic.max_health} y {@code generic.attack_damage}
 * según la configuración en {@code regional-difficulty}.</p>
 *
 * <p>Solo procesa enemigos. Las exclusiones son configurables y los Trial Spawners
 * tienen un límite propio de vida.</p>
 */
public final class RegionalDifficultyModule implements ModuleLifecycle {

    @Override
    public String id() {
        return "regional-difficulty";
    }

    @Override
    public void enable(PluginContext ctx) throws Exception {
        if (!ctx.config().moduloRegionalDifficulty()) {
            ctx.plugin().getLogger().info("Modulo 'regional-difficulty' apagado en config.yml "
                    + "(modules.regional-difficulty=false): no se registra nada (spec §8 L351).");
            return;
        }
        if (!ctx.config().regionalDifficultyHabilitado()) {
            ctx.plugin().getLogger().info("Modulo 'regional-difficulty' desactivado por regional-difficulty.enabled=false.");
            return;
        }

        PluginConfig config = ctx.config();
        JavaPlugin plugin = ctx.plugin();
        Logger log = plugin.getLogger();
        int centerX = config.regionalDifficultyCentroX();
        int centerZ = config.regionalDifficultyCentroZ();
        var zones = config.regionalDifficultyZonas();
        var dimensions = config.regionalDifficultyDimensiones();
        Set<String> excludedEntities = config.regionalDifficultyExcluidas();
        boolean trialSpawnerLimit = config.regionalDifficultyTrialSpawnerLimite();
        double trialSpawnerMaxHealthMultiplier = config.regionalDifficultyTrialSpawnerMaxHealthMultiplier();
        boolean interpolate = config.regionalDifficultyInterpolar();
        int blendDistance = config.regionalDifficultyBlendDistancia();

        Set<EntityType> excludedTypes = EnumSet.noneOf(EntityType.class);
        for (String name : excludedEntities) {
            try {
                EntityType type = EntityType.valueOf(name.toUpperCase(Locale.ROOT));
                excludedTypes.add(type);
            } catch (IllegalArgumentException error) {
                log.warning("Entidad excluida desconocida en regional-difficulty: " + name);
            }
        }

        Listener spawnListener = new RegionalDifficultyListener(
                plugin, centerX, centerZ, zones, dimensions,
                excludedTypes, trialSpawnerLimit, trialSpawnerMaxHealthMultiplier,
                interpolate, blendDistance
        );
        ctx.registrarListener(spawnListener);

        log.info("Modulo 'regional-difficulty' habilitado: " + zones.size() + " zonas Overworld, "
                + dimensions.size() + " dimensiones especiales, " + excludedTypes.size() + " entidades excluidas "
                + "(interpolacion=" + interpolate + ", blend=" + blendDistance + ").");
    }

    @Override
    public void disable() {
    }

    /**
     * Listener que aplica los modificadores de atributos al spawnear criaturas.
     */
    public static final class RegionalDifficultyListener implements Listener {

        private final int centerX;
        private final int centerZ;
        private final java.util.List<PluginConfig.RegionalDifficultyZone> zones;
        private final java.util.Map<String, PluginConfig.RegionalDifficultyDimension> dimensions;
        private final Set<EntityType> excludedTypes;
        private final boolean trialSpawnerLimit;
        private final double trialSpawnerMaxHealthMultiplier;
        private final boolean interpolate;
        private final int blendDistance;
        private final NamespacedKey healthModifierKey;
        private final NamespacedKey damageModifierKey;

        public RegionalDifficultyListener(
                JavaPlugin plugin,
                int centerX, int centerZ,
                java.util.List<PluginConfig.RegionalDifficultyZone> zones,
                java.util.Map<String, PluginConfig.RegionalDifficultyDimension> dimensions,
                Set<EntityType> excludedTypes,
                boolean trialSpawnerLimit,
                double trialSpawnerMaxHealthMultiplier) {
            this(plugin, centerX, centerZ, zones, dimensions, excludedTypes, trialSpawnerLimit,
                    trialSpawnerMaxHealthMultiplier, true, 200);
        }

        public RegionalDifficultyListener(
                JavaPlugin plugin,
                int centerX, int centerZ,
                java.util.List<PluginConfig.RegionalDifficultyZone> zones,
                java.util.Map<String, PluginConfig.RegionalDifficultyDimension> dimensions,
                Set<EntityType> excludedTypes,
                boolean trialSpawnerLimit,
                double trialSpawnerMaxHealthMultiplier,
                boolean interpolate,
                int blendDistance) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.zones = zones;
            this.dimensions = dimensions;
            this.excludedTypes = excludedTypes;
            this.trialSpawnerLimit = trialSpawnerLimit;
            this.trialSpawnerMaxHealthMultiplier = trialSpawnerMaxHealthMultiplier;
            this.interpolate = interpolate;
            this.blendDistance = blendDistance > 0 ? blendDistance : 0;
            this.healthModifierKey = new NamespacedKey(plugin, "regional_health");
            this.damageModifierKey = new NamespacedKey(plugin, "regional_damage");
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onCreatureSpawn(CreatureSpawnEvent event) {
            Entity entity = event.getEntity();
            EntityType type = entity.getType();

            if (!(entity instanceof Enemy) || excludedTypes.contains(type)) {
                return;
            }

            double healthMultiplier;
            double damageMultiplier;
            World.Environment environment = entity.getWorld().getEnvironment();

            if (environment == World.Environment.NETHER) {
                PluginConfig.RegionalDifficultyDimension dim = dimensions.get("the_nether");
                healthMultiplier = dim != null ? dim.healthMultiplier() : 1.40;
                damageMultiplier = dim != null ? dim.damageMultiplier() : 1.30;
            } else if (environment == World.Environment.THE_END) {
                PluginConfig.RegionalDifficultyDimension dim = dimensions.get("the_end");
                healthMultiplier = dim != null ? dim.healthMultiplier() : 1.50;
                damageMultiplier = dim != null ? dim.damageMultiplier() : 1.35;
            } else {
                double dx = entity.getLocation().getX() - centerX;
                double dz = entity.getLocation().getZ() - centerZ;
                double distance = Math.sqrt(dx * dx + dz * dz);

                healthMultiplier = 1.0;
                damageMultiplier = 1.0;
                if (!zones.isEmpty()) {
                    PluginConfig.RegionalDifficultyZone currentZone = null;
                    int currentIndex = -1;
                    for (int i = 0; i < zones.size(); i++) {
                        PluginConfig.RegionalDifficultyZone zone = zones.get(i);
                        int minR = zone.minRadius();
                        int maxR = zone.maxRadius();
                        if (distance >= minR && distance <= maxR) {
                            currentZone = zone;
                            currentIndex = i;
                            break;
                        }
                    }
                    if (currentZone == null) {
                        for (int i = zones.size() - 1; i >= 0; i--) {
                            PluginConfig.RegionalDifficultyZone zone = zones.get(i);
                            if (distance >= zone.minRadius()) {
                                currentZone = zone;
                                currentIndex = i;
                                break;
                            }
                        }
                    }
                    if (currentZone == null) {
                        currentZone = zones.get(0);
                        currentIndex = 0;
                    }

                    healthMultiplier = currentZone.healthMultiplier();
                    damageMultiplier = currentZone.damageMultiplier();

                    if (interpolate && blendDistance > 0 && currentIndex >= 0 && currentIndex < zones.size() - 1) {
                        PluginConfig.RegionalDifficultyZone nextZone = zones.get(currentIndex + 1);
                        double blendStart = Math.max(currentZone.minRadius(), currentZone.maxRadius() - blendDistance);
                        if (distance >= blendStart && distance < nextZone.minRadius()) {
                            double blendWidth = Math.max(1.0, nextZone.minRadius() - blendStart);
                            double t = (distance - blendStart) / blendWidth;
                            if (t < 0.0) {
                                t = 0.0;
                            }
                            if (t > 1.0) {
                                t = 1.0;
                            }
                            healthMultiplier = lerp(currentZone.healthMultiplier(), nextZone.healthMultiplier(), t);
                            damageMultiplier = lerp(currentZone.damageMultiplier(), nextZone.damageMultiplier(), t);
                        }
                    }
                }
            }

            if (trialSpawnerLimit && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER) {
                healthMultiplier = Math.min(healthMultiplier, trialSpawnerMaxHealthMultiplier);
            }

            if (healthMultiplier > 1.0) {
                applyHealthModifier(entity, healthMultiplier);
            }
            if (damageMultiplier > 1.0) {
                applyDamageModifier(entity, damageMultiplier);
            }
        }

        private void applyHealthModifier(Entity entity, double multiplier) {
            if (!(entity instanceof org.bukkit.entity.LivingEntity living)) return;
            var attribute = living.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (attribute == null || attribute.getModifier(healthModifierKey) != null) return;

            double baseValue = attribute.getBaseValue();
            double bonus = baseValue * (multiplier - 1.0);
            double previousMax = attribute.getValue();
            boolean wasFull = living.getHealth() >= previousMax;

            AttributeModifier modifier = new AttributeModifier(
                    healthModifierKey,
                    bonus,
                    AttributeModifier.Operation.ADD_NUMBER
            );
            attribute.addModifier(modifier);

            if (wasFull) {
                living.setHealth(Math.min(attribute.getValue(), previousMax + bonus));
            }
        }

        private void applyDamageModifier(Entity entity, double multiplier) {
            if (!(entity instanceof org.bukkit.entity.LivingEntity living)) return;
            var attribute = living.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE);
            if (attribute == null || attribute.getModifier(damageModifierKey) != null) return;

            double baseValue = attribute.getBaseValue();
            double bonus = baseValue * (multiplier - 1.0);

            AttributeModifier modifier = new AttributeModifier(
                    damageModifierKey,
                    bonus,
                    AttributeModifier.Operation.ADD_NUMBER
            );
            attribute.addModifier(modifier);
        }

        private static double lerp(double from, double to, double t) {
            return from + (to - from) * t;
        }
    }
}
