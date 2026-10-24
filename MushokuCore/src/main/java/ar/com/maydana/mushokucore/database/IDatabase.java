package ar.com.maydana.mushokucore.database;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * CONTRATO CONGELADO — Fase 0 (agente F0), plan §6.1.
 * <p>Implementación: agente <b>A (infra-core)</b> en {@code DatabaseManager.java}.
 * Este archivo no se edita sin actualizar el plan y avisar al integrador (plan §6, regla 2).</p>
 *
 * <p><b>Reglas del contrato</b> (plan §6 regla 3, spec §10 L449, §19 L860):</p>
 * <ul>
 *   <li>NUNCA se llama desde el hilo principal. Prohibido {@code .join()} / {@code .get()}
 *       en el hilo del servidor: se compone con {@code thenApply}/{@code thenAccept}.</li>
 *   <li>Toda mutación Bukkit (efectos, LuckPerms, mensajes, ítems) vuelve al hilo principal
 *       con {@code BukkitScheduler.runTask}.</li>
 *   <li>En {@code onDisable}, {@link #flushAll()} garantiza que las escrituras pendientes
 *       terminen antes de cerrar (spec §8 L353).</li>
 * </ul>
 *
 * <p>Los fallos viajan dentro del {@code CompletableFuture} o se registran en log; jamás
 * explotan en el llamador (fallo seguro §2.5). La política exacta de errores la fija A
 * al implementar. {@code write} usa la misma cola que {@code read}: orden garantizado.</p>
 */
public interface IDatabase {

    /**
     * Lectura en el hilo de BD dedicado.
     * @param op operación sobre la conexión activa; su retorno viaja al futuro.
     */
    <T> CompletableFuture<T> read(Function<Connection, T> op);

    /**
     * Escritura en la misma cola que {@link #read}: orden garantizado.
     * Las ejecuciones se serializan (una detrás de otra).
     */
    CompletableFuture<Void> write(Consumer<Connection> op);

    /**
     * Transacción. Firma textual del plan §6.1: {@code BiConsumer<Connection, Connection>}
     * donde el segundo parámetro era marcado por el plan como "noop reserva".
     * <p>[A VERIFICAR — plan §6.1, sin decisión registrada] la semántica de las dos
     * Connection la define A al implementar; el contrato conserva la firma sin resolverla.</p>
     */
    CompletableFuture<Void> transaction(BiConsumer<Connection, Connection> op);

    /**
     * Copia de seguridad de la base en {@code backups/}.
     * Usado por {@code /mushoku backupdb} (spec §17 L815) y obligatorio antes de migrar (spec §20 L889).
     * @return la ruta del archivo de backup creado.
     * @throws IOException si no se puede escribir el backup.
     */
    Path backup(String motivo) throws IOException;

    /**
     * true si la base responde. Para {@code /mushoku diagnostico} y salidas de diagnóstico.
     */
    boolean isHealthy();

    /**
     * Vacía las colas de escritura pendientes. Paso 3 de {@code onDisable} (spec §8 L353).
     * Puede lanzar excepciones: quien cierra debe tratarlas y loguear el cierre completo.
     */
    void flushAll() throws Exception;
}
