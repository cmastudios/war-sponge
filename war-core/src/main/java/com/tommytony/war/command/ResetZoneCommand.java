package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConsole;
import com.tommytony.war.zone.WarGame;
import com.tommytony.war.zone.Warzone;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

public class ResetZoneCommand extends WarCommand {
    public ResetZoneCommand(ServerAPI plugin) {
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
        sender.sendMessage(MessageFormat.format("Reloading zone {0}...", zoneName));
        Optional<WarGame> game = zone.getGame();
        if (game.isPresent()) {
            game.get().endRound();
        } else {
            zone.reset();
        }
        int affected = (int) Math.floor(zone.getCuboid().getSize());
        sender.sendMessage(MessageFormat.format("Reloaded {0} blocks in zone {1}.", affected, zoneName));

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
        return "resetzone";
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of();
    }

    @Override
    public String getTagline() {
        return "Reset and reload a zone.";
    }

    @Override
    public String getDescription() {
        return "This command will either reset the zone blocks if empty, or end the current round if a game is active (which will reset the blocks).";
    }

    @Override
    public String getUsage() {
        return "<zone>";
    }

    @Override
    public String getPermission() {
        return "war.zone.reset";
    }
}
