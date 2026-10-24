package ar.com.maydana.mushokucore.module.diseases;

import ar.com.maydana.mushokucore.bootstrap.PluginContext;
import ar.com.maydana.mushokucore.command.EnfermedadCommand;
import ar.com.maydana.mushokucore.config.PluginConfig;
import ar.com.maydana.mushokucore.listener.DiseaseInfectionListener;
import ar.com.maydana.mushokucore.listener.DiseaseRestoreListener;
import ar.com.maydana.mushokucore.listener.MilkCureListener;
import ar.com.maydana.mushokucore.module.ModuleLifecycle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Módulo de enfermedades — <b>agente C (enfermedades)</b>, Fase 1.
 * Shell creado por F0; propiedad transferida a C (plan §7, tabla §6.3).
 *
 * <p><b>Qué registra</b> (plan §5.1-5.3):</p>
 * <ul>
 *   <li>{@code DiseasesService} — implementa {@code IEnfermedadesService} y lo publica en el
 *       contexto ({@code registrarServicio}).</li>
 *   <li>Listeners: {@code DiseaseInfectionListener} (contagio por daño desde {@code diseases.yml}),
 *       {@code MilkCureListener} (§13 L690), {@code DiseaseRestoreListener} (join/quit, §13 L689).</li>
 *   <li>Comando: {@code /enfermedad aplicar|curar} — {@code EnfermedadCommand}
 *       (§17 L818-819, permiso {@code mushoku.admin.enfermedades}).</li>
 *   <li>Tarea: {@code DiseaseExpiryTask} cada {@code diseases.tick-check-ticks} (§13 L693);
 *       una sola tarea para todos los jugadores, nunca una por jugador por tick (§13 L692).</li>
 * </ul>
 *
 * <p>Si {@code modules.diseases} está apagado no registra nada (spec §8 L351). Los
 * {@code [PENDIENTE D#]/[PENDIENTE V#]} del plan §9/§10 se resuelven con la propuesta del plan
 * y quedan marcados en el código de las clases correspondientes.</p>
 */
public final class DiseasesModule implements ModuleLifecycle {

    private DiseasesService servicio;
    private DiseaseExpiryTask tarea;

    @Override
    public String id() {
        return "diseases";
    }

    @Override
    public void enable(PluginContext ctx) throws Exception {
        if (!ctx.config().moduloDiseases()) {
            ctx.plugin().getLogger().info("Modulo 'diseases' apagado en config.yml "
                    + "(modules.diseases=false): no se registra nada (spec §8 L351).");
            return;
        }
        PluginConfig config = ctx.config();
        servicio = new DiseasesService(ctx.plugin(), ctx.db(), ctx.audit(), ctx.luckperms(),
                ctx.messages(), ctx.plugin().getLogger(),
                config.multiplicadorOrsted(), config.permisoOrsted());
        int cargadas = servicio.cargarCatalogo();
        if (cargadas == 0) {
            throw new IllegalStateException("diseases.yml sin enfermedades validas: "
                    + "el modulo 'diseases' no puede iniciar (revisar el resource).");
        }

        ctx.registrarServicio(IEnfermedadesService.class, servicio);
        ctx.registrarListener(new DiseaseInfectionListener(servicio));
        ctx.registrarListener(new MilkCureListener(servicio));
        ctx.registrarListener(new DiseaseRestoreListener(servicio));

        EnfermedadCommand comando = new EnfermedadCommand(servicio, ctx.messages());
        ctx.registrarComando("enfermedad", comando, comando);

        tarea = new DiseaseExpiryTask(servicio, ctx.plugin(), config.frecuenciaChequeoTicks());
        tarea.iniciar();

        // Cubre /reload del plugin con jugadores ya conectados (en un arranque normal no hay nadie).
        for (Player jugador : Bukkit.getOnlinePlayers()) {
            servicio.cargar(jugador.getUniqueId());
        }
        ctx.plugin().getLogger().info("Modulo 'diseases' habilitado: " + cargadas
                + " enfermedad(es), tarea cada " + config.frecuenciaChequeoTicks() + " ticks.");
    }

    @Override
    public void disable() {
        if (tarea != null) {
            tarea.cancelar();
            tarea = null;
        }
        if (servicio != null) {
            servicio.detener();
            servicio = null;
        }
    }
}
