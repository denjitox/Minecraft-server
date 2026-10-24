package ar.com.maydana.mushokucore.database.repository;

import ar.com.maydana.mushokucore.database.IDatabase;
import ar.com.maydana.mushokucore.model.Allegiance;
import ar.com.maydana.mushokucore.module.factions.TownAllegiance;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class TownAllegianceRepository {

    private final IDatabase db;

    public TownAllegianceRepository(IDatabase db) {
        this.db = db;
    }

    public CompletableFuture<List<TownAllegiance>> all() {
        return db.read(connection -> {
            List<TownAllegiance> result = new ArrayList<>();
            try (var statement = connection.prepareStatement(
                    "SELECT town_uuid, town_name, allegiance, joined_at, locked_until, deserter_until "
                            + "FROM town_allegiances")) {
                try (var rows = statement.executeQuery()) {
                    while (rows.next()) {
                        result.add(new TownAllegiance(
                                java.util.UUID.fromString(rows.getString(1)), rows.getString(2),
                                Allegiance.valueOf(rows.getString(3)), Instant.parse(rows.getString(4)),
                                instantOrNull(rows.getString(5)), instantOrNull(rows.getString(6))));
                    }
                }
            } catch (java.sql.SQLException exception) {
                throw Sql.reenviar(exception);
            }
            return result;
        });
    }

    public CompletableFuture<Void> save(TownAllegiance state, Instant updatedAt) {
        return db.write(connection -> {
            try (var statement = connection.prepareStatement(
                    "INSERT INTO town_allegiances "
                            + "(town_uuid,town_name,allegiance,joined_at,locked_until,deserter_until,updated_at) "
                            + "VALUES (?,?,?,?,?,?,?) ON CONFLICT(town_uuid) DO UPDATE SET "
                            + "town_name=excluded.town_name, allegiance=excluded.allegiance, "
                            + "joined_at=excluded.joined_at, locked_until=excluded.locked_until, "
                            + "deserter_until=excluded.deserter_until, updated_at=excluded.updated_at")) {
                statement.setString(1, state.townId().toString());
                statement.setString(2, state.townName());
                statement.setString(3, state.allegiance().name());
                statement.setString(4, state.joinedAt().toString());
                statement.setString(5, textOrNull(state.lockedUntil()));
                statement.setString(6, textOrNull(state.deserterUntil()));
                statement.setString(7, updatedAt.toString());
                statement.executeUpdate();
            } catch (java.sql.SQLException exception) {
                throw Sql.reenviar(exception);
            }
        });
    }

    private static Instant instantOrNull(String value) {
        return value == null ? null : Instant.parse(value);
    }

    private static String textOrNull(Instant value) {
        return value == null ? null : value.toString();
    }
}
