package ar.com.maydana.mushokucore.integration;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Repinta el tablist de TAB cuando cambia el grupo de LuckPerms de un jugador.
 *
 * <p>TAB expone los placeholders {@code %luckperms-prefix%} y compañía con refresco periódico de
 * 1000 ms. Forzar {@code PlayerPlaceholder.update(TabPlayer)} los recalcula en el mismo tick en
 * que se escribió el grupo, sin esperar el ciclo.</p>
 *
 * <p>Queda afuera el formato por grupo de {@code groups.yml}: TAB lo recalcula solo cada
 * {@code permission-refresh-interval} (1000 ms por defecto) y su API pública no permite
 * adelantarlo.</p>
 *
 * <p>Reflexión porque TAB es softdepend y su jar no está en el classpath de {@code build.ps1}.
 * Sin TAB, o con una API distinta, {@link #solicitar(UUID)} no hace nada y el tablist queda a
 * cargo del refresco periódico de TAB.</p>
 *
 * <p><b>Hilos:</b> {@link #solicitar(UUID)} acepta cualquier hilo (el outbox de LuckPerms es
 * asíncrono); la API de TAB solo se toca en el hilo principal.</p>
 */
public final class TabListRefresher {

    /** Placeholders de TAB que leen el grupo de LuckPerms, incluido el peso del orden. */
    private static final String[] PLACEHOLDERS = {
            "%luckperms-prefix%",
            "%luckperms-prefixes%",
            "%luckperms-suffix%",
            "%luckperms-suffixes%",
            "%luckperms-weight%"
    };

    private final JavaPlugin plugin;
    private final Logger log;

    private boolean resuelto;
    private Object api;
    private Method getPlayer;
    private Method getPlaceholderManager;
    private Method getPlaceholder;
    private Class<?> playerPlaceholder;
    private Method update;

    public TabListRefresher(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
    }

    /** Pide el refresco del tablist; desde un hilo async vuelve al principal primero. */
    public void solicitar(UUID jugador) {
        if (jugador == null) {
            return;
        }
        if (Bukkit.isPrimaryThread()) {
            refrescar(jugador);
            return;
        }
        try {
            Bukkit.getScheduler().runTask(plugin, () -> refrescar(jugador));
        } catch (RuntimeException apagando) {
            // En onDisable el scheduler rechaza tareas nuevas; el refresco no es crítico.
            log.fine("TAB: scheduler no disponible (apagado); se omite el refresco de " + jugador);
        }
    }

    private void refrescar(UUID jugador) {
        if (!resuelto) {
            resolver();
        }
        if (update == null) {
            return;
        }
        try {
            Object tabPlayer = getPlayer.invoke(api, jugador);
            if (tabPlayer == null) {
                return; // desconectado: no hay TabPlayer que repintar
            }
            Object manager = getPlaceholderManager.invoke(api);
            for (String id : PLACEHOLDERS) {
                Object placeholder = getPlaceholder.invoke(manager, id);
                if (playerPlaceholder.isInstance(placeholder)) {
                    update.invoke(placeholder, tabPlayer);
                }
            }
        } catch (ReflectiveOperationException | RuntimeException fallo) {
            log.log(Level.FINE, "TAB: no se pudo refrescar el tablist de " + jugador, fallo);
        }
    }

    /** Resuelve la API una sola vez: TAB carga antes por ser softdepend. */
    private synchronized void resolver() {
        resuelto = true;
        if (Bukkit.getPluginManager().getPlugin("TAB") == null) {
            return;
        }
        try {
            Class<?> claseApi = Class.forName("me.neznamy.tab.api.TabAPI");
            Class<?> claseTabPlayer = Class.forName("me.neznamy.tab.api.TabPlayer");
            Class<?> claseManager = Class.forName("me.neznamy.tab.api.placeholder.PlaceholderManager");
            Class<?> clasePlayerPlaceholder =
                    Class.forName("me.neznamy.tab.api.placeholder.PlayerPlaceholder");
            Object instancia = claseApi.getMethod("getInstance").invoke(null);
            if (instancia == null) {
                throw new IllegalStateException("TabAPI.getInstance() devolvio null");
            }
            api = instancia;
            getPlayer = claseApi.getMethod("getPlayer", UUID.class);
            getPlaceholderManager = claseApi.getMethod("getPlaceholderManager");
            getPlaceholder = claseManager.getMethod("getPlaceholder", String.class);
            playerPlaceholder = clasePlayerPlaceholder;
            update = clasePlayerPlaceholder.getMethod("update", claseTabPlayer);
            log.info("TAB detectado: el tablist se refresca al cambiar de Potencia.");
        } catch (ReflectiveOperationException | RuntimeException fallo) {
            log.log(Level.WARNING, "TAB presente pero su API no se pudo resolver; el tablist "
                    + "queda a cargo del refresco periodico de TAB.", fallo);
        }
    }
}
