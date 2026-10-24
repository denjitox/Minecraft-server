package ar.com.maydana.mushokucore.bootstrap;

import ar.com.maydana.mushokucore.audit.IAuditoria;
import ar.com.maydana.mushokucore.config.Messages;
import ar.com.maydana.mushokucore.config.PluginConfig;
import ar.com.maydana.mushokucore.database.IDatabase;
import ar.com.maydana.mushokucore.integration.IEstadoBedrock;
import ar.com.maydana.mushokucore.integration.LuckPermsHook;
import ar.com.maydana.mushokucore.integration.TownyHook;
import ar.com.maydana.mushokucore.module.PlaceholderFuente;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Implementación de {@link PluginContext} — Fase 0 (agente F0, bootstrap/ es de F0 §6.3).
 *
 * <p><b>[A MEDIAS a propósito — quién completa qué]:</b></p>
 * <ul>
 *   <li><b>A (infra-core, Fase 1):</b> entrega {@code ConfigService}+impl de {@link PluginConfig},
 *       {@code MessagesService}, {@code DatabaseManager} ({@link IDatabase}),
 *       {@code AuditService} ({@link IAuditoria}), {@code FloodgateHook} ({@link IEstadoBedrock}),
 *       implementación de {@link LuckPermsHook} y {@code PlayerCoreListener}.</li>
 *   <li><b>I (integrador, Fase 2):</b> el <i>wiring</i> — construir esos objetos en
 *       {@code PluginBootstrap} y llamar a los {@code proveer*} de esta clase. Este archivo es
 *       de F0; si el integrador necesita cambiarlo, avisa (plan §6, regla 2).</li>
 *   <li>Acceder a un servicio sin proveer lanza {@link IllegalStateException} con el dueño
 *       pendiente: en el scaffold no hay implementaciones y el error debe ser explícito.</li>
 * </ul>
 *
 * <p>Los métodos que sí funcionan desde Fase 0: registro de servicios, comandos y listeners
 * (son mecánica de Bukkit, sin lógica de negocio).</p>
 */
public final class PluginContextImpl implements PluginContext {

    private final JavaPlugin plugin;
    private final Map<Class<?>, Object> servicios = new ConcurrentHashMap<>();
    private final List<PlaceholderFuente> fuentesPlaceholder = new CopyOnWriteArrayList<>();

    private PluginConfig config;
    private Messages messages;
    private IDatabase db;
    private IAuditoria audit;
    private IEstadoBedrock bedrock;
    private LuckPermsHook luckperms;
    private TownyHook towny;

    public PluginContextImpl(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    // ------------------------------------------------------------------
    // Proveedores del wiring (Fase 2 — dueño del llamado: integrador I)
    // ------------------------------------------------------------------

    public void proveerConfig(PluginConfig valor) { this.config = valor; }

    public void proveerMessages(Messages valor) { this.messages = valor; }

    public void proveerDatabase(IDatabase valor) { this.db = valor; }

    public void proveerAuditoria(IAuditoria valor) { this.audit = valor; }

    public void proveerEstadoBedrock(IEstadoBedrock valor) { this.bedrock = valor; }

    public void proveerLuckPerms(LuckPermsHook valor) { this.luckperms = valor; }

    public void proveerTowny(TownyHook valor) { this.towny = valor; }

    /**
     * Fuentes de placeholders registradas — <b>método extra fuera del interface</b> para que
     * {@code PlaceholderHook} (de D) pueda armar su expansión de PlaceholderAPI.
     * [A VERIFICAR] si se quiere exponer en {@link PluginContext} hay que actualizar el plan
     * §6.1 primero (regla 2); hoy D puede usarlo via cast o el integrador agrega el getter.
     */
    public List<PlaceholderFuente> fuentesPlaceholder() {
        return List.copyOf(fuentesPlaceholder);
    }

    // ------------------------------------------------------------------
    // PluginContext
    // ------------------------------------------------------------------

    @Override
    public JavaPlugin plugin() {
        return plugin;
    }

    @Override
    public PluginConfig config() {
        return exigir(config, "PluginConfig", "agente A (ConfigService) + wiring Fase 2 (integrador)");
    }

    @Override
    public Messages messages() {
        return exigir(messages, "Messages", "agente A (MessagesService) + wiring Fase 2 (integrador)");
    }

    @Override
    public IDatabase db() {
        return exigir(db, "IDatabase", "agente A (DatabaseManager) + wiring Fase 2 (integrador)");
    }

    @Override
    public IAuditoria audit() {
        return exigir(audit, "IAuditoria", "agente A (AuditService) + wiring Fase 2 (integrador)");
    }

    @Override
    public IEstadoBedrock bedrock() {
        return exigir(bedrock, "IEstadoBedrock", "agente A (FloodgateHook) + wiring Fase 2 (integrador)");
    }

    @Override
    public LuckPermsHook luckperms() {
        return exigir(luckperms, "LuckPermsHook", "agente A (implementación) + wiring Fase 2 (integrador)");
    }

    @Override
    public TownyHook towny() {
        return exigir(towny, "TownyHook", "infra-core");
    }

    @Override
    public <T> void registrarServicio(Class<T> tipo, T impl) {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(impl, "impl");
        Object anterior = servicios.put(tipo, impl);
        if (anterior != null) {
            plugin.getLogger().warning("Servicio " + tipo.getName()
                    + " registrado dos veces; se pisa el anterior (revisar orden de modulos).");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T obtener(Class<T> tipo) {
        // null si nadie lo publicó: fallo seguro §2.5 (módulo apagado no tumba a los demás).
        return (T) servicios.get(tipo);
    }

    @Override
    public void registrarPlaceholderFuente(PlaceholderFuente fuente) {
        fuentesPlaceholder.add(Objects.requireNonNull(fuente, "fuente"));
    }

    @Override
    public void registrarComando(String nombre, CommandExecutor exec, TabCompleter tab) {
        Objects.requireNonNull(nombre, "nombre");
        Objects.requireNonNull(exec, "exec");
        PluginCommand comando = plugin.getCommand(nombre);
        if (comando == null) {
            plugin.getLogger().warning("registroComando('" + nombre
                    + "'): el comando no esta declarado en plugin.yml (Fase 0).");
            return;
        }
        comando.setExecutor(exec);
        if (tab != null) {
            comando.setTabCompleter(tab);
        }
    }

    @Override
    public void registrarListener(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(
                Objects.requireNonNull(listener, "listener"), plugin);
    }

    private static <T> T exigir(T valor, String que, String duenoPendiente) {
        if (valor == null) {
            throw new IllegalStateException(que + " sin proveer todavia (scaffold Fase 0) — pendiente de: "
                    + duenoPendiente + ".");
        }
        return valor;
    }
}
