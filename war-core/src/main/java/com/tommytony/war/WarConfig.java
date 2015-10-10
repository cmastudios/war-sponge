package com.tommytony.war;

import com.google.common.collect.ImmutableList;
import com.tommytony.war.zone.ZoneConfig;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * The main war configuration database.
 */
public class WarConfig implements Closeable {
    private final ZoneConfig zoneDefaults;
    /**
     * Database configuration descriptor.
     */
    private Connection conn;

    /**
     * Load the war config database for future use.
     *
     * @param file War configuration database location.
     * @throws FileNotFoundException
     * @throws SQLException
     */
    public WarConfig(File file) throws FileNotFoundException, SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:" + file.getPath());
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS settings (option TEXT, value BLOB)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS zones (name TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS zonemakers (uuid TEXT)");
        }
        zoneDefaults = new ZoneConfig(conn, "zone_settings");
    }

    /**
     * Get the value of an integer setting.
     *
     * @param setting The type of setting to look up.
     * @return the value of the setting or the default if not found.
     * @throws RuntimeException wrapping SQLException
     */
    public int getInt(WarSetting setting) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT value FROM settings WHERE option = ?")) {
            stmt.setString(1, setting.name());
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    return result.getInt(1);
                } else {
                    return (Integer) setting.defaultValue;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get access to zone default settings.
     *
     * @return controller of server zone defaults.
     */
    public ZoneConfig getZoneDefaults() {
        return zoneDefaults;
    }

    /**
     * Load all the enabled war zones on the server.
     *
     * @return list of war zones.
     * @throws SQLException
     */
    public Collection<String> getZones() throws SQLException {
        ArrayList<String> zones = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet result = stmt.executeQuery("SELECT name FROM zones")) {
            while (result.next()) {
                zones.add(result.getString(1));
            }
        }
        return ImmutableList.copyOf(zones);
    }

    /**
     * Add a zone to the database. Does not create any warzone data files.
     *
     * @param zoneName Name of the warzone.
     * @throws SQLException
     */
    public void addZone(String zoneName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO zones (name) VALUES (?)")) {
            stmt.setString(1, zoneName);
            stmt.executeUpdate();
        }
    }

    /**
     * Remove a zone from the database. Does not modify any warzone data files.
     *
     * @param zoneName Name of the warzone.
     * @throws SQLException
     */
    public void deleteZone(String zoneName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM zones WHERE name = ?")) {
            stmt.setString(1, zoneName);
            stmt.executeUpdate();
        }
    }

    /**
     * Get server zone makers. These people have permission to create zones.
     *
     * @return list of zone makers.
     * @throws SQLException
     */
    public Collection<UUID> getZoneMakers() throws SQLException {
        ArrayList<UUID> makers = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet result = stmt.executeQuery("SELECT uuid FROM zonemakers")) {
            while (result.next()) {
                UUID playerId = UUID.fromString(result.getString(1));
                makers.add(playerId);
            }
        }
        return ImmutableList.copyOf(makers);
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * @throws java.io.IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    /**
     * Possible types of settings stored in the war server config database.
     */
    public enum WarSetting {
        MAXZONES(Integer.class, 20),
        MAXZONESIZE(Integer.class, 1_000_000);
        private final Class<?> dataType;
        private final Object defaultValue;

        WarSetting(Class<?> dataType, Object defaultValue) {
            this.dataType = dataType;
            this.defaultValue = defaultValue;
        }
    }
}
