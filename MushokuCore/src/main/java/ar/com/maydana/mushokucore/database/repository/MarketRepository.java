package ar.com.maydana.mushokucore.database.repository;

import ar.com.maydana.mushokucore.database.IDatabase;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Tabla {@code market_purchases} (migracion V003). Registra cada compra para poder
 * auditar el flujo de diamantes; el mercado no maneja saldo virtual.
 */
public final class MarketRepository {

    private final IDatabase db;

    public MarketRepository(IDatabase db) {
        this.db = db;
    }

    public CompletableFuture<Void> registrar(UUID jugador, String itemId, int cantidad,
                                             int precioDiamantes, String ahoraIso) {
        return db.write(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "INSERT INTO market_purchases "
                            + "(player_uuid, item_id, amount, price_diamonds, purchased_at) "
                            + "VALUES (?,?,?,?,?)")) {
                preparada.setString(1, jugador.toString());
                preparada.setString(2, itemId);
                preparada.setInt(3, cantidad);
                preparada.setInt(4, precioDiamantes);
                preparada.setString(5, ahoraIso);
                preparada.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /** Cantidad de compras del jugador desde {@code desdeIso} (diagnostico y limites). */
    public CompletableFuture<Integer> comprasDesde(UUID jugador, String desdeIso) {
        return db.read(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "SELECT COUNT(*) FROM market_purchases "
                            + "WHERE player_uuid=? AND purchased_at>=?")) {
                preparada.setString(1, jugador.toString());
                preparada.setString(2, desdeIso);
                try (var filas = preparada.executeQuery()) {
                    return filas.next() ? filas.getInt(1) : 0;
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }
}
