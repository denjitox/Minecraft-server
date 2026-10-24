package ar.com.maydana.mushokucore.module.factions;

import ar.com.maydana.mushokucore.model.Allegiance;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class FactionsPolicyTest {

    private FactionsPolicyTest() {
    }

    public static void main(String[] args) {
        claimsAreAbsolutelyCapped();
        minimumAppliesOnlyWhenJoining();
        allianceChangePassesThroughNeutral();
        deserterWaitsSevenDays();
        adminBypassesAllBlocks();
        System.out.println("FACTIONS_POLICY_CHECK_OK");
    }

    private static void claimsAreAbsolutelyCapped() {
        require(FactionsPolicy.claimLimit(0, 8, 5, 48) == 8, "base claims");
        require(FactionsPolicy.claimLimit(3, 8, 5, 48) == 23, "member claims");
        require(FactionsPolicy.claimLimit(20, 8, 5, 48) == 48, "absolute cap");
    }

    private static void minimumAppliesOnlyWhenJoining() {
        Instant now = Instant.parse("2026-10-24T12:00:00Z");
        var rejected = FactionsPolicy.decide(null, Allegiance.ORSTED, 2, 3, 8, now, false);
        require(rejected.action() == FactionsPolicy.Action.REJECT, "minimum on join");
        TownAllegiance memberLoss = state(Allegiance.ORSTED, now.minus(Duration.ofDays(20)), null);
        var leaving = FactionsPolicy.decide(memberLoss, Allegiance.APOSTOLES, 2, 3, 8, now, false);
        require(leaving.action() == FactionsPolicy.Action.LEAVE, "member loss keeps current alliance");
    }

    private static void allianceChangePassesThroughNeutral() {
        Instant now = Instant.parse("2026-10-24T12:00:00Z");
        TownAllegiance current = state(Allegiance.ORSTED, now.minus(Duration.ofDays(20)), null);
        var decision = FactionsPolicy.decide(current, Allegiance.APOSTOLES, 4, 3, 8, now, false);
        require(decision.action() == FactionsPolicy.Action.LEAVE, "change must first leave");
    }

    private static void deserterWaitsSevenDays() {
        Instant now = Instant.parse("2026-10-24T12:00:00Z");
        TownAllegiance neutral = state(Allegiance.NEUTRAL, null, now.plus(Duration.ofDays(7)));
        var decision = FactionsPolicy.decide(neutral, Allegiance.ORSTED, 4, 3, 8, now, false);
        require(decision.action() == FactionsPolicy.Action.REJECT && decision.reason().equals("deserter"),
                "deserter cooldown");
    }

    private static void adminBypassesAllBlocks() {
        Instant now = Instant.parse("2026-10-24T12:00:00Z");
        TownAllegiance locked = new TownAllegiance(UUID.randomUUID(), "Test", Allegiance.ORSTED,
                now, now.plus(Duration.ofDays(14)), null);
        var decision = FactionsPolicy.decide(locked, Allegiance.APOSTOLES, 1, 3, 8, now, true);
        require(decision.action() == FactionsPolicy.Action.JOIN, "admin bypass");
    }

    private static TownAllegiance state(Allegiance allegiance, Instant lockedUntil, Instant deserterUntil) {
        Instant joined = Instant.parse("2026-10-01T00:00:00Z");
        return new TownAllegiance(UUID.randomUUID(), "Test", allegiance, joined, lockedUntil, deserterUntil);
    }

    private static void require(boolean condition, String label) {
        if (!condition) {
            throw new AssertionError(label);
        }
    }
}
