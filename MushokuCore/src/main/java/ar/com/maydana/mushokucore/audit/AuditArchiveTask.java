package ar.com.maydana.mushokucore.audit;

import ar.com.maydana.mushokucore.database.repository.AuditRepository;
import ar.com.maydana.mushokucore.util.TimeUtils;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Poda de {@code audit_log} — DECISION D17-D20 (2026-10-18). Corre <b>una vez por día</b> en un
 * hilo asíncrono (lo agenda {@code PluginBootstrap}).
 *
 * <p><b>Qué hace:</b> archiva a CSV las filas de {@code audit_log} con {@code occurred_at} más
 * viejo que {@code audit.retention-days} (7 por default) y recién después las borra. El orden
 * importa: si el CSV falla, las filas NO se borran (el futuro encadenado queda fallido y se
 * saltea el DELETE). El archivo va a {@code plugins/MushokuCore/auditoria/audit_<marca>.csv}
 * (UTF-8, cabecera con los nombres de columna).</p>
 *
 * <p><b>Hilos:</b> Toda la I/O de BD aparece dentro de {@code IDatabase.read}/{@code write}, así
 * que nunca toca el hilo principal (guard §26 L1039). El {@code run()} se agenda con
 * {@code runTaskTimerAsynchronously}.</p>
 *
 * <p><b>Fallos:</b> nunca propaga al scheduler; cualquier {@code RuntimeException} al iniciar se
 * loguea y el ciclo se descarta (p. ej. base cerrada durante el apagado).</p>
 */
public final class AuditArchiveTask implements Runnable {

    private final JavaPlugin plugin;
    private final AuditRepository repo;
    private final int retentionDias;
    private final Logger log;

    public AuditArchiveTask(JavaPlugin plugin, AuditRepository repo, int retentionDias, Logger log) {
        this.plugin = plugin;
        this.repo = repo;
        this.retentionDias = retentionDias;
        this.log = log;
    }

    @Override
    public void run() {
        if (retentionDias <= 0) {
            return; // poda desactivada por config (audit.retention-days <= 0)
        }
        String corteIso = Instant.now().minus(Duration.ofDays(retentionDias)).toString();
        try {
            repo.anterioresA(corteIso).thenCompose(filas -> {
                if (filas.isEmpty()) {
                    return CompletableFuture.completedFuture(0);
                }
                Path csv = escribirCsv(filas);
                return repo.borrarAnterioresA(corteIso).thenApply(borradas -> {
                    log.info("Auditoria: " + borradas + " fila(s) de audit_log mas viejas que "
                            + retentionDias + " dias archivadas en " + csv.getFileName()
                            + " y borradas de la base.");
                    return borradas;
                });
            }).whenComplete((borradas, fallo) -> {
                if (fallo != null) {
                    log.log(Level.WARNING, "Auditoria: fallo la poda de audit_log "
                            + "(las filas NO se borran si el CSV no se pudo escribir).", fallo);
                }
            });
        } catch (RuntimeException fallo) {
            // p. ej. IDatabase ya cerrada durante el apagado: no se propaga al scheduler.
            log.log(Level.WARNING, "Auditoria: no se pudo iniciar la poda de audit_log", fallo);
        }
    }

    private Path escribirCsv(List<AuditRepository.Fila> filas) {
        try {
            Path carpeta = plugin.getDataFolder().toPath().resolve("auditoria");
            Files.createDirectories(carpeta);
            Path destino = carpeta.resolve("audit_" + TimeUtils.marcaArchivo() + ".csv");
            try (Writer salida = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
                salida.write("id,occurred_at,actor_uuid,action,target,details\n");
                for (AuditRepository.Fila fila : filas) {
                    salida.write(campo(String.valueOf(fila.id())));
                    salida.write(',');
                    salida.write(campo(fila.occurredAt()));
                    salida.write(',');
                    salida.write(campo(fila.actorUuid()));
                    salida.write(',');
                    salida.write(campo(fila.action()));
                    salida.write(',');
                    salida.write(campo(fila.target()));
                    salida.write(',');
                    salida.write(campo(fila.details()));
                    salida.write('\n');
                }
            }
            return destino;
        } catch (IOException excepcion) {
            throw new CompletionException(excepcion);
        }
    }

    /** Escapa un campo CSV: comillas dobles si trae coma/punto y coma/comilla; saltos → espacio. */
    private static String campo(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.replace('\r', ' ').replace('\n', ' ');
        if (limpio.indexOf(',') >= 0 || limpio.indexOf(';') >= 0 || limpio.indexOf('"') >= 0) {
            return "\"" + limpio.replace("\"", "\"\"") + "\"";
        }
        return limpio;
    }
}
