package com.tommytony.war.zone;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarLocation;

import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the war zone database file, which contains all the data for the war zone.
 */
public class ZoneStorage implements AutoCloseable {
    private static int DATABASE_VERSION = 1;
    private final Warzone zone;
    private final Connection connection;
    private final File dataStore;
    private final ServerAPI plugin;
    private final Map<String, WarLocation> positionCache;

    /**
     * Initiates a database for a new or existing database.
     *
     * @param zone   The server war zone object for this database.
     * @param plugin The war plugin, for storage information and configuration.
     * @throws SQLException
     */
    ZoneStorage(Warzone zone, ServerAPI plugin) throws SQLException {
        this.zone = zone;
        this.plugin = plugin;
        dataStore = new File(plugin.getDataDir(), String.format("%s.warzone", zone.getName()));
        connection = DriverManager.getConnection("jdbc:sqlite:" + dataStore.getPath());
        positionCache = new HashMap<>();
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
            stmt.executeUpdate("CREATE TABLE block_ids (id INTEGER PRIMARY KEY, name TEXT) WITHOUT ROWID");
            stmt.executeUpdate("CREATE TABLE blocks (x NUMERIC, y NUMERIC, z NUMERIC, id INTEGER, data BLOB)");
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

    WarLocation dbToWorld(WarLocation location) throws SQLException {
        WarLocation position1 = this.getPosition("position1");
        return position1.add(location);
    }

    WarLocation worldToDb(WarLocation location) throws SQLException {
        WarLocation position1 = this.getPosition("position1");
        return location.sub(position1);
    }

    /**
     * Look up a position in the coordinates table.
     *
     * @param name  Name of stored position.
     * @return the location of the position, or absent if not found.
     * @throws SQLException
     */
    public WarLocation getPosition(String name) throws SQLException {
        if (positionCache.containsKey(name)) {
            return positionCache.get(name);
        }
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT x, y, z, world FROM coordinates WHERE name = ?")) {
            stmt.setString(1, name);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    WarLocation warLocation = new WarLocation(resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"), resultSet.getString("world"));
                    if (!name.equals("position1")) {
                        warLocation = dbToWorld(warLocation);
                    }
                    positionCache.put(name, warLocation);
                    return warLocation;
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
        if (!name.equals("position1")) {
            location = worldToDb(location);
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, location.getX());
            stmt.setDouble(2, location.getY());
            stmt.setDouble(3, location.getZ());
            stmt.setString(4, location.getWorld());
            stmt.setString(5, name);
            stmt.execute();
        }
        positionCache.clear();
    }

    public void loadBlocks() throws SQLException {
        Map<Integer, String> blockIds = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, name FROM block_ids"
        )) {
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    blockIds.put(result.getInt("id"), result.getString("name"));
                }
            }
        }
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT x, y, z, id, data FROM blocks"
        )) {
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    String name = blockIds.get(result.getInt("id"));
                    String serialized = result.getString("data");
                    WarLocation loc = new WarLocation(result.getInt("x"), result.getInt("y"), result.getInt("z"),
                            this.getPosition("position1").getWorld());
                    loc = dbToWorld(loc);
                    WarBlock block = new WarBlock(name, null, serialized, (short) 0);
                    plugin.setBlock(loc, block);
                }
            }
        }
    }

    private static int BATCH_SIZE = 10000;

    public void saveBlocks() throws SQLException {
        long startTime = System.currentTimeMillis();
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM block_ids");
            stmt.executeUpdate("DELETE FROM blocks");
        }
        Map<String, Integer> blockIds = new HashMap<>();
        int i = 0, changed = 0;
        connection.setAutoCommit(false);
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO blocks (x, y, z, id, data) VALUES (?, ?, ?, ?, ?)"
        )) {
            for (WarLocation loc : zone.getCuboid()) {
                WarBlock block = plugin.getBlock(loc, false);
                if (!blockIds.containsKey(block.getBlockName())) {
                    blockIds.put(block.getBlockName(), i++);
                }
                stmt.setInt(1, worldToDb(loc).getBlockX());
                stmt.setInt(2, worldToDb(loc).getBlockY());
                stmt.setInt(3, worldToDb(loc).getBlockZ());
                stmt.setInt(4, blockIds.get(block.getBlockName()));
                stmt.setString(5, block.getSerialized());
                stmt.addBatch();
                if (++changed % BATCH_SIZE == 0) {
                    stmt.executeBatch();
                    if ((System.currentTimeMillis() - startTime) >= 5000L) {
                        String seconds = new DecimalFormat("#0.00").format((double) (System.currentTimeMillis() - startTime) / 1000.0D);
                        plugin.logInfo("Still saving zone " + zone.getName() + ", " + seconds + " seconds elapsed.");
                    }
                }
            }
            stmt.executeBatch();
        }
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO block_ids (id, name) VALUES (?, ?)"
        )) {
            for (Map.Entry<String, Integer> e : blockIds.entrySet()) {
                stmt.setInt(1, e.getValue());
                stmt.setString(2, e.getKey());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
        connection.commit();
        connection.setAutoCommit(true);
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
