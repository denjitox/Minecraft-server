package ar.com.maydana.mushokucore.database.repository;

import ar.com.maydana.mushokucore.database.IDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tabla {@code audit_log} (spec §10 L525-532) — agente A (infra-core).
 * Acceso crudo a la tabla; el uso conveniente es {@code AuditService} (IAuditoria),
 * que ya despacha al hilo correcto y nunca lanza. Repositorio público para que el
 * {@code AuditArchiveTask} (DECISION D17-D20, 2026-10-18) y los diagnósticos lo usen
 * sin duplicar el SQL.
 *
 * <p><b>Hilo:</b> igual que el resto de {@link IDatabase}, estos métodos solo pueden llamarse
 * fuera del hilo principal (el guard de {@code DatabaseManager} lanza si no).</p>
 */
public final class AuditRepository {

    private final IDatabase db;

    public AuditRepository(IDatabase db) {
        this.db = db;
    }

    /** Una fila cruda de {@code audit_log}, tal como viaja al CSV de archivo. */
    public record Fila(long id, String occurredAt, String actorUuid, String action,
                       String target, String details) {
    }

    /** Insert directo (usar AuditService.registrar salvo que se necesite el futuro). */
    public CompletableFuture<Void> insertar(UUID actor, String accion, String target,
                                            String detalles, String ahoraIso) {
        return db.write(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "INSERT INTO audit_log (occurred_at, actor_uuid, action, target, details) "
                            + "VALUES (?,?,?,?,?)")) {
                preparada.setString(1, ahoraIso);
                preparada.setString(2, actor == null ? null : actor.toString());
                preparada.setString(3, accion);
                preparada.setString(4, target);
                preparada.setString(5, detalles);
                preparada.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /**
     * Filas más viejas que {@code corteIso} (exclusivo), ordenadas cronológicamente.
     * Comparación lexicográfica: los timestamps son ISO-8601 UTC ({@code TimeUtils}), así que el
     * orden de texto coincide con el cronológico [V17].
     *
     * @param corteIso instante de corte ISO-8601; se conservan las filas con {@code occurred_at >= corteIso}.
     */
    public CompletableFuture<List<Fila>> anterioresA(String corteIso) {
        return db.read(conexion -> {
            List<Fila> filas = new ArrayList<>();
            try (var preparada = conexion.prepareStatement(
                    "SELECT id, occurred_at, actor_uuid, action, target, details FROM audit_log "
                            + "WHERE occurred_at < ? ORDER BY occurred_at, id")) {
                preparada.setString(1, corteIso);
                try (var resultado = preparada.executeQuery()) {
                    while (resultado.next()) {
                        filas.add(new Fila(resultado.getLong(1), resultado.getString(2),
                                resultado.getString(3), resultado.getString(4),
                                resultado.getString(5), resultado.getString(6)));
                    }
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
            return filas;
        });
    }

    /**
     * Borra las filas más viejas que {@code corteIso}.
     * @return cuántas filas se borraron.
     */
    public CompletableFuture<Integer> borrarAnterioresA(String corteIso) {
        AtomicInteger borradas = new AtomicInteger();
        return db.write(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "DELETE FROM audit_log WHERE occurred_at < ?")) {
                preparada.setString(1, corteIso);
                borradas.set(preparada.executeUpdate());
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        }).thenApply(vacio -> borradas.get());
    }
}
