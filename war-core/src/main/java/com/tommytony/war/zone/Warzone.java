package com.tommytony.war.zone;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.item.WarEntity;
import com.tommytony.war.item.WarInventory;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;

import java.io.File;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Representation of a war zone area, blocks, and settings.
 * Each zone is to only have one instance of Warzone.class at any given time while the server is running.
 */
public class Warzone implements AutoCloseable {
    private final String name;
    private final ZoneStorage db;
    private final ZoneConfig config;
    private final ServerAPI plugin;
    private final ZoneListener listener;
    private WarGame game;
    private Map<WarLocation, String> gates;

    /**
     * Load or create a war zone from the war settings store.
     *
     * @param name Name of warzone
     * @param plugin War plugin
     */
    public Warzone(String name, ServerAPI plugin) {
        this.name = name;
        this.plugin = plugin;
        try {
            this.db = new ZoneStorage(this, plugin);
            this.config = new ZoneConfig(db.getConnection(), "settings", plugin.getWarConfig().getZoneDefaults());
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        this.listener = new ZoneListener(this, plugin);
    }

    /**
     * Get a cuboid containing all blocks in the warzone.
     *
     * @return cuboid region for zone.
     */
    public WarCuboid getCuboid() {
        try {
            return new WarCuboid(db.getPosition("position1"), db.getPosition("position2"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the area the zone occupies. Updates position1 and position2 in the database.
     *
     * @param cuboid Cuboid region of zone space.
     */
    public void setCuboid(WarCuboid cuboid) {
        try {
            db.setPosition("position1", cuboid.getMinBlock());
            db.setPosition("position2", cuboid.getMaxBlock());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Get the location of the warzone lobby.
     *
     * @return lobby location.
     * @throws IllegalStateException if no lobby exists.
     */
    public WarLocation getTeleport() {
        try {
            if (db.hasPosition("lobby")) {
                return db.getPosition("lobby");
            } else {
                throw new IllegalStateException(MessageFormat.format("No lobby found for zone {0}.", name));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the location of the warzone lobby.
     *
     * @param location new lobby location.
     * @throws IllegalArgumentException illegal lobby location.
     */
    public void setTeleport(WarLocation location) {
        if (this.getCuboid().contains(location)) {
            throw new IllegalArgumentException("Lobby position cannot be set inside of a zone.");
        }
        try {
            db.setPosition("lobby", location);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a list of teams in this zone. This function is based on the team spawn locations saved in the database.
     *
     * @return list of teams.
     */
    public List<String> getTeams() {
        try {
            return db.getTeams();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the location of the team spawn.
     *
     * @param teamName name of team in this zone.
     * @return location of spawn.
     */
    public WarLocation getTeamSpawn(String teamName) {
        try {
            return db.getPosition("teamspawn" + teamName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the location of the spawn for a particular team.
     *
     * @param teamName team of spawn.
     * @param location spawn location.
     * @throws IllegalArgumentException illegal spawn position.
     */
    public void setTeamSpawn(String teamName, WarLocation location) {
        if (!this.getCuboid().contains(location)) {
            throw new IllegalArgumentException("Team spawn position must be set inside of a zone.");
        }
        try {
            db.setPosition("teamspawn" + teamName, location);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete the spawn of a team in the zone. This effectively deletes the team due to the algorithm employed by the
     * #getZones method.
     *
     * @param teamName team to delete.
     */
    public void deleteTeam(String teamName) {
        try {
            db.deletePosition("teamspawn" + teamName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the location of the warzone data file. All information, blocks, settings, and locations for a warzone are
     * saved in this file.
     *
     * @return warzone file location.
     */
    public File getDataFile() {
        return db.getDataStore();
    }

    /**
     * Save all blocks in the warzone. This method will block on the current thread until all blocks are saved. Server
     * administrators may prevent crashes resulting from large warzone saving by tweaking the maxzonesize setting.
     * <p>
     * This method saves only blocks. All other data, such as coordinates and settings, are saved automatically.
     */
    public void save() {
        plugin.logInfo("Saving zone " + this.getName() + "...");
        try {
            db.saveBlocks();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Replace all the blocks in the region of the warzone to the blocks stored in the warzone database. This will block
     * on the current thread until finished.
     */
    public void reset() {
        plugin.logInfo("Reloading zone " + this.getName() + "...");
        plugin.removeEntity(this.getCuboid(), WarEntity.ITEM);
        try {
            db.loadBlocks();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a wall around the warzone to prevent the entry of a specific player.
     *
     * @param player target of block updates.
     */
    public void mask(WarPlayer player) {
        for (WarLocation loc : this.getCuboid()) {
            // following condition checks if the block is on a face of the cuboid
            if (loc.getBlockX() == this.getCuboid().getMinBlock().getBlockX() || loc.getBlockX() == this.getCuboid().getMaxBlock().getBlockX()
                    || loc.getBlockY() == this.getCuboid().getMinBlock().getBlockY() || loc.getBlockY() == this.getCuboid().getMaxBlock().getBlockY()
                    || loc.getBlockZ() == this.getCuboid().getMinBlock().getBlockZ() || loc.getBlockZ() == this.getCuboid().getMaxBlock().getBlockZ()) {
                if (plugin.getBlock(loc, true).getBlockName().toLowerCase().contains("air"))
                    player.setLocalBlock(loc, new WarBlock("minecraft:glass", null, "", (short) 0));
            }
        }
    }

    /**
     * Remove the wall around the warzone in a player's view, by re-sending all the blocks that were originally
     * replaced.
     *
     * @param player target of block updates.
     */
    public void unmask(WarPlayer player) {
        for (WarLocation loc : this.getCuboid()) {
            // following condition checks if the block is on a face of the cuboid
            if (loc.getBlockX() == this.getCuboid().getMinBlock().getBlockX() || loc.getBlockX() == this.getCuboid().getMaxBlock().getBlockX()
                    || loc.getBlockY() == this.getCuboid().getMinBlock().getBlockY() || loc.getBlockY() == this.getCuboid().getMaxBlock().getBlockY()
                    || loc.getBlockZ() == this.getCuboid().getMinBlock().getBlockZ() || loc.getBlockZ() == this.getCuboid().getMaxBlock().getBlockZ()) {
                player.setLocalBlock(loc, plugin.getBlock(loc, false));
            }
        }
    }

    /**
     * Closes the warzone's underlying database, saving all information.
     * @throws Exception if the database cannot be closed.
     */
    @Override
    public void close() throws Exception {
        db.close();
    }

    public ZoneConfig getConfig() {
        return config;
    }

    public ZoneListener getListener() {
        return listener;
    }

    /**
     * Gets the active game in this warzone. May not be present if no game is underway.
     *
     * @return game or none.
     */
    public Optional<WarGame> getGame() {
        return Optional.ofNullable(game);
    }

    void setGame(WarGame game) {
        this.game = game;
    }

    /**
     * Set the location of a gate. Gates take players from a location outside the zone to a position on a team.
     *
     * @param gateName either a valid team name or 'autoassign'.
     * @param location location outside the warzone.
     * @throws IllegalArgumentException invalid gate name or location position.
     */
    public void setGate(String gateName, WarLocation location) {
        if (!gateName.equals("autoassign") && getTeamSpawn(gateName) == null) {
            throw new IllegalArgumentException("Gate must be for an existing team or set to autoassign.");
        }
        if (this.getCuboid().contains(location)) {
            throw new IllegalArgumentException("Gate position cannot be set inside of a zone.");
        }
        gates = null;
        try {
            db.setPosition("gate" + gateName, location);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Remove a gate linking to this warzone.
     *
     * @param gateName either a valid team name or 'autoassign'.
     */
    public void deleteGate(String gateName) {
        gates = null;
        try {
            db.deletePosition("gate" + gateName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the location of all gates linking to this warzone. The result is cached in memory for efficient lookup when
     * a player moves.
     *
     * @return mapping of locations to gate names.
     */
    public Map<WarLocation, String> getGates() {
        if (gates != null) {
            return gates;
        }
        try {
            gates = db.getGates();
            return gates;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Start a new game in the warzone. This is called when a player enters a gate and no game already exists.
     * @throws IllegalStateException If the warzone is disabled or has no teams.
     */
    public void newGame() {
        if (this.getConfig().getBoolean(ZoneSetting.EDITING))
            throw new IllegalStateException("Warzone disabled.");
        if (this.getTeams().isEmpty())
            throw new IllegalStateException("Cannot start a game in a warzone without teams.");
        setGame(new WarGame(this, plugin));
    }

    /**
     * Save the contents of a inventory to the zone.
     *
     * @param name      name of the inventory to update or create.
     * @param inventory contents of the inventory.
     */
    public void saveInventory(String name, WarInventory inventory) {
        try {
            db.saveInventory(name, inventory);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieve the contents of an inventory.
     *
     * @param name name of the inventory to look up.
     * @return loaded inventory data or an empty inventory if not found.
     */
    public WarInventory getInventory(String name) {
        try {
            WarInventory inventory = db.getInventory(name);
            if (inventory == null)
                return new WarInventory();
            return inventory;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
