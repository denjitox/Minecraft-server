package ar.com.maydana.mushokucore.database.repository;

import ar.com.maydana.mushokucore.database.IDatabase;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Tablas {@code npc_reputation} y {@code npc_gifts} (migracion V003).
 *
 * <p>La reputacion se cachea en {@code NpcReputationService}; esta clase solo persiste.
 * El ultimo regalo se consulta antes de entregar para aplicar el cooldown aunque el
 * servidor se haya reiniciado.</p>
 */
public final class NpcRepository {

    private final IDatabase db;

    public NpcRepository(IDatabase db) {
        this.db = db;
    }

    public CompletableFuture<Integer> reputacion(UUID jugador, String npcId) {
        return db.read(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "SELECT value FROM npc_reputation WHERE player_uuid=? AND npc_id=?")) {
                preparada.setString(1, jugador.toString());
                preparada.setString(2, npcId);
                try (var filas = preparada.executeQuery()) {
                    return filas.next() ? filas.getInt(1) : 0;
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    public CompletableFuture<Map<String, Integer>> todas(UUID jugador) {
        return db.read(conexion -> {
            Map<String, Integer> resultado = new HashMap<>();
            try (var preparada = conexion.prepareStatement(
                    "SELECT npc_id, value FROM npc_reputation WHERE player_uuid=?")) {
                preparada.setString(1, jugador.toString());
                try (var filas = preparada.executeQuery()) {
                    while (filas.next()) {
                        resultado.put(filas.getString(1), filas.getInt(2));
                    }
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
            return resultado;
        });
    }

    public CompletableFuture<Void> fijar(UUID jugador, String npcId, int valor, String ahoraIso) {
        return db.write(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "INSERT INTO npc_reputation (player_uuid, npc_id, value, updated_at) "
                            + "VALUES (?,?,?,?) ON CONFLICT(player_uuid, npc_id) DO UPDATE SET "
                            + "value=excluded.value, updated_at=excluded.updated_at")) {
                preparada.setString(1, jugador.toString());
                preparada.setString(2, npcId);
                preparada.setInt(3, valor);
                preparada.setString(4, ahoraIso);
                preparada.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /** ISO del ultimo regalo de ese tipo, o null si nunca se entrego. */
    public CompletableFuture<String> ultimoRegalo(UUID jugador, String npcId, String regaloId) {
        return db.read(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "SELECT last_given_at FROM npc_gifts "
                            + "WHERE player_uuid=? AND npc_id=? AND gift_id=?")) {
                preparada.setString(1, jugador.toString());
                preparada.setString(2, npcId);
                preparada.setString(3, regaloId);
                try (var filas = preparada.executeQuery()) {
                    return filas.next() ? filas.getString(1) : null;
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    public CompletableFuture<Void> registrarRegalo(UUID jugador, String npcId, String regaloId,
                                                   String ahoraIso) {
        return db.write(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "INSERT INTO npc_gifts (player_uuid, npc_id, gift_id, last_given_at) "
                            + "VALUES (?,?,?,?) ON CONFLICT(player_uuid, npc_id, gift_id) DO UPDATE SET "
                            + "last_given_at=excluded.last_given_at")) {
                preparada.setString(1, jugador.toString());
                preparada.setString(2, npcId);
                preparada.setString(3, regaloId);
                preparada.setString(4, ahoraIso);
                preparada.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }
}
