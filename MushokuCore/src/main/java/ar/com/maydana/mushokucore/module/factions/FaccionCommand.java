package ar.com.maydana.mushokucore.module.factions;

import ar.com.maydana.mushokucore.config.Messages;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class FaccionCommand implements CommandExecutor {

    private final FactionsService service;
    private final Messages messages;

    public FaccionCommand(FactionsService service, Messages messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mushoku.factions.use")) {
            sender.sendMessage(messages.texto("generic.no-permission"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.texto("factions.players-only"));
            return true;
        }
        TownyInfo.send(player, service, messages);
        return true;
    }

    private static final class TownyInfo {
        private static void send(Player player, FactionsService service, Messages messages) {
            var town = service.townOf(player);
            if (town == null) {
                player.sendMessage(messages.texto("factions.no-town"));
                return;
            }
            TownAllegiance state = service.state(town.id());
            player.sendMessage(messages.texto("factions.info", Map.of(
                    "pueblo", town.name(),
                    "miembros", Integer.toString(town.members()),
                    "alianza", state == null ? "Neutral" : FactionsService.display(state.allegiance()),
                    "claims", Integer.toString(town.claimedBlocks()),
                    "limite", Integer.toString(service.claimLimit(town.members())))));
        }
    }
}
