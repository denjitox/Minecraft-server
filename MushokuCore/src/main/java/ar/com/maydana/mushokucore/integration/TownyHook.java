package ar.com.maydana.mushokucore.integration;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class TownyHook {

    public boolean isPresent() {
        return Bukkit.getPluginManager().isPluginEnabled("Towny");
    }

    public TownView townOf(Player player) {
        requireMainThread();
        if (!isPresent()) {
            return null;
        }
        Resident resident = TownyAPI.getInstance().getResident(player);
        Town town = resident == null ? null : resident.getTownOrNull();
        return town == null ? null : view(town);
    }

    public TownView town(UUID townId) {
        requireMainThread();
        Town town = isPresent() ? TownyAPI.getInstance().getTown(townId) : null;
        return town == null ? null : view(town);
    }

    public boolean nationExists(String nationName) {
        requireMainThread();
        return isPresent() && TownyAPI.getInstance().getNation(nationName) != null;
    }

    public ChangeResult changeNation(UUID townId, String nationName) {
        requireMainThread();
        Town town = TownyAPI.getInstance().getTown(townId);
        if (town == null) {
            return ChangeResult.failed("El pueblo ya no existe en Towny.");
        }
        Nation previous = town.getNationOrNull();
        Nation target = nationName == null ? null : TownyAPI.getInstance().getNation(nationName);
        if (nationName != null && target == null) {
            return ChangeResult.failed("La nación fija '" + nationName + "' no existe en Towny.");
        }
        try {
            if (previous != null) {
                town.removeNation();
                previous.save();
            }
            if (target != null) {
                town.setNation(target);
                target.save();
            }
            town.save();
            return new ChangeResult(true, previous == null ? null : previous.getName(), null);
        } catch (Exception exception) {
            restore(town, previous);
            return ChangeResult.failed(exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
    }

    public boolean restoreNation(UUID townId, String nationName) {
        requireMainThread();
        Town town = TownyAPI.getInstance().getTown(townId);
        Nation nation = nationName == null ? null : TownyAPI.getInstance().getNation(nationName);
        if (town == null || (nationName != null && nation == null)) {
            return false;
        }
        return restore(town, nation);
    }

    private boolean restore(Town town, Nation nation) {
        try {
            Nation current = town.getNationOrNull();
            if (current != null) {
                town.removeNation();
                current.save();
            }
            if (nation != null) {
                town.setNation(nation);
                nation.save();
            }
            town.save();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static TownView view(Town town) {
        Nation nation = town.getNationOrNull();
        Resident mayor = town.getMayor();
        return new TownView(town.getUUID(), town.getName(), town.getNumResidents(),
                mayor == null ? null : mayor.getUUID(), nation == null ? null : nation.getName(),
                town.getNumTownBlocks());
    }

    private static void requireMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("La API de Towny solo se usa en el hilo principal.");
        }
    }

    public record TownView(UUID id, String name, int members, UUID mayorId,
                           String nationName, int claimedBlocks) {
    }

    public record ChangeResult(boolean changed, String previousNation, String error) {
        private static ChangeResult failed(String error) {
            return new ChangeResult(false, null, error);
        }
    }
}
