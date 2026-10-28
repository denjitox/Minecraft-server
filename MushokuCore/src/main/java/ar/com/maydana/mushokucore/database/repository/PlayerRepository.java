package ar.com.maydana.mushokucore.database.repository;

import ar.com.maydana.mushokucore.database.IDatabase;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Tabla {@code players} (spec §10 L465-471) — agente A (infra-core).
 * Refresh de {@code last_name} en cada join (spec §2.4 L75-77); identidad = UUID.
 * Todas las operaciones pasan por {@link IDatabase}: jamás SQL en hilo principal.
 */
public final class PlayerRepository {

    /** Fila de players. {@code bedrock} se materializa como boolean. */
    public record Fila(UUID uuid, String nombre, String primeraConexion,
                       String ultimaVisita, boolean bedrock) {
    }

    private final IDatabase db;

    public PlayerRepository(IDatabase db) {
        this.db = db;
    }

    /** Upsert de join: crea la fila la primera vez; después refresca nombre/última visita/bedrock. */
    public CompletableFuture<Void> registrarEntrada(UUID uuid, String nombre,
                                                   boolean bedrock, String ahoraIso) {
        return db.write(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "INSERT INTO players (uuid, last_name, first_join_at, last_seen_at, bedrock) "
                            + "VALUES (?,?,?,?,?) ON CONFLICT(uuid) DO UPDATE SET "
                            + "last_name=excluded.last_name, last_seen_at=excluded.last_seen_at, "
                            + "bedrock=excluded.bedrock")) {
                preparada.setString(1, uuid.toString());
                preparada.setString(2, nombre);
                preparada.setString(3, ahoraIso);
                preparada.setString(4, ahoraIso);
                preparada.setInt(5, bedrock ? 1 : 0);
                preparada.executeUpdate();
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /** @return la fila del jugador o vacío si nunca entró. */
    public CompletableFuture<Optional<Fila>> porUuid(UUID uuid) {
        return db.read(conexion -> {
            try (var preparada = conexion.prepareStatement(
                    "SELECT uuid, last_name, first_join_at, last_seen_at, bedrock FROM players WHERE uuid = ?")) {
                preparada.setString(1, uuid.toString());
                try (var filas = preparada.executeQuery()) {
                    if (filas.next()) {
                        return Optional.of(new Fila(
                                UUID.fromString(filas.getString(1)), filas.getString(2),
                                filas.getString(3), filas.getString(4), filas.getInt(5) == 1));
                    }
                    return Optional.empty();
                }
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }

    /**
     * Todos los nombres persistidos (uuid -> last_name). Permite precargar la cache de nombres
     * del plugin sin tocar Bukkit ni el hilo principal.
     */
    public CompletableFuture<Map<UUID, String>> nombresConocidos() {
        return db.read(conexion -> {
            Map<UUID, String> nombres = new HashMap<>();
            try (var preparada = conexion.prepareStatement("SELECT uuid, last_name FROM players");
                 var filas = preparada.executeQuery()) {
                while (filas.next()) {
                    nombres.put(UUID.fromString(filas.getString(1)), filas.getString(2));
                }
                return nombres;
            } catch (java.sql.SQLException excepcion) {
                throw Sql.reenviar(excepcion);
            }
        });
    }
}
