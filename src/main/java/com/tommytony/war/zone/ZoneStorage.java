package com.tommytony.war.zone;

import com.tommytony.war.WarPlugin;
import com.tommytony.war.struct.WarLocation;

import java.io.File;
import java.sql.*;

/**
 * Manages the war zone database file, which contains all the data for the war zone.
 */
public class ZoneStorage implements AutoCloseable {
    private static int DATABASE_VERSION = 1;
    private final Warzone zone;
    private final WarPlugin plugin;
    private final Connection connection;
    private final File dataStore;

    /**
     * Initiates a database for a new or existing database.
     *
     * @param zone   The server war zone object for this database.
     * @param plugin The war plugin, for storage information and configuration.
     * @throws SQLException
     */
    ZoneStorage(Warzone zone, WarPlugin plugin) throws SQLException {
        this.zone = zone;
        this.plugin = plugin;
        dataStore = new File(plugin.getDataDir(), String.format("%s.warzone", zone.getName()));
        connection = DriverManager.getConnection("jdbc:sqlite:" + dataStore.getPath());
        this.upgradeDatabase();
    }

    Connection getConnection() {
        return connection;
    }

    public File getDataStore() {
        return dataStore;
    }

    /**
     * Check the database stored version information and perform upgrade tasks if necessary.
     *
     * @throws SQLException
     */
    private void upgradeDatabase() throws SQLException {
        int version;
        try (
                Statement stmt = connection.createStatement();
                ResultSet resultSet = stmt.executeQuery("PRAGMA user_version")
        ) {
            version = resultSet.getInt("user_version");
        }
        if (version > DATABASE_VERSION) {
            // version is from a future version
            throw new IllegalStateException(String.format("Unsupported zone version: %d. War current version: %d",
                    version, DATABASE_VERSION));
        } else if (version == 0) {
            // brand new database file
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE TABLE coordinates (name TEXT UNIQUE, x NUMERIC, y NUMERIC, z NUMERIC, world TEXT)");
            stmt.executeUpdate(String.format("PRAGMA user_version = %d", DATABASE_VERSION));
        } else if (version < DATABASE_VERSION) {
            // upgrade
            switch (version) {
                // none yet
                default:
                    // some odd bug or people messing with their database
                    throw new IllegalStateException(String.format("Unsupported zone version: %d.", version));
            }
        }
    }

    /**
     * Look up a position in the coordinates table.
     *
     * @param name  Name of stored position.
     * @return the location of the position, or absent if not found.
     * @throws SQLException
     */
    public WarLocation getPosition(String name) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT x, y, z, world FROM coordinates WHERE name = ?")) {
            stmt.setString(1, name);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return new WarLocation(resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"), resultSet.getString("world"));
                }
            }
        }
        return null;
    }

    public boolean hasPosition(String name) throws SQLException {
        return getPosition(name) != null;
    }

    /**
     * Set a position in the coordinates table to a location.
     *
     * @param name     Name of position in storage.
     * @param location Location to write to the storage.
     * @throws SQLException
     */
    public void setPosition(String name, WarLocation location) throws SQLException {
        String sql;
        if (this.hasPosition(name)) {
            sql = "UPDATE coordinates SET x = ?, y = ?, z = ?, world = ? WHERE name = ?";
        } else {
            sql = "INSERT INTO coordinates (x, y, z, world, name) VALUES (?, ?, ?, ?, ?)";
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, location.getX());
            stmt.setDouble(2, location.getY());
            stmt.setDouble(3, location.getZ());
            stmt.setString(4, location.getWorld());
            stmt.setString(5, name);
            stmt.execute();
        }
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        connection.close();
    }
}
