package ar.com.maydana.mushokucore.module.npcs;

import ar.com.maydana.mushokucore.util.Componentes;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Crea la entidad de un vecino.
 *
 * <p>Si la definicion trae skin, se crea un {@link Mannequin} con un
 * {@link ResolvableProfile} estatico (propiedad {@code textures} con el base64 de
 * {@code npcs.yml}). Si no hay skin, o si el mannequin falla, se cae a un
 * {@link Villager} sin skin. En ambos casos la entidad lleva el id del NPC en su
 * {@code PersistentDataContainer}, que es la unica marca que se usa para reconciliar
 * duplicados al cargar chunks.</p>
 *
 * <p>La API de mannequin es tipada desde Paper 26.2, asi que no hace falta reflexion.
 * Si el jar llegara a correr en un servidor sin esa entidad, el {@link LinkageError}
 * se atrapa y queda el aldeano.</p>
 */
public final class NpcEntityFactory {

    private final JavaPlugin plugin;
    private final NamespacedKey clave;

    public NpcEntityFactory(JavaPlugin plugin) {
        this.plugin = plugin;
        this.clave = new NamespacedKey(plugin, "npc_id");
    }

    public NamespacedKey clave() {
        return clave;
    }

    public Entity crear(Location ubicacion, NpcDefinition definicion) {
        Entity entidad = definicion.tieneSkin() ? crearMannequin(ubicacion, definicion) : null;
        if (entidad == null) {
            entidad = ubicacion.getWorld().spawn(ubicacion, Villager.class);
        }
        preparar(entidad, definicion);
        return entidad;
    }

    private Entity crearMannequin(Location ubicacion, NpcDefinition definicion) {
        Mannequin mannequin = null;
        try {
            mannequin = ubicacion.getWorld().spawn(ubicacion, Mannequin.class);
            mannequin.setProfile(perfil(definicion));
            mannequin.setImmovable(true);
            if (definicion.titulo() != null && !definicion.titulo().isBlank()) {
                mannequin.setDescription(Componentes.de(definicion.titulo()));
            }
            return mannequin;
        } catch (RuntimeException | LinkageError fallo) {
            if (mannequin != null && mannequin.isValid()) {
                mannequin.remove();
            }
            plugin.getLogger().warning("NPC '" + definicion.id()
                    + "': no se pudo crear el mannequin con skin ("
                    + fallo.getClass().getSimpleName() + ": " + fallo.getMessage()
                    + "). Revisa skin.value en npcs.yml o npcs-skins.yml. Se usa aldeano.");
            return null;
        }
    }

    /**
     * Perfil estatico: con al menos una propiedad el mannequin no consulta Mojang.
     * Geyser toma estas mismas propiedades para armar la skin del jugador Bedrock.
     */
    private static ResolvableProfile perfil(NpcDefinition definicion) {
        // Sin nombre: el nombre visible lo pone customName. El perfil queda solo con la
        // textura, que es lo unico que mira el cliente para dibujar la skin.
        ResolvableProfile.Builder armador = ResolvableProfile.resolvableProfile();
        armador.addProperty(new ProfileProperty("textures", definicion.skinValue(), firma(definicion)));
        return armador.build();
    }

    /** La firma es opcional; una cadena vacia no vale como firma. */
    private static String firma(NpcDefinition definicion) {
        String firma = definicion.skinSignature();
        return firma == null || firma.isBlank() ? null : firma;
    }

    private void preparar(Entity entidad, NpcDefinition definicion) {
        entidad.setInvulnerable(true);
        entidad.setSilent(true);
        entidad.setGravity(false);
        entidad.setPersistent(true);
        entidad.setCustomNameVisible(true);
        entidad.customName(Componentes.de(definicion.nombre()));
        entidad.getPersistentDataContainer().set(clave, PersistentDataType.STRING, definicion.id());
        if (entidad instanceof LivingEntity vivo) {
            vivo.setRemoveWhenFarAway(false);
            try {
                vivo.setAI(false);
            } catch (RuntimeException ignorado) {
                // El mannequin no tiene IA; el aldeano si acepta setAI.
            }
        }
    }

    /** Id del NPC guardado en la entidad, o null si no es un vecino. */
    public String npcId(Entity entidad) {
        if (entidad == null) {
            return null;
        }
        return entidad.getPersistentDataContainer().get(clave, PersistentDataType.STRING);
    }
}
