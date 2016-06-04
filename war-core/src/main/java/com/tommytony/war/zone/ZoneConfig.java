package com.tommytony.war.zone;

import java.sql.*;

/**
 * The zone configuration settings database.
 */
public class ZoneConfig {
    /**
     * Database configuration descriptor.
     */
    private final Connection conn;
    /**
     * Table of values to manage. May be a table in a zone database or the main war database.
     */
    private final String table;
    /**
     * Root zone config, for fallback. Null if this is the war main settings.
     */
    private final ZoneConfig parent;

    /**
     * Manages a zone configuration section.
     *
     * @param database Active database to use.
     * @param table    Table name to use in database. Created if it does not exist. Needs to be trusted input.
     * @param parent   Parent zone config, for fallback. Could be zone config for a team or war global for zones.
     * @throws SQLException if there is an error creating or updating tables.
     */
    public ZoneConfig(Connection database, String table, ZoneConfig parent) throws SQLException {
        this.conn = database;
        this.table = table;
        this.parent = parent;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(String.format("CREATE TABLE IF NOT EXISTS %s (option TEXT, value BLOB)", table));
        }
    }

    /**
     * Manages a zone configuration section.
     *
     * @param database Active database to use.
     * @param table    Table name to use in database. Created if it does not exist. Needs to be trusted input.
     * @throws SQLException if there is an error creating or updating tables.
     */
    public ZoneConfig(Connection database, String table) throws SQLException {
        this(database, table, null);
    }

    /**
     * Get the value of an integer setting.
     *
     * @param setting The type of setting to look up.
     * @return the value of the setting or the default if not found.
     */
    public int getInt(ZoneSetting setting) {
        try (PreparedStatement stmt = conn.prepareStatement(String.format("SELECT value FROM %s WHERE option = ?", table))) {
            stmt.setString(1, setting.name());
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    // found an override for this config level
                    return result.getInt(1);
                } else if (parent != null) {
                    // look for it in zone/global configs; will be recursive upwards
                    return parent.getInt(setting);
                } else {
                    // the hard-coded value for fallback
                    return (Integer) setting.getDefaultValue();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set a value in the database, for an integer.
     * @param setting Setting to change.
     * @param value New value to add or replace.
     */
    public void setInt(ZoneSetting setting, int value) {
        boolean exists;
        String sql = "INSERT INTO %s (value, option) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(String.format("SELECT value FROM %s WHERE option = ?", table))) {
            stmt.setString(1, setting.name());
            try (ResultSet result = stmt.executeQuery()) {
                exists = result.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (exists) {
            sql = "UPDATE %s SET value = ? WHERE option = ?";
        }
        try (PreparedStatement stmt = conn.prepareStatement(String.format(sql, table))) {
            stmt.setInt(1, value);
            stmt.setString(2, setting.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the value of a boolean setting.
     *
     * @param setting Type of setting to lookup.
     * @return value of setting or the default if not found.
     */
    public boolean getBoolean(ZoneSetting setting) {
        try (PreparedStatement stmt = conn.prepareStatement(String.format("SELECT value FROM %s WHERE option = ?", table))) {
            stmt.setString(1, setting.name());
            try (ResultSet result = stmt.executeQuery()) {
                if (result.next()) {
                    // found an override for this config level
                    return Boolean.parseBoolean(result.getString(1));
                } else if (parent != null) {
                    // look for it in zone/global configs; will be recursive upwards
                    return parent.getBoolean(setting);
                } else {
                    // the hard-coded value for fallback
                    return (Boolean) setting.getDefaultValue();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set a value in the database, for a boolean setting.
     * @param setting Setting to change.
     * @param value New value to add or replace.
     */
    public void setBoolean(ZoneSetting setting, boolean value) {
        boolean exists;
        String sql = "INSERT INTO %s (value, option) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(String.format("SELECT value FROM %s WHERE option = ?", table))) {
            stmt.setString(1, setting.name());
            try (ResultSet result = stmt.executeQuery()) {
                exists = result.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (exists) {
            sql = "UPDATE %s SET value = ? WHERE option = ?";
        }
        try (PreparedStatement stmt = conn.prepareStatement(String.format(sql, table))) {
            stmt.setString(1, Boolean.toString(value));
            stmt.setString(2, setting.name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the value of any zone setting.
     * @param setting Setting to lookup.
     * @return value of setting, or the default if not found.
     */
    public Object getObject(ZoneSetting setting) {
        if (setting.getDataType() == Integer.class) {
            return this.getInt(setting);
        } else if (setting.getDataType() == Boolean.class) {
            return this.getBoolean(setting);
        }
        return null;
    }

    /**
     * Set the value of any zone setting. This function will convert the value stored in the string to the appropriate
     * datatype.
     * @param setting Setting to change.
     * @param value value of setting to add or replace.
     * @throws NumberFormatException contents of value are invalid for the type of setting.
     */
    public void setValue(ZoneSetting setting, String value) {
        if (setting.getDataType() == Integer.class) {
            setInt(setting, Integer.parseInt(value));
        } else if (setting.getDataType() == Boolean.class) {
            setBoolean(setting, Boolean.parseBoolean(value));
        }
    }
}
