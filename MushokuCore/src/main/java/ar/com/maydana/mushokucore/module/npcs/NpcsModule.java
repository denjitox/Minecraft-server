package ar.com.maydana.mushokucore.module.npcs;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.database.repository.NpcRepository;
import ar.com.maydana.mushokucore.module.ModuleLifecycle;
import org.bukkit.Bukkit;

/**
 * Modulo de vecinos: subconjunto server-side de MCA que si es viable en Paper con
 * crossplay.
 *
 * <ul>
 *   <li>dialogo por click derecho o por bloque ancla,</li>
 *   <li>regalos con cooldown y reputacion persistida,</li>
 *   <li>trueques por GUI o por comando,</li>
 *   <li>nombre y skin desde {@code npcs.yml},</li>
 *   <li>rutina liviana de mirada.</li>
 * </ul>
 *
 * <p>Matrimonio, hijos y genetica quedan fuera: dependen del mod MCA del cliente y no
 * funcionan en Bedrock.</p>
 */
public final class NpcsModule implements ModuleLifecycle {

    private NpcsService servicio;
    private NpcRoutineTask rutina;

    @Override
    public String id() {
        return "npcs";
    }

    @Override
    public void enable(PluginContext ctx) throws Exception {
        if (!ctx.config().moduloNpcs()) {
            ctx.plugin().getLogger().info("Modulo 'npcs' apagado en config.yml "
                    + "(modules.npcs=false): no se registra nada.");
            return;
        }
        NpcRepository repositorio = new NpcRepository(ctx.db());
        NpcReputationService reputacion = new NpcReputationService(repositorio);
        NpcEntityFactory fabrica = new NpcEntityFactory(ctx.plugin());
        servicio = new NpcsService(ctx, repositorio, reputacion, fabrica);

        int definidos = servicio.cargar();
        ctx.registrarListener(new NpcListener(servicio));
        VecinoCommand comando = new VecinoCommand(ctx, servicio);
        ctx.registrarComando("vecino", comando, comando);

        // El spawn se difiere un tick: los mundos ya estan cargados y los chunks de las
        // ubicaciones se resuelven igual por ChunkLoadEvent si no lo estan.
        Bukkit.getScheduler().runTask(ctx.plugin(), servicio::spawnearTodos);

        int segundos = ctx.config().npcsRutinaSegundos();
        if (segundos > 0) {
            rutina = new NpcRoutineTask(servicio);
            rutina.iniciar(ctx.plugin(), segundos);
        }

        if (definidos == 0) {
            ctx.plugin().getLogger().warning("Modulo 'npcs' habilitado sin definiciones: "
                    + "revisar npcs.yml antes de colocar vecinos.");
        } else {
            ctx.plugin().getLogger().info("Modulo 'npcs' habilitado: " + definidos
                    + " definicion(es), " + servicio.ubicados() + " ubicado(s), rutina cada "
                    + segundos + "s.");
        }
    }

    @Override
    public void disable() {
        if (rutina != null) {
            rutina.cancelar();
            rutina = null;
        }
        if (servicio != null) {
            servicio.detener();
            servicio = null;
        }
    }
}
