package com.tommytony.war.zone;

import com.tommytony.war.WarConfig;
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

    /**
     * Load or create a war zone from the war settings store.
     *
     * @param plugin Instance of the plugin War.
     * @param name   Name of the zone. Used to locate the zone data file.
     */
    public Warzone(String name, File dataDir, WarConfig config) {
        this.name = name;
        try {
            this.db = new ZoneStorage(this, dataDir);
            this.config = new ZoneConfig(db.getConnection(), "settings", config.getZoneDefaults());
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

    @Override
    public void close() throws Exception {
        db.close();
    }
}
