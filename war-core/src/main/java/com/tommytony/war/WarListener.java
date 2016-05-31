package com.tommytony.war;

import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.WarGame;
import com.tommytony.war.zone.Warzone;

import java.util.Optional;

/**
 * Handles all events received by War.
 */
public class WarListener {
    private final ServerAPI plugin;

    WarListener(ServerAPI warPlugin) {
        plugin = warPlugin;
    }

    public boolean handlePlayerMovement(WarPlayer player, WarLocation from, WarLocation to) {
        boolean fromInZone = plugin.getZones().values().stream().anyMatch(warzone -> warzone.getCuboid().contains(from));
        boolean toInZone = plugin.getZones().values().stream().anyMatch(warzone -> warzone.getCuboid().contains(to));
        if (fromInZone && !toInZone) { // leaving a zone
            return plugin.getZones().values().stream()
                    .filter(warzone -> warzone.getCuboid().contains(from)).findFirst().orElse(null)
                    .getListener().handlePlayerLeaveZone(player, from, to);
        } else if (!fromInZone && toInZone) { // entering a zone
            return plugin.getZones().values().stream()
                    .filter(warzone -> warzone.getCuboid().contains(to)).findFirst().orElse(null)
                    .getListener().handlePlayerEnterZone(player, from, to);
        } else if (fromInZone) { // interaction inside of a zone
            return plugin.getZones().values().stream()
                    .filter(warzone -> warzone.getCuboid().contains(from)).findFirst().orElse(null)
                    .getListener().handlePlayerMovementInWarzone(player, from, to);
        } else { // interaction outside of a zone
            for (Warzone zone : plugin.getZones().values()) {
                // check all gates in each war zone
                zone.getGates().keySet().stream().filter(loc -> to.getBlockLoc().equals(loc.getBlockLoc())).forEach(loc -> {
                    String gateName = zone.getGates().get(loc);
                    if (gateName.equals("autoassign")) {
                        if (!zone.getGame().isPresent()) {
                            zone.newGame();
                        }
                        Optional<WarGame> game = zone.getGame();
                        if (game.isPresent()) {
                            game.get().autoAssign(player);
                        }
                    } else {
                        if (!zone.getGame().isPresent()) {
                            zone.newGame();
                        }
                        Optional<WarGame> game = zone.getGame();
                        if (game.isPresent()) {
                            WarGame.Team team = game.get().getTeam(gateName);
                            game.get().assign(player, team);
                        }
                    }
                });
            }
        }
        return false;
    }
}
