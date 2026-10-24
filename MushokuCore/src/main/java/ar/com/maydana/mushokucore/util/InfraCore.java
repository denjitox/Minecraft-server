package ar.com.maydana.mushokucore.util;

import ar.com.maydana.mushokucore.audit.AuditService;
import ar.com.maydana.mushokucore.bootstrap.PluginContextImpl;
import ar.com.maydana.mushokucore.config.ConfigService;
import ar.com.maydana.mushokucore.config.MessagesService;
import ar.com.maydana.mushokucore.database.DatabaseManager;
import ar.com.maydana.mushokucore.database.repository.PlayerRepository;
import ar.com.maydana.mushokucore.integration.FloodgateHook;
import ar.com.maydana.mushokucore.integration.LuckPermsService;
import ar.com.maydana.mushokucore.integration.TownyHook;
import ar.com.maydana.mushokucore.listener.PlayerCoreListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Instalador de todo el stack de infra-core en UNA llamada — agente A (infra-core).
 * Propiedad A (plan §6.3: {@code util/*}); usa los {@code proveer*} públicos de
 * {@link PluginContextImpl} (bootstrap/ es de F0; no se modifica).
 *
 * <p><b>Uso (wiring de {@code PluginBootstrap}, dueño: integrador I, Fase 2):</b></p>
 * <pre>
 *   InfraCore.Instalacion infra = InfraCore.instalar(plugin, contexto);
 *   // en disable(): InfraCore.cerrar(infra);
 * </pre>
 *
 * <p>Orden (spec §8): config → mensajes → hooks de dependencias opcionales → SQLite +
 * migraciones (único punto que espera I/O de BD) → auditoría → listener core. Si el
 * schema del config.yml del operador no es 2, <b>no se abre SQLite</b> y los módulos
 * quedan apagados (fallo seguro §2.5); el resto de los getters siguen respondiendo.</p>
 */
public final class InfraCore {

    private InfraCore() {
    }

    /** Resultado de {@link #instalar}; guarda lo necesario para el cierre de onDisable. */
    public record Instalacion(DatabaseManager db, boolean esquemaValido) {

        /** true si la base se abrió y hay algo que cerrar. */
        public boolean baseAbierta() {
            return db != null;
        }
    }

    /**
     * Construye y provee todo el stack de A en el contexto. Lanza si SQLite falla de
     * forma irrecuperable (spec §8: MushokuCorePlugin captura y deshabilita el plugin;
     * una migración roja nunca deja el esquema a medias).
     */
    public static Instalacion instalar(JavaPlugin plugin, PluginContextImpl contexto)
            throws Exception {
        ConfigService configSvc = ConfigService.cargar(plugin);
        contexto.proveerConfig(configSvc.config());
        contexto.proveerMessages(MessagesService.cargar(plugin));

        FloodgateHook bedrock = new FloodgateHook(plugin);
        contexto.proveerEstadoBedrock(bedrock);
        LuckPermsService luckperms = new LuckPermsService(plugin);
        contexto.proveerLuckPerms(luckperms);
        TownyHook towny = new TownyHook();
        contexto.proveerTowny(towny);

        if (!configSvc.esquemaValido()) {
            plugin.getLogger().warning("SQLite NO se abre (schema de config.yml invalido). "
                    + "El plugin queda a la espera de un config.yml schema 2 (spec §2.5).");
            return new Instalacion(null, false);
        }

        DatabaseManager db = DatabaseManager.abrir(
                plugin, configSvc.config(), Bukkit::isPrimaryThread, 30);
        contexto.proveerDatabase(db);

        AuditService auditoria = new AuditService(plugin, db);
        contexto.proveerAuditoria(auditoria);

        PlayerCoreListener listener = new PlayerCoreListener(
                plugin, new PlayerRepository(db), bedrock, plugin.getLogger());
        contexto.registrarListener(listener);

        plugin.getLogger().info("Infra-core instalado: SQLite=" + db.archivo().getFileName()
                + ", floodgate=" + (bedrock.floodgateDisponible() ? "si" : "no (fallback texto)")
                + ", luckperms=" + (luckperms.disponible() ? "si" : "no (ranks degradado)")
                + ", towny=" + (towny.isPresent() ? "si" : "no (factions degradado)"));
        return new Instalacion(db, true);
    }

    /**
     * Paso 3-4 de onDisable (spec §8 L353): vacía las colas de escritura y cierra la
     * base. Idempotente; cada fase se intenta aunque la anterior falle.
     */
    public static void cerrar(Instalacion instalacion) {
        if (instalacion == null || instalacion.db() == null) {
            return;
        }
        DatabaseManager db = instalacion.db();
        try {
            db.flushAll();
        } catch (Exception excepcion) {
            // "mejor perder la cola que colgar" (spec §8 L353): se intenta cerrar igual.
            excepcion.printStackTrace();
        }
        db.cerrar();
    }
}
