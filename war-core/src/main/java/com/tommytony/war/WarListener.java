package com.tommytony.war;

import com.tommytony.war.struct.WarLocation;

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
        if (fromInZone && !toInZone) {
            return plugin.getZones().values().stream()
                    .filter(warzone -> warzone.getCuboid().contains(from)).findFirst().orElse(null)
                    .getListener().handlePlayerLeaveZone(player, from, to);
        } else if (!fromInZone && toInZone) {
            return plugin.getZones().values().stream()
                    .filter(warzone -> warzone.getCuboid().contains(to)).findFirst().orElse(null)
                    .getListener().handlePlayerEnterZone(player, from, to);
        } else if (fromInZone) {
            return plugin.getZones().values().stream()
                    .filter(warzone -> warzone.getCuboid().contains(from)).findFirst().orElse(null)
                    .getListener().handlePlayerMovementInWarzone(player, from, to);
        }
        return false;
    }
}
