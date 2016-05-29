package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConsole;
import com.tommytony.war.zone.Warzone;

import java.text.MessageFormat;
import java.util.List;

public class DeleteZoneCommand extends WarCommand {
    public DeleteZoneCommand(ServerAPI plugin) {
        super(plugin);
    }

    @Override
    void handleCommand(WarConsole sender, String[] args) {
        if (args.length != 1) {
            throw new InvalidArgumentsError();
        }
        String zoneName = args[0];
        Warzone zone = getPlugin().getZone(zoneName);
        if (zone == null) {
            throw new CommandUserError(MessageFormat.format("Warzone {0} not found.", zoneName));
        }
        String output = getPlugin().deleteZone(zoneName);
        sender.sendMessage(MessageFormat.format("Deleted warzone {0}, moved data file to {1}.", zone, output));
    }

    @Override
    List<String> handleTab(WarConsole sender, String[] args) {
        if (args.length == 0) {
            return ImmutableList.copyOf(getPlugin().getZones().keySet());
        } else if (args.length != 1) {
            return ImmutableList.of();
        }
        ImmutableList.Builder<String> list = ImmutableList.builder();
        getPlugin().getZones().values().stream()
                .filter(zone -> zone.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                .forEach(zone -> list.add(zone.getName()));
        return list.build();
    }
}
