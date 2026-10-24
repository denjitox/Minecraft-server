package ar.com.maydana.mushokucore.module.factions;

import ar.com.maydana.mushokucore.config.Messages;
import ar.com.maydana.mushokucore.model.Allegiance;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class AlianzaCommand implements CommandExecutor, TabCompleter {

    private final FactionsService service;
    private final Messages messages;

    public AlianzaCommand(FactionsService service, Messages messages) {
        this.service = service;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            return admin(sender, args);
        }
        if (!sender.hasPermission("mushoku.factions.use")) {
            sender.sendMessage(messages.texto("generic.no-permission"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messages.texto("factions.players-only"));
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            sender.sendMessage(messages.texto("factions.alliance-usage"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("elegir")) {
            Allegiance requested = FactionsService.parse(args[1]);
            if (requested == null || requested == Allegiance.NEUTRAL) {
                sender.sendMessage(messages.texto("factions.invalid-alliance"));
            } else {
                service.choose(player, requested, result -> send(sender, result));
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("confirmar")) {
            service.confirm(player, result -> send(sender, result));
        } else {
            sender.sendMessage(messages.texto("factions.alliance-usage"));
        }
        return true;
    }

    private boolean admin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mushoku.admin")) {
            sender.sendMessage(messages.texto("generic.no-permission"));
            return true;
        }
        if (args.length != 4 || !args[1].equalsIgnoreCase("establecer")) {
            sender.sendMessage(messages.texto("factions.admin-usage"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        Allegiance requested = FactionsService.parse(args[3]);
        if (target == null) {
            sender.sendMessage(messages.texto("generic.player-not-found", java.util.Map.of("jugador", args[2])));
        } else if (requested == null) {
            sender.sendMessage(messages.texto("factions.invalid-alliance"));
        } else {
            java.util.UUID actor = sender instanceof Player player ? player.getUniqueId() : null;
            service.adminSet(actor, target, requested, result -> send(sender, result));
        }
        return true;
    }

    private void send(CommandSender sender, FactionsService.Result result) {
        sender.sendMessage(messages.texto(result.messageKey(), result.replacements()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.add("info");
            options.add("elegir");
            options.add("confirmar");
            if (sender.hasPermission("mushoku.admin")) {
                options.add("admin");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("elegir")) {
            options.add("orsted");
            options.add("apostoles");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            options.add("establecer");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            Bukkit.getOnlinePlayers().forEach(player -> options.add(player.getName()));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("admin")) {
            options.add("orsted");
            options.add("apostoles");
            options.add("neutral");
        }
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return options.stream().filter(value -> value.startsWith(prefix)).toList();
    }
}
