package ar.com.maydana.mushokucore.database.repository;

import ar.com.maydana.mushokucore.database.IDatabase;
import ar.com.maydana.mushokucore.util.Uuids;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Tabla {@code boss_damage} (spec §10 L484-491) — agente A (infra-core).
 * Acumula daño por (jefe, jugador). El <b>tope de overkill y la resolución de
 * proyectiles/mascotas son lógica de B</b> (spec §11 L558-575): B calcula el daño
 * neto a aplicar y llama a {@link #sumarDanio}. El flush del buffer y el borrado
 * tras asignar Potencia también los decide B (§19 L863-864).
 */
public final class BossDamageRepository {

    /** Fila del ranking de un jefe. */
    public record FilaDanio(UUID jugador, double danio, String ultimoGolpe) {
    }

    private final IDatabase db;

    public BossDamageRepository(IDatabase db) {
        this.db = db;
    }

    /** Acumula daño (suma, no pisa). Upsert: el primer golpe crea la fila. */
    public CompletableFuture<Void> sumarDanio(String bossUuid, String bossTipo,
                                              UUID jugador, double danio, String ahoraIso) {
        return db.write(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "INSERT INTO boss_damage (boss_uuid, boss_type, player_uuid, damage, last_hit_at) "
                            + "VALUES (?,?,?,?,?) ON CONFLICT(boss_uuid, player_uuid) DO UPDATE SET "
                            + "damage=damage+excluded.damage, last_hit_at=excluded.last_hit_at")) {
                preparada.setString(1, bossUuid);
                preparada.setString(2, bossTipo);
                preparada.setString(3, jugador.toString());
                preparada.setDouble(4, danio);
                preparada.setString(5, ahoraIso);
                preparada.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /** Ranking de daño contra un jefe, de mayor a menor. */
    public CompletableFuture<List<FilaDanio>> ranking(String bossUuid) {
        return db.read(conexion -> {
            List<FilaDanio> resultado = new ArrayList<>();
            try (var preparada = conexion.prepareStatement(
                    "SELECT player_uuid, damage, last_hit_at FROM boss_damage "
                            + "WHERE boss_uuid=? ORDER BY damage DESC")) {
                preparada.setString(1, bossUuid);
                try (var filas = preparada.executeQuery()) {
                    while (filas.next()) {
                        resultado.add(new FilaDanio(
                                UUID.fromString(filas.getString(1)),
                                filas.getDouble(2), filas.getString(3)));
                    }
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
            return resultado;
        });
    }

    /** Borra todo el daño de un jefe (tras asignar la Potencia o si el jefe desaparece). */
    public CompletableFuture<Void> borrarJefe(String bossUuid) {
        return db.write(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "DELETE FROM boss_damage WHERE boss_uuid=?")) {
                preparada.setString(1, bossUuid);
                preparada.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }
}
