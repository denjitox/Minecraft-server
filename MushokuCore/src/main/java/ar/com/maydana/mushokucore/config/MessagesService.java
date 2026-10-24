package ar.com.maydana.mushokucore.config;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Implementación de {@link Messages} sobre messages.yml — agente A (infra-core), plan §4.3.
 * El interface {@code Messages} es F0/contracto; la lectura vive acá (resolución Fase 0
 * documentada en el propio Messages.java).
 *
 * <p>Si messages.yml no existe se escribe el del JAR (esqueleto plan §4.3). Una clave
 * faltante devuelve la propia clave en gris (nunca null, fallo seguro §2.5) y se loguea
 * una sola vez por clave para no llenar la consola. Los {@code &} se traducen a códigos
 * de color de Bukkit.</p>
 */
public final class MessagesService implements Messages {

    private final YamlConfiguration yaml;
    private final java.util.logging.Logger log;
    private final Set<String> avisadas = ConcurrentHashMap.newKeySet();

    private MessagesService(YamlConfiguration yaml, java.util.logging.Logger log) {
        this.yaml = yaml;
        this.log = log;
    }

    /** Carga {@code plugins/MushokuCore/messages.yml}; lo crea desde el JAR si falta. */
    public static MessagesService cargar(JavaPlugin plugin) {
        File archivo = new File(plugin.getDataFolder(), "messages.yml");
        if (!archivo.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        return new MessagesService(
                YamlConfiguration.loadConfiguration(archivo), plugin.getLogger());
    }

    @Override
    public String texto(String clave) {
        return colorear(bruto(clave));
    }

    @Override
    public String texto(String clave, Map<String, String> reemplazos) {
        String texto = bruto(clave);
        if (reemplazos != null) {
            for (Map.Entry<String, String> par : reemplazos.entrySet()) {
                texto = texto.replace("{" + par.getKey() + "}",
                        par.getValue() == null ? "" : par.getValue());
            }
        }
        return colorear(texto);
    }

    private String bruto(String clave) {
        String valor = yaml.getString(clave);
        if (valor == null) {
            if (avisadas.add(clave)) {
                log.warning("Clave de messages.yml ausente: " + clave
                        + " (se muestra la clave como texto; fallo seguro §2.5).");
            }
            return ChatColor.GRAY + clave;
        }
        return valor;
    }

    private static String colorear(String texto) {
        return ChatColor.translateAlternateColorCodes('&', texto);
    }
}
