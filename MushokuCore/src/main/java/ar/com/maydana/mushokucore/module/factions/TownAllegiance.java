package ar.com.maydana.mushokucore.module.factions;

import ar.com.maydana.mushokucore.model.Allegiance;
import java.time.Instant;
import java.util.UUID;

public record TownAllegiance(UUID townId, String townName, Allegiance allegiance,
                             Instant joinedAt, Instant lockedUntil, Instant deserterUntil) {
}
