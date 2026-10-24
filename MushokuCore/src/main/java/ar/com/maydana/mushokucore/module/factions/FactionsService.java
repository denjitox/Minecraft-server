package ar.com.maydana.mushokucore.module.factions;

import ar.com.maydana.mushokucore.audit.IAuditoria;
import ar.com.maydana.mushokucore.config.PluginConfig;
import ar.com.maydana.mushokucore.database.repository.TownAllegianceRepository;
import ar.com.maydana.mushokucore.integration.TownyHook;
import ar.com.maydana.mushokucore.model.Allegiance;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class FactionsService {

    private static final Duration CONFIRMATION_TTL = Duration.ofMinutes(5);

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final TownyHook towny;
    private final TownAllegianceRepository repository;
    private final IAuditoria audit;
    private final Logger log;
    private final Map<UUID, TownAllegiance> states = new ConcurrentHashMap<>();
    private final Map<UUID, PendingChoice> pending = new ConcurrentHashMap<>();
    private volatile boolean ready;
    private volatile boolean stopped;

    public FactionsService(JavaPlugin plugin, PluginConfig config, TownyHook towny,
                           TownAllegianceRepository repository, IAuditoria audit, Logger log) {
        this.plugin = plugin;
        this.config = config;
        this.towny = towny;
        this.repository = repository;
        this.audit = audit;
        this.log = log;
    }

    public void start() {
        async(() -> repository.all().whenComplete((loaded, error) -> main(() -> {
            if (error != null) {
                log.log(Level.SEVERE, "Facciones: no se pudo cargar la caché de alianzas.", error);
                return;
            }
            for (TownAllegiance state : loaded) {
                states.put(state.townId(), state);
            }
            ready = true;
            log.info("Facciones: caché cargada con " + loaded.size() + " pueblo(s).");
        })));
    }

    public void stop() {
        stopped = true;
        pending.clear();
        states.clear();
    }

    public TownyHook.TownView townOf(Player player) {
        return towny.townOf(player);
    }

    public TownAllegiance state(UUID townId) {
        return states.get(townId);
    }

    public int claimLimit(int members) {
        return FactionsPolicy.claimLimit(members, config.claimsBase(), config.claimsPorMiembro(),
                config.claimsMaximos());
    }

    public void choose(Player actor, Allegiance requested, Consumer<Result> reply) {
        if (!available(reply)) {
            return;
        }
        TownyHook.TownView town = requireMayor(actor, reply);
        if (town == null || !fixedNationsExist(reply)) {
            return;
        }
        TownAllegiance current = states.get(town.id());
        if (!townyMatches(town, current, reply)) {
            return;
        }
        Instant now = Instant.now();
        FactionsPolicy.Decision decision = FactionsPolicy.decide(current, requested, town.members(),
                config.miembrosMinimos(), config.miembrosMaximos(), now, false);
        if (decision.action() == FactionsPolicy.Action.REJECT) {
            reply.accept(rejection(decision.reason(), current, now));
            return;
        }
        Allegiance effective = decision.action() == FactionsPolicy.Action.LEAVE
                ? Allegiance.NEUTRAL : requested;
        pending.put(actor.getUniqueId(), new PendingChoice(town.id(), effective, now.plus(CONFIRMATION_TTL)));
        reply.accept(Result.ok("factions.choice-pending", Map.of(
                "pueblo", town.name(), "alianza", display(effective),
                "accion", effective == Allegiance.NEUTRAL ? "pasar a neutral" : "ingresar")));
    }

    public void confirm(Player actor, Consumer<Result> reply) {
        if (!available(reply)) {
            return;
        }
        PendingChoice choice = pending.remove(actor.getUniqueId());
        if (choice == null || Instant.now().isAfter(choice.expiresAt())) {
            reply.accept(Result.fail("factions.no-pending"));
            return;
        }
        TownyHook.TownView town = requireMayor(actor, reply);
        if (town == null || !town.id().equals(choice.townId())) {
            reply.accept(Result.fail("factions.town-changed"));
            return;
        }
        apply(actor.getUniqueId(), town, choice.allegiance(), false, reply);
    }

    public void adminSet(UUID actor, Player target, Allegiance requested, Consumer<Result> reply) {
        if (!available(reply)) {
            return;
        }
        TownyHook.TownView town = towny.townOf(target);
        if (town == null) {
            reply.accept(Result.fail("factions.no-town"));
            return;
        }
        if (requested != Allegiance.NEUTRAL && !fixedNationsExist(reply)) {
            return;
        }
        apply(actor, town, requested, true, reply);
    }

    private void apply(UUID actor, TownyHook.TownView town, Allegiance requested, boolean admin,
                       Consumer<Result> reply) {
        Instant now = Instant.now();
        TownAllegiance current = states.get(town.id());
        FactionsPolicy.Decision decision = FactionsPolicy.decide(current, requested, town.members(),
                config.miembrosMinimos(), config.miembrosMaximos(), now, admin);
        if (decision.action() == FactionsPolicy.Action.REJECT) {
            reply.accept(rejection(decision.reason(), current, now));
            return;
        }
        String nation = nationName(requested);
        TownyHook.ChangeResult townyChange = towny.changeNation(town.id(), nation);
        if (!townyChange.changed()) {
            reply.accept(Result.fail("factions.towny-error", Map.of("error", townyChange.error())));
            return;
        }
        TownAllegiance next = nextState(town, requested, now, admin);
        async(() -> repository.save(next, now).whenComplete((ignored, error) -> main(() -> {
            if (error != null) {
                boolean restored = towny.restoreNation(town.id(), townyChange.previousNation());
                log.log(Level.SEVERE, "Facciones: SQLite rechazó el cambio; rollback Towny=" + restored, error);
                reply.accept(Result.fail("factions.db-error"));
                return;
            }
            states.put(town.id(), next);
            audit.registrar(actor, "alianza.establecer", town.id().toString(),
                    "pueblo=" + town.name() + ", alianza=" + requested + ", admin=" + admin);
            log.warning("Facciones: espejo LuckPerms pendiente; no se conceden beneficios de alianza. "
                    + "pueblo=" + town.name() + ", alianza=" + requested);
            reply.accept(Result.ok("factions.changed", Map.of(
                    "pueblo", town.name(), "alianza", display(requested))));
        })));
    }

    private TownAllegiance nextState(TownyHook.TownView town, Allegiance requested,
                                     Instant now, boolean admin) {
        if (requested == Allegiance.NEUTRAL) {
            Instant deserterUntil = admin ? null : now.plus(Duration.ofDays(config.diasCooldownDesertor()));
            return new TownAllegiance(town.id(), town.name(), requested, now, null, deserterUntil);
        }
        return new TownAllegiance(town.id(), town.name(), requested, now,
                now.plus(Duration.ofDays(config.diasBloqueoAlianza())), null);
    }

    private TownyHook.TownView requireMayor(Player actor, Consumer<Result> reply) {
        TownyHook.TownView town = towny.townOf(actor);
        if (town == null) {
            reply.accept(Result.fail("factions.no-town"));
            return null;
        }
        if (!actor.getUniqueId().equals(town.mayorId())) {
            reply.accept(Result.fail("factions.mayor-only"));
            return null;
        }
        return town;
    }

    private boolean fixedNationsExist(Consumer<Result> reply) {
        if (!towny.nationExists(config.nacionOrsted())) {
            reply.accept(Result.fail("factions.fixed-nation-missing", Map.of("nacion", config.nacionOrsted())));
            return false;
        }
        if (!towny.nationExists(config.nacionApostoles())) {
            reply.accept(Result.fail("factions.fixed-nation-missing", Map.of("nacion", config.nacionApostoles())));
            return false;
        }
        return true;
    }

    private boolean townyMatches(TownyHook.TownView town, TownAllegiance current, Consumer<Result> reply) {
        String expected = current == null ? null : nationName(current.allegiance());
        if (java.util.Objects.equals(expected, town.nationName())) {
            return true;
        }
        reply.accept(Result.fail("factions.state-mismatch", Map.of(
                "towny", town.nationName() == null ? "neutral" : town.nationName(),
                "mushoku", current == null ? "sin registro" : display(current.allegiance()))));
        return false;
    }

    private boolean available(Consumer<Result> reply) {
        if (stopped || !ready) {
            reply.accept(Result.fail("factions.loading"));
            return false;
        }
        if (!towny.isPresent()) {
            reply.accept(Result.fail("factions.towny-unavailable"));
            return false;
        }
        return true;
    }

    private Result rejection(String reason, TownAllegiance current, Instant now) {
        return switch (reason) {
            case "locked" -> Result.fail("factions.locked", Map.of("fecha", current.lockedUntil().toString()));
            case "deserter" -> Result.fail("factions.deserter", Map.of("fecha", current.deserterUntil().toString()));
            case "too-few-members" -> Result.fail("factions.too-few-members",
                    Map.of("minimo", Integer.toString(config.miembrosMinimos())));
            case "too-many-members" -> Result.fail("factions.too-many-members",
                    Map.of("maximo", Integer.toString(config.miembrosMaximos())));
            case "already-selected", "already-neutral" -> Result.fail("factions.already-selected");
            default -> Result.fail("factions.change-rejected");
        };
    }

    private String nationName(Allegiance allegiance) {
        return switch (allegiance) {
            case ORSTED -> config.nacionOrsted();
            case APOSTOLES -> config.nacionApostoles();
            case NEUTRAL -> null;
        };
    }

    public static Allegiance parse(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "orsted" -> Allegiance.ORSTED;
            case "apostoles", "apóstoles" -> Allegiance.APOSTOLES;
            case "neutral" -> Allegiance.NEUTRAL;
            default -> null;
        };
    }

    public static String display(Allegiance allegiance) {
        return switch (allegiance) {
            case ORSTED -> "Orsted Corp";
            case APOSTOLES -> "Apóstoles del Dios Humano";
            case NEUTRAL -> "Neutral";
        };
    }

    private void async(Runnable task) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    private void main(Runnable task) {
        if (!stopped) {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    private record PendingChoice(UUID townId, Allegiance allegiance, Instant expiresAt) {
    }

    public record Result(boolean success, String messageKey, Map<String, String> replacements) {
        public static Result ok(String key) {
            return new Result(true, key, Map.of());
        }

        public static Result ok(String key, Map<String, String> replacements) {
            return new Result(true, key, replacements);
        }

        public static Result fail(String key) {
            return new Result(false, key, Map.of());
        }

        public static Result fail(String key, Map<String, String> replacements) {
            return new Result(false, key, replacements);
        }
    }
}
