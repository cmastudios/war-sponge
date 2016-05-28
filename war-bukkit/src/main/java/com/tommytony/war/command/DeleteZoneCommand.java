package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.WarPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.text.MessageFormat;
import java.util.List;

public class DeleteZoneCommand implements TabExecutor {
    private final WarPlugin plugin;

    public DeleteZoneCommand(WarPlugin warPlugin) {
        this.plugin = warPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            throw new CommandException("Insufficient arguments.");
        }
        String zone = args[0].trim();
        if (!plugin.getZones().containsKey(zone)) {
            throw new CommandException("Failed to find warzone " + zone);
        }
        String output = plugin.deleteZone(zone);
        sender.sendMessage(MessageFormat.format("Deleted warzone {0}, moved data file to {1}.", zone, output));
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
