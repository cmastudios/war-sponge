package com.tommytony.war;

import com.tommytony.war.item.WarItem;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.WarGame;
import com.tommytony.war.zone.Warzone;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a player on the server. Provides the abstraction for all interaction directly related to a specific
 * player. Manages state for War functionality.
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

    /**
     * Check if the player is in an active game.
     *
     * @return true if the player is playing War
     */
    public boolean isPlayingWar() {
        return plugin.getZones().values().stream()
                .filter(z -> {
                    Optional<WarGame> game = z.getGame();
                    return game.isPresent() && game.get().isPlaying(this);
                }).count() > 0;
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

    /**
     * Gets all relevant information to a player's condition in the world.
     *
     * @return current state of the player
     */
    public abstract PlayerState getState();

    /**
     * Change attributes of the player.
     *
     * @param state new state
     */
    public abstract void setState(PlayerState state);

    /**
     * Get the warzone the player occupies.
     *
     * @return warzone with active game
     */
    public Warzone getWarzone() {
        return plugin.getZones().values().stream()
                .filter(z -> {
                    Optional<WarGame> game = z.getGame();
                    return game.isPresent() && game.get().isPlaying(this);
                }).findAny().orElse(null);
    }

    /**
     * Get the item in the player's main hand.
     *
     * @return item in hand
     */
    public abstract WarItem getItemInHand();

    /**
     * Get the player's name with formatting applied to represent their team, if teams are based on colors.
     * <p>
     * Despite having a similar name in comparison to the server implementations, this function uses the player's
     * username with formatting from their status in War only.
     *
     * @return formatted name
     */
    public String getDisplayName() {
        if (isPlayingWar()) {
            return getWarzone().getGame().orElseThrow(IllegalStateException::new).getPlayerTeam(this).getColor() + getName();
        }
        return getName();
    }

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

    public static class WarGameMode {
        public static final int SURVIVAL = 0;
        public static final int CREATIVE = 1;
        public static final int ADVENTURE = 2;
    }

    /**
     * Stores a player's items and status upon entering a warzone, for restoration after a game.
     */
    public static class PlayerState {
        private int gameMode;
        private WarItem[] inventory;
        private WarItem helmet, chestplate, leggings, boots;
        private double health;
        private double exhaustion;
        private double saturation;
        private double hunger;
        private double level;
        private double experience;
        private boolean flying;

        public PlayerState(int gameMode, WarItem[] inventory, WarItem helmet, WarItem chestplate, WarItem leggings,
                           WarItem boots, double health, double exhaustion, double saturation, double hunger,
                           double level, double experience, boolean flying) {
            this.gameMode = gameMode;
            this.inventory = inventory;
            this.helmet = helmet;
            this.chestplate = chestplate;
            this.leggings = leggings;
            this.boots = boots;
            this.health = health;
            this.exhaustion = exhaustion;
            this.saturation = saturation;
            this.hunger = hunger;
            this.level = level;
            this.experience = experience;
            this.flying = flying;
        }

        public int getGameMode() {
            return gameMode;
        }

        public void setGameMode(int gameMode) {
            this.gameMode = gameMode;
        }

        public WarItem[] getInventory() {
            return inventory;
        }

        public void setInventory(WarItem[] inventory) {
            this.inventory = inventory;
        }

        public WarItem getHelmet() {
            return helmet;
        }

        public void setHelmet(WarItem helmet) {
            this.helmet = helmet;
        }

        public WarItem getChestplate() {
            return chestplate;
        }

        public void setChestplate(WarItem chestplate) {
            this.chestplate = chestplate;
        }

        public WarItem getLeggings() {
            return leggings;
        }

        public void setLeggings(WarItem leggings) {
            this.leggings = leggings;
        }

        public WarItem getBoots() {
            return boots;
        }

        public void setBoots(WarItem boots) {
            this.boots = boots;
        }

        public double getHealth() {
            return health;
        }

        public void setHealth(double health) {
            this.health = health;
        }

        public double getExhaustion() {
            return exhaustion;
        }

        public void setExhaustion(double exhaustion) {
            this.exhaustion = exhaustion;
        }

        public double getSaturation() {
            return saturation;
        }

        public void setSaturation(double saturation) {
            this.saturation = saturation;
        }

        public double getHunger() {
            return hunger;
        }

        public void setHunger(double hunger) {
            this.hunger = hunger;
        }

        public double getLevel() {
            return level;
        }

        public void setLevel(double level) {
            this.level = level;
        }

        public double getExperience() {
            return experience;
        }

        public void setExperience(double experience) {
            this.experience = experience;
        }

        public boolean isFlying() {
            return flying;
        }

        public void setFlying(boolean flying) {
            this.flying = flying;
        }
    }
}
