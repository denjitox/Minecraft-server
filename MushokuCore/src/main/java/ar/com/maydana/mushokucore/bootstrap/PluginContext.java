package ar.com.maydana.mushokucore.bootstrap;

import ar.com.maydana.mushokucore.audit.IAuditoria;
import ar.com.maydana.mushokucore.config.Messages;
import ar.com.maydana.mushokucore.config.PluginConfig;
import ar.com.maydana.mushokucore.database.IDatabase;
import ar.com.maydana.mushokucore.integration.IEstadoBedrock;
import ar.com.maydana.mushokucore.integration.LuckPermsHook;
import ar.com.maydana.mushokucore.integration.TownyHook;
import ar.com.maydana.mushokucore.module.PlaceholderFuente;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * CONTRATO CONGELADO — Fase 0 (agente F0), plan §6.1 (texto casi literal del plan).
 * <p>Servicio locator: los módulos (A/B/C/D) consumen por aquí lo que poseen otros módulos
 * en vez de instanciar clases ajenas (plan §6, regla 1). Implementación:
 * {@code PluginContextImpl} (bootstrap/, F0).</p>
 *
 * <p>Los servicios de base ({@link #config()}, {@link #messages()}, {@link #db()},
 * {@link #audit()}, {@link #bedrock()}, {@link #luckperms()}) se "proveen" desde fuera;
 * en el scaffold de Fase 0 todavía no hay implementaciones y lanzan
 * {@link IllegalStateException} con el dueño pendiente — el wiring es de A/Fase 2.</p>
 */
public interface PluginContext {

    /** La instancia del plugin (scheduler, logger, recursos). */
    JavaPlugin plugin();

    /** Configuración validada (config.yml schema 2, spec §9). Impl: agente A (ConfigService). */
    PluginConfig config();

    /** Textos de messages.yml. Impl: agente A (MessagesService). */
    Messages messages();

    /** Acceso async a SQLite. Impl: agente A (DatabaseManager). */
    IDatabase db();

    /** Auditoría de toda escritura (spec §17 L835). Impl: agente A (AuditService). */
    IAuditoria audit();

    /** Detección Java/Bedrock vía Floodgate (spec §16). Impl: agente A (FloodgateHook). */
    IEstadoBedrock bedrock();

    /** Grupos y permisos (spec §11 L572-573). Impl: agente A (clase nueva sobre este interface). */
    LuckPermsHook luckperms();

    /** Acceso tipado a pueblos, alcaldes, naciones y claims de Towny. */
    TownyHook towny();

    /**
     * Un módulo publica su API para que otros la consuman
     * (B publica {@code IPotenciasService}, C publica {@code IEnfermedadesService}).
     */
    <T> void registrarServicio(Class<T> tipo, T impl);

    /**
     * Obtiene un servicio publicado. Devuelve {@code null} si nadie lo registró
     * (p. ej. módulo apagado en modules.*): el llamador decide cómo degradar —
     * fallo seguro §2.5. [PROPUESTA F0] el plan §6.1 no fijaba la semántica de "no registrado".
     */
    <T> T obtener(Class<T> tipo);

    /** Registra una fuente de placeholders (§18 L856: solo caché, cero SQL por render). */
    void registrarPlaceholderFuente(PlaceholderFuente fuente);

    /**
     * Registra un comando declarado en plugin.yml.
     * @param nombre nombre del comando en plugin.yml (p. ej. {@code "mushoku"}).
     * @param exec   executor (obligatorio para que el comando responda).
     * @param tab    tab-completador; puede ser {@code null} (anulable según el plan §6.1).
     */
    void registrarComando(String nombre, CommandExecutor exec, TabCompleter tab);

    /** Registra un listener (escanea sus @EventHandler). */
    void registrarListener(Listener listener);
}
