package ar.com.maydana.mushokucore;

import ar.com.maydana.mushokucore.bootstrap.PluginBootstrap;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Clase principal de MushokuCore — texto del spec §8 L361-385 (manda el spec).
 * Solo API pública de Paper (org.bukkit.*), sin NMS (§2.3 L71-73).
 * El paquete main coincide con plugin.yml (§6 L233): {@code ar.com.maydana.mushokucore}.
 *
 * <p>[A MEDIAS] — los módulos y el wiring completo viven en {@link PluginBootstrap}:
 * Fase 1 (A/B/C/D) y Fase 2 (integrador) lo completan. Este archivo es de F0/§6.3.</p>
 */
public final class MushokuCorePlugin extends JavaPlugin {

    private PluginBootstrap bootstrap;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            bootstrap = new PluginBootstrap(this);
            bootstrap.enable();
            getLogger().info("MushokuCore iniciado correctamente.");
        } catch (Exception excepcion) {
            getLogger().log(Level.SEVERE, "No se pudo iniciar MushokuCore", excepcion);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            bootstrap.disable();
        }
    }
}
