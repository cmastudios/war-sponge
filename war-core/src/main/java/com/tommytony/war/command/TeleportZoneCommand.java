package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConsole;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.zone.Warzone;

import java.text.MessageFormat;
import java.util.List;

public class TeleportZoneCommand extends WarCommand {
    public TeleportZoneCommand(ServerAPI plugin) {
        super(plugin);
    }

    @Override
    void handleCommand(WarConsole sender, String[] args) {
        if (args.length == 0) {
            StringBuilder zones = new StringBuilder();
            boolean first = true;
            for (String zoneName : getPlugin().getZones().keySet()) {
                if (!first) {
                    zones.append(", ");
                }
                first = false;
                zones.append(zoneName);
            }
            sender.sendMessage(MessageFormat.format("Zones: {0}", zones));
            return;
        }
        if (!(sender instanceof WarPlayer)) {
            throw new NotPlayerError();
        }
        String zoneName = args[0];
        Warzone zone = getPlugin().getZone(zoneName);
        if (zone == null) {
            throw new CommandUserError(MessageFormat.format("Zone {0} not found.", zoneName));
        }
        WarPlayer player = (WarPlayer) sender;
        player.setLocation(zone.getTeleport());
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

    @Override
    public String getName() {
        return "warzone";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("zone");
    }

    @Override
    public String getTagline() {
        return "Teleport to a warzone.";
    }

    @Override
    public String getDescription() {
        return "With no arguments, lists available zones. With one argument, teleports player to the zone's lobby.";
    }

    @Override
    public String getUsage() {
        return "<zone>";
    }

    @Override
    public String getPermission() {
        return "war.teleport";
    }
}
