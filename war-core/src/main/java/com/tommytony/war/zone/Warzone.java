package com.tommytony.war.zone;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarPlayer;
import com.tommytony.war.item.WarEntity;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;

import java.io.File;
import java.sql.SQLException;
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

    public WarLocation getTeleport() {
        try {
            if (db.hasPosition("lobby")) {
                return db.getPosition("lobby");
            } else {
                throw new RuntimeException("No teleport location found for zone " + name);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTeleport(WarLocation location) {
        if (this.getCuboid().contains(location)) {
            throw new IllegalStateException("Lobby position cannot be set inside of a zone.");
        }
        try {
            db.setPosition("lobby", location);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getTeams() {
        try {
            return db.getTeams();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public WarLocation getTeamSpawn(String teamName) {
        try {
            return db.getPosition("teamspawn" + teamName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTeamSpawn(String teamName, WarLocation location) {
        if (!this.getCuboid().contains(location)) {
            throw new IllegalStateException("Team spawn position must be set inside of a zone.");
        }
        try {
            db.setPosition("teamspawn" + teamName, location);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTeam(String teamName) {
        try {
            db.deletePosition("teamspawn" + teamName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public File getDataFile() {
        return db.getDataStore();
    }

    public void save() {
        plugin.logInfo("Saving zone " + this.getName() + "...");
        try {
            db.saveBlocks();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        plugin.logInfo("Reloading zone " + this.getName() + "...");
        plugin.removeEntity(this.getCuboid(), WarEntity.ITEM);
        try {
            db.loadBlocks();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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

    public Optional<WarGame> getGame() {
        return Optional.ofNullable(game);
    }

    public void setGame(WarGame game) {
        this.game = game;
    }

    public void setGate(String gateName, WarLocation location) {
        if (!gateName.equals("autoassign") && getTeamSpawn(gateName) == null) {
            throw new IllegalArgumentException("Gate must be for an existing team or set to autoassign.");
        }
        if (this.getCuboid().contains(location)) {
            throw new IllegalStateException("Gate position cannot be set inside of a zone.");
        }
        gates = null;
        try {
            db.setPosition("gate" + gateName, location);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteGate(String gateName) {
        gates = null;
        try {
            db.deletePosition("gate" + gateName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<WarLocation, String> gates;

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

    public void newGame() {
        if (this.getConfig().getBoolean(ZoneSetting.EDITING))
            throw new IllegalStateException("Warzone disabled.");
        if (this.getTeams().isEmpty())
            throw new IllegalStateException("Cannot start a game in a warzone without teams.");
        setGame(new WarGame(this, plugin));
    }
}
