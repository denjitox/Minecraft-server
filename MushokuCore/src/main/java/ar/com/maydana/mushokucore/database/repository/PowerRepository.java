package ar.com.maydana.mushokucore.database.repository;

import ar.com.maydana.mushokucore.database.IDatabase;
import ar.com.maydana.mushokucore.util.Uuids;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Tabla {@code world_powers} (spec §10 L473-482) — agente A (infra-core).
 * La asignación es <b>idempotente por revisión</b> (plan §6 regla 5 / spec §11 L575):
 * {@code UPDATE ... WHERE position=? AND (owner_uuid IS NULL OR revision=?)}; 0 filas
 * afectadas = otro proceso ganó la carrera y el llamador aborta sin duplicar.
 */
public final class PowerRepository {

    /** Fila de world_powers. */
    public record Fila(int posicion, String titulo, UUID duenio, String duenioNombre,
                       String adquiridoEn, String motivo, int revision) {
    }

    private final IDatabase db;

    public PowerRepository(IDatabase db) {
        this.db = db;
    }

    /** Los 7 puestos (libres o no). Siempre 7 filas: la V001 las crea sembradas por B. */
    public CompletableFuture<List<Fila>> todos() {
        return db.read(conexion -> {
            List<Fila> resultado = new ArrayList<>(7);
            try (var sentencia = conexion.createStatement();
                 var filas = sentencia.executeQuery(
                         "SELECT position, title, owner_uuid, owner_name, acquired_at, reason, revision "
                                 + "FROM world_powers ORDER BY position")) {
                while (filas.next()) {
                    resultado.add(new Fila(
                            filas.getInt(1), filas.getString(2),
                            Uuids.parse(filas.getString(3)), filas.getString(4),
                            filas.getString(5), filas.getString(6), filas.getInt(7)));
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
            return resultado;
        });
    }

    /**
     * Asigna un puesto si sigue libre ({@code owner_uuid IS NULL}) o si la revisión vista
     * sigue vigente. Devuelve false si otra escritura ganó (0 filas): el llamador aborta.
     */
    public CompletableFuture<Boolean> asignar(int posicion, String titulo, UUID jugador,
                                              String nombreJugador, String motivo,
                                              int revisionEsperada, String ahoraIso) {
        return db.read(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "UPDATE world_powers SET title=?, owner_uuid=?, owner_name=?, acquired_at=?, "
                            + "reason=?, revision=revision+1 "
                            + "WHERE position=? AND (owner_uuid IS NULL OR revision=?)")) {
                preparada.setString(1, titulo);
                preparada.setString(2, jugador.toString());
                preparada.setString(3, nombreJugador);
                preparada.setString(4, ahoraIso);
                preparada.setString(5, motivo);
                preparada.setInt(6, posicion);
                preparada.setInt(7, revisionEsperada);
                return preparada.executeUpdate() > 0;
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /** Libera un puesto con la misma guarda de revisión (idempotente). */
    public CompletableFuture<Boolean> liberar(int posicion, int revisionEsperada, String motivo) {
        return db.read(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "UPDATE world_powers SET owner_uuid=NULL, owner_name=NULL, acquired_at=NULL, "
                            + "reason=?, revision=revision+1 WHERE position=? AND revision=?")) {
                preparada.setString(1, motivo);
                preparada.setInt(2, posicion);
                preparada.setInt(3, revisionEsperada);
                return preparada.executeUpdate() > 0;
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }
}
