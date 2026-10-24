package ar.com.maydana.mushokucore.module.ranks.task;

import ar.com.maydana.mushokucore.database.repository.BossDamageRepository;
import ar.com.maydana.mushokucore.module.ranks.ContributionTracker;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Vuelco periódico del buffer de daño a jefes — spec §19 L863 ("no guardar a disco por cada
 * punto de daño; acumular en memoria y realizar escrituras agrupadas seguras"). Dueño:
 * agente B (plan §6.3, tabla §5.3: cada {@code ranks.boss-buffer-flush-seconds}, 5 s).
 *
 * <p>Toma los <b>deltas</b> de {@link ContributionTracker#drenarParaVuelco()} y los empuja al
 * upsert <b>aditivo</b> de {@code boss_damage} ({@link BossDamageRepository#sumarDanio} suma,
 * no pisa): enviar totales duplicaría el daño. La misma tarea sirve de vuelco <b>inmediato</b>
 * ({@link #flushAhora()}) cuando muere un jefe o cambia un puesto (§19 L864); el módulo la
 * inyecta en {@code PotenciasService.setFlushInmediato}.</p>
 *
 * <p><b>Hilo:</b> solo toca la BD y el buffer en memoria; la agenda
 * {@code runTaskTimerAsynchronously} y el vuelco inmediato se despacha desde el servicio con
 * {@code enHiloAsync}, así que {@code IDatabase} nunca ve el hilo principal (§26 L1039).</p>
 */
public final class BossBufferFlushTask implements Runnable {

    private final ContributionTracker tracker;
    private final BossDamageRepository bossDamage;
    private final Logger log;

    public BossBufferFlushTask(ContributionTracker tracker, BossDamageRepository bossDamage, Logger log) {
        this.tracker = tracker;
        this.bossDamage = bossDamage;
        this.log = log;
    }

    @Override
    public void run() {
        flushAhora();
    }

    /**
     * Drena los deltas pendientes de todas las peleas y los encola en {@code boss_damage}.
     * Idempotente por diseño del buffer (lo volcado no se repite); pensado para correr en un
     * hilo asíncrono.
     */
    public void flushAhora() {
        List<ContributionTracker.GolpeAcumulado> pendientes = tracker.drenarParaVuelco();
        if (pendientes.isEmpty()) {
            return;
        }
        for (ContributionTracker.GolpeAcumulado golpe : pendientes) {
            bossDamage.sumarDanio(golpe.bossUuid().toString(), golpe.tipoJefe(), golpe.jugador(),
                    golpe.danio(), golpe.ultimoGolpeIso()).whenComplete((vacio, fallo) -> {
                        if (fallo != null) {
                            log.log(Level.WARNING, "Potencias: no se pudo volcar el dano de "
                                    + golpe.jugador() + " contra " + golpe.bossUuid(), fallo);
                        }
                    });
        }
    }
}
