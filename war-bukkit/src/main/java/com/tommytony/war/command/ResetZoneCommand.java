package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import com.tommytony.war.zone.Warzone;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class ResetZoneCommand implements TabExecutor {
    private final WarPlugin plugin;

    public ResetZoneCommand(WarPlugin warPlugin) {
        this.plugin = warPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String zoneName = args[0];
            Warzone zone = plugin.getZone(zoneName);
            if (zone == null) {
                throw new CommandException(String.format("Can't find zone %s", zoneName));
            }
            sender.sendMessage(String.format("Reloading zone %s...", zoneName));
            zone.reset();
            int affected = (int) Math.floor(zone.getCuboid().getSize());
            sender.sendMessage(String.format("Reloaded %d blocks in zone %s.", affected, zoneName));
            return true;
        }
        throw new CommandException("Insufficient arguments.");
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
