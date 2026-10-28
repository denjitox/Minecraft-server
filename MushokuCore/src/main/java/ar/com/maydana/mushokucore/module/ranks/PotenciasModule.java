package ar.com.maydana.mushokucore.module.ranks;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.command.PotenciasCommand;
import ar.com.maydana.mushokucore.database.IDatabase;
import ar.com.maydana.mushokucore.database.repository.BossDamageRepository;
import ar.com.maydana.mushokucore.database.repository.PlayerRepository;
import ar.com.maydana.mushokucore.database.repository.PowerRepository;
import ar.com.maydana.mushokucore.database.repository.PvpRepository;
import ar.com.maydana.mushokucore.listener.BossDamageListener;
import ar.com.maydana.mushokucore.listener.PotenciasJoinListener;
import ar.com.maydana.mushokucore.listener.PvPKillListener;
import ar.com.maydana.mushokucore.module.ModuleLifecycle;
import ar.com.maydana.mushokucore.module.ranks.task.BossBufferFlushTask;
import ar.com.maydana.mushokucore.module.ranks.task.LuckPermsOutboxRetryTask;
import ar.com.maydana.mushokucore.module.ranks.task.PowerReconcileTask;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 * Entrada del módulo Siete Potencias — dueño: agente B (plan §6.3). Implementada en Fase 1.
 *
 * <p>Construye y cablea todo el módulo ranks: repositorios (agente A), buffer de daño
 * ({@link ContributionTracker}), outbox de LuckPerms ({@link LuckPermsOutbox}) y servicio
 * ({@link PotenciasService}); registra los listeners (§11 L553/L577), el comando
 * {@code /potencias} (§17 L816-817) y las tres tareas periódicas (§5.3); y publica
 * {@code IPotenciasService} para D y los placeholders.</p>
 *
 * <p><b>Guardas de arranque (fallo seguro §2.5):</b> si {@code enabled=false} o
 * {@code modules.ranks=false} no se registra nada; si {@code ctx.db()} no está provisto
 * (config inválida / wiring incompleto) se loguea SEVERE y no arranca, sin tumbar a los demás
 * módulos.</p>
 */
public final class PotenciasModule implements ModuleLifecycle {

    /** Reintento del outbox de LuckPerms: 60 s = 1200 ticks (§11 L592). */
    private static final long TICKS_OUTBOX = 1200L;

    private PluginContext ctx;
    private ContributionTracker tracker;
    private PotenciasService servicio;
    private BossBufferFlushTask flushTask;
    private BukkitTask tareaFlush;
    private BukkitTask tareaReconcile;
    private BukkitTask tareaOutbox;

    @Override
    public String id() {
        return "ranks";
    }

    @Override
    public void enable(PluginContext ctx) {
        this.ctx = ctx;
        Logger log = ctx.plugin().getLogger();

        boolean habilitado;
        boolean moduloRanks;
        try {
            habilitado = ctx.config().enabled();
            moduloRanks = ctx.config().moduloRanks();
        } catch (IllegalStateException sinConfig) {
            log.log(Level.SEVERE, "Potencias: PluginConfig no disponible (wiring Fase 2 "
                    + "incompleto); el modulo ranks no arranca.", sinConfig);
            return;
        }
        if (!habilitado) {
            log.info("Potencias: plugin deshabilitado (config.enabled=false); modulo ranks inactivo.");
            return;
        }
        if (!moduloRanks) {
            log.info("Potencias: modulo ranks apagado (modules.ranks=false); no se registra nada.");
            return;
        }

        IDatabase db;
        try {
            db = ctx.db();
        } catch (IllegalStateException sinBd) {
            log.log(Level.SEVERE, "Potencias: IDatabase no disponible (config invalida o wiring "
                    + "incompleto); el modulo ranks no arranca.", sinBd);
            return;
        }

        PowerRepository powers = new PowerRepository(db);
        BossDamageRepository bossDamage = new BossDamageRepository(db);
        PvpRepository pvp = new PvpRepository(db);
        PlayerRepository jugadores = new PlayerRepository(db);
        this.tracker = new ContributionTracker();
        LuckPermsOutbox outbox = new LuckPermsOutbox(ctx);
        this.servicio = new PotenciasService(ctx, powers, bossDamage, pvp, tracker, outbox, jugadores);

        // Carga de caché y siembra de world_powers: el servicio despacha a un hilo asíncrono
        // (IDatabase prohíbe el hilo principal, §26 L1039).
        this.servicio.cargarCache();
        this.servicio.cargarNombres();

        // Si el plugin arranca con jugadores online (recarga), no pasan por PlayerJoinEvent:
        // se cachean ahora, en el hilo principal, antes de que cualquier tarea async los lea.
        for (Player enLinea : ctx.plugin().getServer().getOnlinePlayers()) {
            this.servicio.recordarNombre(enLinea.getUniqueId(), enLinea.getName());
        }

        // Vuelco del buffer de daño: periódico e inmediato (§19 L863-864).
        this.flushTask = new BossBufferFlushTask(tracker, bossDamage, log);
        this.servicio.setFlushInmediato(flushTask::flushAhora);

        JavaPlugin plugin = ctx.plugin();
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        long ticksFlush = Math.max(20L, ctx.config().bufferFlushSegundos() * 20L);
        this.tareaFlush = scheduler.runTaskTimerAsynchronously(plugin, flushTask, ticksFlush, ticksFlush);

        PowerReconcileTask reconciliacion = new PowerReconcileTask(servicio, tracker, bossDamage,
                ctx.config().contribucionJefeExpiraMinutos(), log);
        long ticksReconcile = Math.max(1200L, ctx.config().reconciliacionMinutos() * 1200L);
        this.tareaReconcile = scheduler.runTaskTimerAsynchronously(
                plugin, reconciliacion, ticksReconcile, ticksReconcile);

        this.tareaOutbox = scheduler.runTaskTimerAsynchronously(
                plugin, new LuckPermsOutboxRetryTask(outbox), TICKS_OUTBOX, TICKS_OUTBOX);

        // Listeners (§11 L553, L577) y comando (§17 L816-817).
        ctx.registrarListener(new BossDamageListener(ctx, servicio, tracker));
        ctx.registrarListener(new PvPKillListener(ctx, servicio, pvp));
        ctx.registrarListener(new PotenciasJoinListener(servicio));
        PotenciasCommand comando = new PotenciasCommand(ctx);
        ctx.registrarComando("potencias", comando, comando);

        // Publicar la API para D, los placeholders y /mtestado (§6.1).
        ctx.registrarServicio(IPotenciasService.class, servicio);

        avisarMundosDeJefes(log);

        log.info("Potencias: modulo ranks habilitado (flush cada " + ticksFlush
                + " ticks, reconciliacion cada " + ticksReconcile + " ticks, outbox cada "
                + TICKS_OUTBOX + " ticks).");
    }

    @Override
    public void disable() {
        cancelar(tareaFlush);
        cancelar(tareaReconcile);
        cancelar(tareaOutbox);
        if (ctx == null) {
            return;
        }
        // En onDisable Bukkit ya marco el plugin como deshabilitado: el scheduler rechaza
        // CUALQUIER registro nuevo de tareas —incluso runTaskAsynchronously, con
        // IllegalPluginAccessException (RuntimeException, NO IllegalStateException)— y el guard de
        // IDatabase sigue prohibiendo el hilo principal. Por eso aca NO se programa nada, ni se
        // llama a volcarPendientes()/flushAhora(): drenar el buffer y volcar desde el hilo
        // principal perderia los deltas. El vaciado final de las colas de escritura ya encoladas
        // lo hace InfraCore.cerrar(infra) -> db.flushAll() DESPUES de deshabilitar los modulos
        // (wiring Fase 2). Los deltas en memoria aun no volcados (ventana <=
        // ranks.boss-buffer-flush-seconds) no se persisten en este apagado.
        ctx.plugin().getLogger().info("Potencias: tareas canceladas; el vaciado final lo hace "
                + "db.flushAll() en el cierre de infraestructura.");
    }

    /**
     * Aviso de guarda (DECISION D7 2026-10-18: {@code ranks.valid-boss-worlds} quedó en
     * {@code [world]}): si la lista queda vacía, solo vale el mundo principal; el Ender Dragon
     * vive en {@code <world>_the_end}, así que su muerte no asignaría la Potencia 2 salvo que ese
     * mundo se agregue a la lista (no se fabrica una asignación).
     */
    private void avisarMundosDeJefes(Logger log) {
        Set<String> validos = ctx.config().mundosValidosJefes();
        if (validos != null && !validos.isEmpty()) {
            return;
        }
        log.warning("Potencias [DECISION D7]: ranks.valid-boss-worlds esta vacio, asi que solo "
                + "vale el mundo principal '" + ctx.config().mundoNombre() + "' para jefes. El "
                + "Ender Dragon vive en un mundo aparte ('..._the_end'): su primera muerte NO "
                + "asignara la Potencia 2 hasta que se agregue ese mundo a la lista.");
    }

    private static void cancelar(BukkitTask tarea) {
        if (tarea != null) {
            tarea.cancel();
        }
    }
}
