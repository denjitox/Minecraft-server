package ar.com.maydana.mushokucore.bootstrap;

import ar.com.maydana.mushokucore.audit.AuditArchiveTask;
import ar.com.maydana.mushokucore.database.repository.AuditRepository;
import ar.com.maydana.mushokucore.module.commands.CommandsModule;
import ar.com.maydana.mushokucore.module.diseases.DiseasesModule;
import ar.com.maydana.mushokucore.module.factions.FactionsModule;
import ar.com.maydana.mushokucore.module.market.MarketModule;
import ar.com.maydana.mushokucore.module.npcs.NpcsModule;
import ar.com.maydana.mushokucore.module.ranks.PotenciasModule;
import ar.com.maydana.mushokucore.module.regionaldifficulty.RegionalDifficultyModule;
import ar.com.maydana.mushokucore.util.InfraCore;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Ciclo de inicio y apagado — Fase 0 (agente F0, plan §2/§6.3; secuencia completa: spec §8).
 *
 * <p><b>Estado del wiring (integrador, Fase 2 — 2026-10-18):</b> {@link InfraCore#instalar} se
 * llama en {@link #enable()} <b>antes</b> de habilitar los módulos (spec §8 pasos 1-6), y
 * {@link InfraCore#cerrar} en {@link #disable()} (spec §8 L351-355). Sin ese wiring, los
 * módulos de escritura (ranks, diseases) quedan apagados por falta de config/BD: medido en la
 * prueba de arranque del 2026-10-18.</p>
 *
 * <ul>
 *   <li><b>A (infra-core):</b> los pasos 1-6 del §8 dentro de {@code InfraCore}.</li>
 *   <li><b>B (potencias):</b> {@link PotenciasModule#enable} (listeners, tareas, publica IPotenciasService).</li>
 *   <li><b>C (enfermedades):</b> {@link DiseasesModule#enable}.</li>
 *   <li><b>D (ui-comandos-import):</b> {@link CommandsModule#enable} (comandos reales; su registro
 *       pisa el executor provisional de abajo).</li>
 * </ul>
 */
public final class PluginBootstrap {

    /** Poda de auditoría (DECISION D17-D20): primer disparo a los 30 s, luego cada 24 h. */
    private static final long RETARDO_AUDITORIA_TICKS = 20L * 30L;
    private static final long PERIODO_AUDITORIA_TICKS = 20L * 60L * 60L * 24L;

    private final JavaPlugin plugin;
    private PluginContextImpl contexto;
    private ModuleRegistry registro;
    private InfraCore.Instalacion infra;
    private BukkitTask tareaAuditoria;

    public PluginBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Orden de enable (spec §8): contexto → infra-core (config, mensajes, hooks, SQLite+migraciones,
     * auditoría, listener core) → executor provisional de /mushoku → módulos en orden fijo →
     * informe de módulos.
     * @throws Exception si algo irreparable falla; {@code MushokuCorePlugin} la captura y
     *                   deshabilita el plugin (spec §8 L370-375).
     */
    public void enable() throws Exception {
        contexto = new PluginContextImpl(plugin);

        // Spec §8 pasos 1-6 (dueño: agente A; wiring: integrador): config → mensajes → hooks
        // opcionales (floodgate/luckperms) → SQLite + migraciones → auditoría → listener core.
        // Si el config.yml del operador no es schema 2 no se abre SQLite y los módulos de
        // escritura quedan apagados (fallo seguro §2.5): el resto de los getters siguen vivos.
        infra = InfraCore.instalar(plugin, contexto);

        registrarComandoMushokuProvisional();

        registro = new ModuleRegistry(plugin.getLogger());
        registro.registrar(new PotenciasModule());           // dueño: agente B
        registro.registrar(new DiseasesModule());            // dueño: agente C
        registro.registrar(new FactionsModule());
        registro.registrar(new RegionalDifficultyModule());  // escalado regional de dificultad
        registro.registrar(new MarketModule());              // economia por diamantes
        registro.registrar(new NpcsModule());                // vecinos con skin y dialogo
        registro.registrar(new CommandsModule());            // dueño: agente D — último (§6.3)
        List<String> activos = registro.habilitarTodos(contexto);

        // spec §8 paso 11: informar módulos activos e inactivos.
        plugin.getLogger().info("Modulos habilitados: " + activos);
        if (infra != null && !infra.esquemaValido()) {
            plugin.getLogger().warning("Modulos de escritura apagados: config.yml sin schema 2.");
        }

        // Poda diaria de audit_log (DECISION D17-D20). Es transversal a los módulos: la agenda
        // el bootstrap sobre la BD de infra-core.
        programarAuditoria();
    }

    /** Cierre (spec §8 L351-355): deshabilita módulos en orden inverso; luego vacía colas y cierra la BD. */
    public void disable() {
        cancelarAuditoria();
        if (registro != null) {
            registro.deshabilitarTodos();
        }
        // §8 L353-354: flushAll() (con su propio try/catch interno) y cierre de la base.
        InfraCore.cerrar(infra);
    }

    /**
     * Agenda {@link AuditArchiveTask} cada 24 h (DECISION D17-D20). Sin BD (config sin schema 2)
     * o con {@code audit.retention-days <= 0} no se agenda nada (fallo seguro §2.5). El registro
     * en el scheduler va en try/catch: en onDisable el plugin ya está deshabilitado y el scheduler
     * lanza {@code IllegalPluginAccessException} (RuntimeException) — no debe tumbar el arranque.
     */
    private void programarAuditoria() {
        if (infra == null || infra.db() == null) {
            return; // sin BD no hay audit_log que podar
        }
        int dias;
        try {
            dias = contexto.config().auditRetencionDias();
        } catch (IllegalStateException sinConfig) {
            plugin.getLogger().warning("Auditoria: config sin proveer; no se agenda la poda de audit_log.");
            return;
        }
        if (dias <= 0) {
            plugin.getLogger().info("Auditoria: audit.retention-days=" + dias + "; poda desactivada.");
            return;
        }
        AuditArchiveTask tarea = new AuditArchiveTask(plugin, new AuditRepository(infra.db()), dias,
                plugin.getLogger());
        try {
            tareaAuditoria = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                    plugin, tarea, RETARDO_AUDITORIA_TICKS, PERIODO_AUDITORIA_TICKS);
            plugin.getLogger().info("Auditoria: poda de audit_log cada 24 h (retencion "
                    + dias + " dias).");
        } catch (RuntimeException apagando) {
            plugin.getLogger().log(Level.WARNING,
                    "Auditoria: no se pudo agendar la poda de audit_log.", apagando);
        }
    }

    private void cancelarAuditoria() {
        if (tareaAuditoria != null) {
            tareaAuditoria.cancel();
            tareaAuditoria = null;
        }
    }

    /**
     * [A MEDIAS — Fase 1, agente D; retiro: integrador Fase 2]
     * Executor provisional para que {@code /mushoku} responda desde el scaffold de Fase 0
     * (sin él, el comando declarado en plugin.yml no tendría executor).
     * Se registra <b>antes</b> de habilitar los módulos: cuando D registre
     * {@code MushokuCommand} desde {@code CommandsModule.enable()}, este queda pisado y no
     * hay doble registro.
     */
    private void registrarComandoMushokuProvisional() {
        contexto.registrarComando("mushoku", (sender, command, label, args) -> {
            sender.sendMessage("MushokuCore " + plugin.getPluginMeta().getVersion()
                    + " — scaffold Fase 0. Comandos en construccion (agente D).");
            return true;
        }, null);
    }
}
