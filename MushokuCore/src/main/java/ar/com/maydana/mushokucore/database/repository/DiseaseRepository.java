package ar.com.maydana.mushokucore.database.repository;

import ar.com.maydana.mushokucore.database.IDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Tabla {@code diseases} (spec §10 L507-514) — agente A (infra-core).
 * Persistencia de enfermedades; las reglas de contagio/efectos/cura (spec §13) son
 * lógica de C. La expiración por tarea liviana (§13 L693) usa {@link #expirar}.
 */
public final class DiseaseRepository {

    /** Fila de diseases. {@code expiraEn} null = no expira. */
    public record FilaEnfermedad(String diseaseId, String contratadaEn,
                                 String expiraEn, String fuente) {
    }

    private final IDatabase db;

    public DiseaseRepository(IDatabase db) {
        this.db = db;
    }

    /** Aplica o refresca una enfermedad (upsert por PK player+disease). */
    public CompletableFuture<Void> aplicar(UUID jugador, String diseaseId,
                                           String ahoraIso, String expiraEnIso, String fuente) {
        return db.write(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "INSERT INTO diseases (player_uuid, disease_id, contracted_at, expires_at, source) "
                            + "VALUES (?,?,?,?,?) ON CONFLICT(player_uuid, disease_id) DO UPDATE SET "
                            + "contracted_at=excluded.contracted_at, expires_at=excluded.expires_at, "
                            + "source=excluded.source")) {
                preparada.setString(1, jugador.toString());
                preparada.setString(2, diseaseId);
                preparada.setString(3, ahoraIso);
                preparada.setString(4, expiraEnIso);
                preparada.setString(5, fuente);
                preparada.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /** Enfermedades activas del jugador (no expiradas a las {@code ahoraIso} dadas). */
    public CompletableFuture<List<FilaEnfermedad>> activas(UUID jugador, String ahoraIso) {
        return db.read(conexion -> {
            List<FilaEnfermedad> resultado = new ArrayList<>();
            try (var preparada = conexion.prepareStatement(
                    "SELECT disease_id, contracted_at, expires_at, source FROM diseases "
                            + "WHERE player_uuid=? AND (expires_at IS NULL OR expires_at>?)")) {
                preparada.setString(1, jugador.toString());
                preparada.setString(2, ahoraIso);
                try (var filas = preparada.executeQuery()) {
                    while (filas.next()) {
                        resultado.add(new FilaEnfermedad(
                                filas.getString(1), filas.getString(2),
                                filas.getString(3), filas.getString(4)));
                    }
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
            return resultado;
        });
    }

    /** @return cuántas filas borró (0 = nada que curar). diseaseId null = todas. */
    public CompletableFuture<Integer> curar(UUID jugador, String diseaseId) {
        return db.read(conexion -> {
            try {
                if (diseaseId == null) {
                    try (var preparada = conexion.prepareStatement(
                            "DELETE FROM diseases WHERE player_uuid=?")) {
                        preparada.setString(1, jugador.toString());
                        return preparada.executeUpdate();
                    }
                }
                try (var preparada = conexion.prepareStatement(
                        "DELETE FROM diseases WHERE player_uuid=? AND disease_id=?")) {
                    preparada.setString(1, jugador.toString());
                    preparada.setString(2, diseaseId);
                    return preparada.executeUpdate();
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /** Borra las filas ya expiradas (tarea liviana §13 L693). @return cantidad borrada. */
    public CompletableFuture<Integer> expirar(String ahoraIso) {
        return db.read(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "DELETE FROM diseases WHERE expires_at IS NOT NULL AND expires_at<=?")) {
                preparada.setString(1, ahoraIso);
                return preparada.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }
}
