package com.tommytony.war.zone;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.WarConfig;
import com.tommytony.war.item.WarEntity;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;

import java.io.File;
import java.sql.SQLException;

/**
 * Representation of a war zone area, blocks, and settings.
 * Each zone is to only have one instance of Warzone.class at any given time while the server is running.
 */
public class Warzone implements AutoCloseable {
    private final String name;
    private final ZoneStorage db;
    private final ZoneConfig config;
    private final ServerAPI plugin;

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
            this.config = new ZoneConfig(db.getConnection(), "settings", plugin.getConfig().getZoneDefaults());
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
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

    public WarCuboid getCuboid() {
        try {
            return new WarCuboid(db.getPosition("position1"), db.getPosition("position2"));
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
        try {
            db.setPosition("lobby", location);
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

    @Override
    public void close() throws Exception {
        db.close();
    }

    public ZoneConfig getConfig() {
        return config;
    }
}
