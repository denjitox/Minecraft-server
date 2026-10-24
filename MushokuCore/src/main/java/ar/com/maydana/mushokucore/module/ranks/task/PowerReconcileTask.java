package ar.com.maydana.mushokucore.module.ranks.task;

import ar.com.maydana.mushokucore.database.repository.BossDamageRepository;
import ar.com.maydana.mushokucore.module.ranks.ContributionTracker;
import ar.com.maydana.mushokucore.module.ranks.PotenciasService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reconciliación periódica del módulo ranks — spec §11 L588 ("el puesto 5 se recalcula ...
 * mediante una tarea de reconciliación periódica") y plan §5.3 (cada
 * {@code ranks.reconcile-minutes}, 30 min). Dueño: agente B.
 *
 * <p>Hace dos cosas, ambas idempotentes:</p>
 * <ol>
 *   <li>recalcula el puesto 5 (Dios de la Muerte) con el ranking de bajas válidas
 *       ({@link PotenciasService#recalcularPuesto5(String)});</li>
 *   <li>expira peleas contra jefes sin actividad desde
 *       {@code ranks.boss-contribution-expiry-minutes} (spec §9 L436) y borra su registro de
 *       {@code boss_damage} (el jefe se descargó o su evento de muerte se perdió).</li>
 * </ol>
 *
 * <p><b>Hilo:</b> se agenda con {@code runTaskTimerAsynchronously}; toda la I/O de BD queda
 * fuera del hilo principal (§26 L1039).</p>
 */
public final class PowerReconcileTask implements Runnable {

    private final PotenciasService servicio;
    private final ContributionTracker tracker;
    private final BossDamageRepository bossDamage;
    private final int expiracionMinutos;
    private final Logger log;

    public PowerReconcileTask(PotenciasService servicio, ContributionTracker tracker,
                              BossDamageRepository bossDamage, int expiracionMinutos, Logger log) {
        this.servicio = servicio;
        this.tracker = tracker;
        this.bossDamage = bossDamage;
        this.expiracionMinutos = expiracionMinutos;
        this.log = log;
    }

    @Override
    public void run() {
        try {
            servicio.recalcularPuesto5("reconciliacion");
            expirarPeleasViejas();
        } catch (RuntimeException excepcion) {
            // Ninguna tarea periódica puede tumbar al scheduler (§26 L1037).
            log.log(Level.WARNING, "Potencias: la reconciliacion periodica fallo; "
                    + "se reintenta en el proximo ciclo.", excepcion);
        }
    }

    private void expirarPeleasViejas() {
        if (expiracionMinutos <= 0) {
            return; // 0/negativo = expiración desactivada [config §9 L436]
        }
        List<UUID> expiradas;
        try {
            expiradas = tracker.expirarInactivas(Instant.now(), Duration.ofMinutes(expiracionMinutos));
        } catch (RuntimeException excepcion) {
            log.log(Level.WARNING, "Potencias: fallo al expirar peleas inactivas.", excepcion);
            return;
        }
        for (UUID boss : expiradas) {
            log.info("Potencias: pelea del jefe " + boss + " expirada por inactividad ("
                    + expiracionMinutos + " min); se borra su registro de boss_damage.");
            bossDamage.borrarJefe(boss.toString()).whenComplete((vacio, fallo) -> {
                if (fallo != null) {
                    log.log(Level.WARNING, "Potencias: no se pudo borrar el boss_damage de "
                            + boss + " al expirar la pelea.", fallo);
                }
            });
        }
    }
}
