package ar.com.maydana.mushokucore.config;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Carga y valida config.yml (schema 2) — agente A (infra-core), spec §9, plan §4.2.
 *
 * <p><b>Fallo seguro §2.5 / plan §4.2:</b> si el archivo del operador es schema 1 o no
 * tiene {@code schema}, NO se arranca ningún módulo de escritura ni se abre SQLite.
 * El archivo del operador NO se modifica (así lo exige el plan: lo reemplaza el operador
 * en el paso de instalación §24); se leen los defaults del jar para los getters y se
 * loguean instrucciones claras. Se puede construir con {@link #cargar(JavaPlugin)} y
 * consultar {@link #esquemaValido()}.</p>
 */
public final class ConfigService {

    private final PluginConfig config;
    private final boolean esquemaValido;

    private ConfigService(PluginConfig config, boolean esquemaValido) {
        this.config = config;
        this.esquemaValido = esquemaValido;
    }

    /**
     * Carga {@code plugins/MushokuCore/config.yml}; si no existe, escribe el default del jar
     * ({@code saveResource}). Si el schema no es 2, deja los módulos apagados y lo loguea.
     */
    public static ConfigService cargar(JavaPlugin plugin) {
        var archivo = new java.io.File(plugin.getDataFolder(), "config.yml");
        if (!archivo.exists()) {
            plugin.saveResource("config.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(archivo);
        int schema = yaml.getInt("schema", 0);
        if (schema == 2) {
            return new ConfigService(new PluginConfigImpl(yaml, false), true);
        }
        // Fallo seguro: schema 1 o ausente (config viejo del paquete base, 34 líneas).
        plugin.getLogger().warning("config.yml tiene schema=" + schema + " (se espera 2). "
                + "MushokuCore NO abre SQLite ni activa modulos de escritura (fallo seguro, spec §2.5). "
                + "Reemplaza plugins/MushokuCore/config.yml por el del JAR nuevo (schema 2) y reinicia. "
                + "El archivo existente NO fue modificado por el plugin.");
        YamlConfiguration defs = yamlDefaultDelJar(plugin);
        return new ConfigService(new PluginConfigImpl(defs, true), false);
    }

    /** Defaults del config.yml empaquetado en el JAR (nunca null: si falla, instancia vacía). */
    private static YamlConfiguration yamlDefaultDelJar(JavaPlugin plugin) {
        try (var flujo = plugin.getResource("config.yml")) {
            if (flujo != null) {
                return YamlConfiguration.loadConfiguration(
                        new InputStreamReader(flujo, StandardCharsets.UTF_8));
            }
        } catch (IOException excepcion) {
            plugin.getLogger().log(Level.WARNING, "No se pudo leer config.yml del JAR", excepcion);
        }
        return new YamlConfiguration();
    }

    /** El holder inmutable construido. Sus getters ya reflejan el fallo seguro si aplica. */
    public PluginConfig config() {
        return config;
    }

    /** true si el archivo del operador era schema 2 y se puede abrir la base. */
    public boolean esquemaValido() {
        return esquemaValido;
    }
}
