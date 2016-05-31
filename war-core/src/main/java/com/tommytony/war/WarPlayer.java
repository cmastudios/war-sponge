package com.tommytony.war;

import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarLocation;

import java.util.UUID;

/**
 * Stores state for players.
 */
public abstract class WarPlayer extends WarConsole {
    private final UUID playerId;
    private final ServerAPI plugin;
    private ZoneCreationState zoneCreationState;

    /**
     * Construct a WarPlayer. This instance is valid for as long as the plugin is loaded.
     *
     * @param playerId Unique ID of the player.
     * @param plugin War plugin.
     */
    public WarPlayer(UUID playerId, ServerAPI plugin) {
        this.playerId = playerId;
        this.plugin = plugin;
    }

    /**
     * Check if the player is a zone maker setting a new war zone.
     *
     * @return true if the person has not finished setting a zone
     */
    public boolean isCreatingZone() {
        return this.getZoneCreationState() != null;
    }

    /**
     * Get data about the current zone in progress.
     *
     * @return data
     */
    public ZoneCreationState getZoneCreationState() {
        return zoneCreationState;
    }

    /**
     * Set the current zone in progress.
     *
     * @param zoneCreationState new zone data
     */
    public void setZoneCreationState(ZoneCreationState zoneCreationState) {
        this.zoneCreationState = zoneCreationState;
    }

    UUID getPlayerId() {
        return playerId;
    }

    public boolean isPlayingWar() {
        return plugin.getZones().values().stream()
                .filter(z -> z.getGame().isPresent() && z.getGame().get().isPlaying(this)).count() > 0;
    }

    /**
     * Check if the player is online. War will load player instances for offline players.
     *
     * @return true if player is online
     */
    public abstract boolean isOnline();

    /**
     * Get the current world location.
     *
     * @return player location
     */
    public abstract WarLocation getLocation();

    /**
     * Teleport the player to a new location.
     *
     * @param location new location
     */
    public abstract void setLocation(WarLocation location);

    /**
     * Get block that the player is currently targeting with their cursor. In Bukkit, limited to 100 blocks.
     *
     * @return targeted block or null if none found
     */
    public abstract WarLocation getTargetBlock();

    /**
     * Check if the player can modify war zones.
     *
     * @return true if player is a zone maker.
     */
    public abstract boolean isZoneMaker();

    /**
     * Set a block visible to the player only.
     *
     * @param location location to place block
     * @param block    block data
     */
    public abstract void setLocalBlock(WarLocation location, WarBlock block);

    /**
     * Get the player's name.
     *
     * @return name
     */
    public abstract String getName();

    public static class ZoneCreationState {
        private final String zoneName;
        private WarLocation position1;

        public ZoneCreationState(String zoneName) {
            this.zoneName = zoneName;
        }

        public String getZoneName() {
            return zoneName;
        }

        public boolean isPosition1Set() {
            return this.getPosition1() != null;
        }

        public WarLocation getPosition1() {
            return position1;
        }

        public void setPosition1(WarLocation position1) {
            this.position1 = position1;
        }
    }
}
