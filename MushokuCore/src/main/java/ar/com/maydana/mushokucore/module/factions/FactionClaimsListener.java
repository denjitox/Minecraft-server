package ar.com.maydana.mushokucore.module.factions;

import com.palmergames.bukkit.towny.event.TownPreClaimEvent;
import com.palmergames.bukkit.towny.event.town.TownCalculateMaxTownBlocksEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class FactionClaimsListener implements Listener {

    private final FactionsService service;

    public FactionClaimsListener(FactionsService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMaximumCalculated(TownCalculateMaxTownBlocksEvent event) {
        event.setTownBlockCount(service.claimLimit(event.getTown().getNumResidents()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreClaim(TownPreClaimEvent event) {
        int limit = service.claimLimit(event.getTown().getNumResidents());
        if (event.getTown().getNumTownBlocks() >= limit) {
            event.setCancelled(true);
            event.setCancelMessage("MushokuCore: límite absoluto de claims alcanzado (" + limit + ").");
        }
    }
}
