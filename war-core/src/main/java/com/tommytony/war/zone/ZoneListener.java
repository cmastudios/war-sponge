package com.tommytony.war.zone;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.struct.WarLocation;

import java.util.Optional;

/**
 * Handles events that occur in relation to a war zone.
 */
public class ZoneListener {
    private final Warzone warzone;
    private final ServerAPI plugin;

    ZoneListener(Warzone warzone, ServerAPI plugin) {
        this.warzone = warzone;
        this.plugin = plugin;
    }

    public boolean handlePlayerMovementInWarzone(WarPlayer player, WarLocation from, WarLocation to) {
        return false;
    }

    public boolean handlePlayerLeaveZone(WarPlayer player, WarLocation from, WarLocation to) {
        Optional<WarGame> game = warzone.getGame();
        if (game.isPresent() && game.get().isPlaying(player)) {
            player.sendMessage("Please use /warleave to exit the game before leaving.");
            return true;
        }
        return false;
    }

    public boolean handlePlayerEnterZone(WarPlayer player, WarLocation from, WarLocation to) {
        Optional<WarGame> game = warzone.getGame();
        if (game.isPresent() || !player.isZoneMaker()) {
            player.sendMessage("Please join a team first.");
            player.setLocation(warzone.getTeleport());
            warzone.mask(player);
            plugin.delayTask(3, () -> warzone.unmask(player));
            return false;
        }
        return false;
    }
}
