package ar.com.maydana.mushokucore.database.repository;

import ar.com.maydana.mushokucore.database.IDatabase;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Tablas {@code pvp_score} y {@code pvp_pair_cooldown} (spec §10 L493-505) — agente A.
 * Las reglas de baja válida / anti-farmeo / cooldown de 12 h (spec §11 L577-588) son
 * lógica de B: B consulta {@link #ultimoAsesinatoValido} y decide; acá solo persistimos.
 */
public final class PvpRepository {

    private final IDatabase db;

    public PvpRepository(IDatabase db) {
        this.db = db;
    }

    /** Bajas válidas acumuladas del jugador (placeholder %mushoku_player_valid_kills%). */
    public CompletableFuture<Integer> bajasValidas(UUID jugador) {
        return db.read(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "SELECT valid_kills FROM pvp_score WHERE player_uuid=?")) {
                preparada.setString(1, jugador.toString());
                try (var filas = preparada.executeQuery()) {
                    return filas.next() ? filas.getInt(1) : 0;
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /** Suma una baja válida (upsert). */
    public CompletableFuture<Void> sumarBaja(UUID jugador, String ahoraIso) {
        return db.write(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "INSERT INTO pvp_score (player_uuid, valid_kills, updated_at) VALUES (?,1,?) "
                            + "ON CONFLICT(player_uuid) DO UPDATE SET "
                            + "valid_kills=valid_kills+1, updated_at=excluded.updated_at")) {
                preparada.setString(1, jugador.toString());
                preparada.setString(2, ahoraIso);
                preparada.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /** Última baja válida atacante→víctima, para el cooldown por pareja (§11 L579). */
    public CompletableFuture<Optional<String>> ultimoAsesinatoValido(UUID atacante, UUID victima) {
        return db.read(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "SELECT last_valid_kill_at FROM pvp_pair_cooldown "
                            + "WHERE attacker_uuid=? AND victim_uuid=?")) {
                preparada.setString(1, atacante.toString());
                preparada.setString(2, victima.toString());
                try (var filas = preparada.executeQuery()) {
                    return filas.next() ? Optional.of(filas.getString(1)) : Optional.empty();
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /** Registra (o refresca) el cooldown de la pareja atacante→víctima. */
    public CompletableFuture<Void> registrarAsesinato(UUID atacante, UUID victima, String ahoraIso) {
        return db.write(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "INSERT INTO pvp_pair_cooldown (attacker_uuid, victim_uuid, last_valid_kill_at) "
                            + "VALUES (?,?,?) ON CONFLICT(attacker_uuid, victim_uuid) DO UPDATE SET "
                            + "last_valid_kill_at=excluded.last_valid_kill_at")) {
                preparada.setString(1, atacante.toString());
                preparada.setString(2, victima.toString());
                preparada.setString(3, ahoraIso);
                preparada.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }
}
