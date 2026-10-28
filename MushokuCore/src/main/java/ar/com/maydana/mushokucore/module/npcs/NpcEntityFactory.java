package ar.com.maydana.mushokucore.module.npcs;

import ar.com.maydana.mushokucore.util.Componentes;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Crea la entidad de un vecino.
 *
 * <p>Intenta {@code org.bukkit.entity.Mannequin} por reflexion: es una entidad de
 * 1.21.9+ y el proyecto tiene que compilar aunque el {@code paper-api} del build no la
 * exponga. Si la clase no existe, si falla el spawn o si la skin no se puede aplicar,
 * cae a un {@link Villager} sin skin. En ambos casos la entidad lleva el id del NPC en
 * su {@code PersistentDataContainer}, que es la unica marca que se usa para
 * reconciliar duplicados al cargar chunks.</p>
 *
 * <p>El perfil con la skin tambien va por reflexion porque la API que resuelve
 * ({@code io.papermc.paper.datacomponent.item.ResolvableProfile}) esta marcada como
 * experimental y no se quiere atar el build a ella.</p>
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
        Class<?> claseMannequin;
        Entity entidad;
        try {
            claseMannequin = Class.forName("org.bukkit.entity.Mannequin");
            if (!Entity.class.isAssignableFrom(claseMannequin)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Class<? extends Entity> tipo = (Class<? extends Entity>) claseMannequin;
            entidad = ubicacion.getWorld().spawn(ubicacion, tipo);
        } catch (Throwable sinMannequin) {
            plugin.getLogger().fine("Mannequin no disponible para el NPC '" + definicion.id()
                    + "': " + sinMannequin.getClass().getSimpleName());
            return null;
        }

        try {
            aplicarPerfil(entidad, claseMannequin, definicion);
        } catch (Throwable fallo) {
            plugin.getLogger().warning("NPC '" + definicion.id()
                    + "': no se pudo aplicar la skin ("
                    + fallo.getClass().getSimpleName() + ": " + fallo.getMessage()
                    + "). Se usa aldeano.");
            entidad.remove();
            return null;
        }

        invocar(claseMannequin, entidad, "setImmovable", boolean.class, true);
        invocar(claseMannequin, entidad, "setDescription", Component.class,
                Componentes.de(definicion.titulo()));
        return entidad;
    }

    /**
     * Arma un {@code ResolvableProfile} estatico con la propiedad {@code textures} y lo
     * asigna al mannequin.
     *
     * <p>Clases reales en Paper 1.21.9+: el perfil es
     * {@code io.papermc.paper.datacomponent.item.ResolvableProfile} (Paper, no
     * {@code org.bukkit.profile}) y la propiedad es
     * {@code com.destroystokyo.paper.profile.ProfileProperty}. Con al menos una propiedad
     * el perfil queda estatico: el cliente no hace ninguna consulta de red y usa la skin
     * del base64.</p>
     */
    private static void aplicarPerfil(Entity entidad, Class<?> claseMannequin,
                                      NpcDefinition definicion)
            throws ReflectiveOperationException {
        Class<?> clasePerfil =
                Class.forName("io.papermc.paper.datacomponent.item.ResolvableProfile");
        // addProperty y build se buscan en la interfaz publica: la implementacion real
        // (BuilderImpl) es package-private, y un Method obtenido de esa clase falla con
        // IllegalAccessException al invocarlo desde aca.
        Class<?> claseArmador =
                Class.forName("io.papermc.paper.datacomponent.item.ResolvableProfile$Builder");
        Object armador = clasePerfil.getMethod("resolvableProfile").invoke(null);

        Class<?> clasePropiedad =
                Class.forName("com.destroystokyo.paper.profile.ProfileProperty");
        Object propiedad = clasePropiedad
                .getConstructor(String.class, String.class, String.class)
                .newInstance("textures", definicion.skinValue(), firma(definicion));
        claseArmador.getMethod("addProperty", clasePropiedad).invoke(armador, propiedad);

        Object perfil = claseArmador.getMethod("build").invoke(armador);
        claseMannequin.getMethod("setProfile", clasePerfil).invoke(entidad, perfil);
    }

    /** La firma es opcional; una cadena vacia no vale como firma. */
    private static String firma(NpcDefinition definicion) {
        String firma = definicion.skinSignature();
        return firma == null || firma.isBlank() ? null : firma;
    }

    private static void invocar(Class<?> tipo, Entity entidad, String metodo,
                                Class<?> parametro, Object valor) {
        try {
            tipo.getMethod(metodo, parametro).invoke(entidad, valor);
        } catch (ReflectiveOperationException | RuntimeException ignorado) {
            // La version no expone ese extra: el NPC sigue funcionando sin el.
        }
    }

    private void preparar(Entity entidad, NpcDefinition definicion) {
        entidad.setInvulnerable(true);
        entidad.setSilent(true);
        entidad.setGravity(false);
        entidad.setCustomNameVisible(true);
        entidad.customName(Componentes.de(definicion.nombre()));
        entidad.getPersistentDataContainer().set(clave, PersistentDataType.STRING, definicion.id());
        if (entidad instanceof LivingEntity vivo) {
            vivo.setRemoveWhenFarAway(false);
            try {
                vivo.setAI(false);
            } catch (RuntimeException ignorado) {
                // Algunas entidades no aceptan setAI; no es critico.
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
