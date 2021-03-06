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
import java.util.UUID;

/**
 * The main war configuration database.
 */
public class WarConfig implements Closeable {
    public static final String DEFAULT_FORMAT = "{0} <{1}> ({2})";
    public static final String MODIFIED_FORMAT = "{0} <{1}> = {2}";
    private final ZoneConfig zoneDefaults;
    /**
     * Database configuration descriptor.
     */
    private Connection conn;

    /**
     * Load the war config database for future use.
     *
     * @param file War configuration database location.
     * @throws FileNotFoundException if folder for database does not exist.
     * @throws SQLException if there is an error creating or updating tables.
     */
    WarConfig(File file) throws FileNotFoundException, SQLException {
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
     * @throws SQLException error executing query.
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
     * @throws SQLException error executing update.
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
     * @throws SQLException error executing update.
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
     * @throws SQLException error executing query.
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
     * Gets the value of any war setting.
     *
     * @param setting Setting to lookup.
     * @return value of setting, or the default if not found.
     */
    public Object getObject(WarSetting setting) {
        if (setting.getDataType() == Integer.class) {
            return this.getInt(setting);
        }
        return null;
    }

    /**
     * Set the value of any war setting. This function will convert the value stored in the string to the appropriate
     * datatype.
     * @param setting Setting to change.
     * @param value value of setting to add or replace.
     * @throws NumberFormatException contents of value are invalid for the type of setting.
     */
    public void setValue(WarSetting setting, String value) {
        if (setting.getDataType() == Integer.class) {
            setInt(setting, Integer.valueOf(value));
        }
    }

    /**
     * Set a value in the database, for an integer.
     * @param setting Setting to change.
     * @param value New value to add or replace.
     */
    public void setInt(WarSetting setting, int value) {
        boolean exists;
        String sql = "INSERT INTO settings (value, option) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement("SELECT value FROM settings WHERE option = ?")) {
            stmt.setString(1, setting.name());
            try (ResultSet result = stmt.executeQuery()) {
                exists = result.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (exists) {
            sql = "UPDATE settings SET value = ? WHERE option = ?";
        }
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, value);
            stmt.setString(2, setting.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

        public Class<?> getDataType() {
            return dataType;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }
}
