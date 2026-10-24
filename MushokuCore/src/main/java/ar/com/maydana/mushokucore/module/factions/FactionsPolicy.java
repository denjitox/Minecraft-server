package ar.com.maydana.mushokucore.module.factions;

import ar.com.maydana.mushokucore.model.Allegiance;
import java.time.Instant;

public final class FactionsPolicy {

    private FactionsPolicy() {
    }

    public static int claimLimit(int members, int base, int perMember, int absoluteMax) {
        long calculated = (long) base + (long) perMember * Math.max(0, members);
        return (int) Math.min(Math.max(0, calculated), Math.max(0, absoluteMax));
    }

    public static Decision decide(TownAllegiance current, Allegiance requested, int members,
                                  int minimum, int maximum, Instant now, boolean admin) {
        Allegiance actual = current == null ? Allegiance.NEUTRAL : current.allegiance();
        if (admin) {
            return new Decision(requested == Allegiance.NEUTRAL ? Action.LEAVE : Action.JOIN, "admin");
        }
        if (actual == requested) {
            return new Decision(Action.REJECT, "already-selected");
        }
        if (actual != Allegiance.NEUTRAL) {
            if (current.lockedUntil() != null && now.isBefore(current.lockedUntil())) {
                return new Decision(Action.REJECT, "locked");
            }
            return new Decision(Action.LEAVE, "must-pass-neutral");
        }
        if (requested == Allegiance.NEUTRAL) {
            return new Decision(Action.REJECT, "already-neutral");
        }
        if (current != null && current.deserterUntil() != null && now.isBefore(current.deserterUntil())) {
            return new Decision(Action.REJECT, "deserter");
        }
        if (members < minimum) {
            return new Decision(Action.REJECT, "too-few-members");
        }
        if (members > maximum) {
            return new Decision(Action.REJECT, "too-many-members");
        }
        return new Decision(Action.JOIN, "join");
    }

    public enum Action {
        JOIN,
        LEAVE,
        REJECT
    }

    public record Decision(Action action, String reason) {
    }
}
