package ar.com.maydana.mushokucore.integration;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Implementación de {@link IEstadoBedrock} — agente A (infra-core), spec §16, plan §6.1.
 * Detección Java/Bedrock vía la API de Floodgate <b>por reflexión</b>: en el classpath
 * offline de compilación (build.ps1) no hay jar de Floodgate y [DECISIÓN D9] difiere
 * los compileOnly de terceros a 1.1; la reflexión además hace que la ausencia total del
 * plugin sea indistinguente de "no disponible" (fallo seguro §2.5, spec §2.5/§16 L792).
 *
 * <p>Sin formularios en MVP (BedrockFormUi = 1.3, spec §3 L132): el fallback por
 * comando/texto siempre está disponible.</p>
 */
public final class FloodgateHook implements IEstadoBedrock {

    private final Object api;
    private final Method esJugadorFloodgate;

    public FloodgateHook(JavaPlugin plugin) {
        Object instanciaApi = null;
        Method metodo = null;
        try {
            Class<?> clase = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Method obtener = clase.getMethod("getInstance");
            instanciaApi = obtener.invoke(null);
            metodo = clase.getMethod("isFloodgatePlayer", UUID.class);
            plugin.getLogger().info("Floodgate detectado: deteccion Java/Bedrock activa.");
        } catch (Throwable cualquierFallo) {
            // ClassNotFoundException (sin Floodgate) o API no lista: degradar en silencio.
            plugin.getLogger().log(Level.FINE,
                    "Floodgate no disponible; todos los jugadores se tratan como Java.", cualquierFallo);
        }
        this.api = instanciaApi;
        this.esJugadorFloodgate = metodo;
    }

    @Override
    public boolean floodgateDisponible() {
        return api != null && esJugadorFloodgate != null;
    }

    @Override
    public boolean esBedrock(UUID jugador) {
        if (jugador == null || !floodgateDisponible()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(esJugadorFloodgate.invoke(api, jugador));
        } catch (Throwable fallo) {
            // Nunca tumbar un join por la API de terceros (fallo seguro §2.5).
            return false;
        }
    }

    /** true si Floodgate está cargado como plugin (diagnóstico de /mushoku estado). */
    public static boolean pluginPresente() {
        return Bukkit.getPluginManager().getPlugin("floodgate") != null;
    }
}
