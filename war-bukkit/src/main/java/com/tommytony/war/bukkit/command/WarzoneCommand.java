package com.tommytony.war.bukkit.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.zone.Warzone;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public class WarzoneCommand implements TabExecutor {
    private final WarPlugin plugin;

    public WarzoneCommand(WarPlugin warPlugin) {
        this.plugin = warPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            StringBuilder zones = new StringBuilder();
            boolean first = true;
            for (String zoneName : plugin.getZones().keySet()) {
                if (!first) {
                    zones.append(", ");
                }
                first = false;
                zones.append(zoneName);
            }
            sender.sendMessage("Warzones: " + zones.toString());
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can't be used by console.");
            return true;
        }
        String zoneName = args[0];
        Warzone zone = plugin.getZone(zoneName);
        if (zone == null) {
            sender.sendMessage("Can't find zone " + zoneName);
            return false;
        }
        Player player = (Player) sender;
        player.teleport(plugin.getBukkitLocation(zone.getTeleport()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<String> list = ImmutableList.builder();
        plugin.getZones().values().stream().filter(zone -> zone.getName().toLowerCase().startsWith(args[0].toLowerCase())).forEach(zone -> list.add(zone.getName()));
        return list.build();
    }
}
