package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConsole;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.zone.Warzone;

import java.text.MessageFormat;
import java.util.List;

public class SetPointCommand extends WarCommand {
    public SetPointCommand(ServerAPI plugin) {
        super(plugin);
    }

    @Override
    void handleCommand(WarConsole sender, String[] args) {
        if (!(sender instanceof WarPlayer)) {
            throw new NotPlayerError();
        }
        WarPlayer player = (WarPlayer) sender;
        if (args.length < 2) {
            throw new InvalidArgumentsError();
        }
        String zoneName = args[0];
        Warzone zone = getPlugin().getZone(zoneName);
        if (zone == null) {
            throw new CommandUserError(MessageFormat.format("Warzone {0} not found.", zoneName));
        }
        String object = args[1];
        if (object.equalsIgnoreCase("lobby")) {
            zone.setTeleport(player.getLocation());
            player.sendMessage("Lobby set to your current location.");
        } else if (object.equalsIgnoreCase("spawn")) {
            if (args.length < 3) {
                throw new CommandUserError("Setting this position requires a team argument.");
            }
            String team = args[2];
            zone.setTeamSpawn(team, player.getLocation());
            player.sendMessage(MessageFormat.format("Spawn point for team {0} set to your current location.", team));
        } else if (object.equalsIgnoreCase("gate")) {
            if (args.length < 3) {
                throw new CommandUserError("Setting this position requires an argument of either team or autoassign.");
            }
            String team = args[2];
            zone.setGate(team, player.getLocation());
            player.sendMessage(MessageFormat.format("Gate for {0} set to your current location.", team));
        }
    }

    @Override
    List<String> handleTab(WarConsole sender, String[] args) {
        if (args.length == 0) {
            return ImmutableList.of("lobby", "spawn", "gate");
        } else if (args.length == 1) {
            ImmutableList.Builder<String> list = ImmutableList.builder();
            ImmutableList.of("lobby", "spawn", "gate").stream().filter(s -> s.startsWith(args[0]))
                    .forEach(list::add);
            return list.build();
        } else {
            return ImmutableList.of();
        }
    }
}
