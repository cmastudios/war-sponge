package com.tommytony.war.command;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConsole;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.zone.WarGame;
import com.tommytony.war.zone.Warzone;

import java.util.List;
import java.util.Optional;

public class LeaveCommand extends WarCommand {
    public LeaveCommand(ServerAPI plugin) {
        super(plugin);
    }

    @Override
    void handleCommand(WarConsole sender, String[] args) {
        if (!(sender instanceof WarPlayer)) {
            throw new NotPlayerError();
        }
        WarPlayer player = (WarPlayer) sender;
        for (Warzone zone : getPlugin().getZones().values()) {
            Optional<WarGame> game = zone.getGame();
            if (game.isPresent()) {
                if (game.get().isPlaying(player)) {
                    game.get().removePlayer(player);
                    return;
                }
            }
        }
    }

    @Override
    List<String> handleTab(WarConsole sender, String[] args) {
        return ImmutableList.of();
    }
}
