package com.tommytony.war.zone;

import com.tommytony.war.ServerAPI;
import com.tommytony.war.item.WarInventory;
import com.tommytony.war.item.WarItem;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarLocation;

import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the war zone database file, which contains all the data for the war zone.
 */
class ZoneStorage implements AutoCloseable {
    private static int DATABASE_VERSION = 2;
    private static int BATCH_SIZE = 10000;
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

    File getDataStore() {
        return dataStore;
    }

    /**
     * Check the database stored version information and perform upgrade tasks if necessary.
     *
     * @throws SQLException
     */
    private void upgradeDatabase() throws SQLException {
        int version;
        Statement stmt = connection.createStatement();
        try (ResultSet resultSet = stmt.executeQuery("PRAGMA user_version")) {
            version = resultSet.getInt("user_version");
        }
        if (version > DATABASE_VERSION) {
            // version is from a future version
            throw new IllegalStateException(String.format("Unsupported zone version: %d. War current version: %d",
                    version, DATABASE_VERSION));
        } else if (version < DATABASE_VERSION) {
            // upgrade
            switch (version) {
                case 0:
                    // brand new database file
                    stmt.executeUpdate("CREATE TABLE coordinates (name TEXT UNIQUE, x NUMERIC, y NUMERIC, z NUMERIC, pitch NUMERIC, yaw NUMERIC, world TEXT)");
                    stmt.executeUpdate("CREATE TABLE block_ids (id INTEGER, name TEXT)");
                    stmt.executeUpdate("CREATE TABLE blocks (x NUMERIC, y NUMERIC, z NUMERIC, id INTEGER, meta INTEGER, data BLOB)");
                    stmt.executeUpdate("PRAGMA user_version = 1");
                case 1:
                    stmt.executeUpdate("CREATE TABLE inv_labels (id INTEGER PRIMARY KEY, label TEXT UNIQUE)");
                    stmt.executeUpdate("CREATE TABLE inventories (inv_id INTEGER, item_id INTEGER, name TEXT, data TEXT, size INTEGER)");
                    stmt.executeUpdate("PRAGMA user_version = 2");
                    break;
                default:
                    // some odd bug or people messing with their database
                    throw new IllegalStateException(String.format("Unsupported zone version: %d.", version));
            }
        }
        stmt.close();
    }

    /**
     * Converts a database position to a real world coordinate. Calculated by adding the position1 back to the saved
     * location.
     *
     * @param location location to convert.
     * @return world location.
     * @throws SQLException
     */
    WarLocation dbToWorld(WarLocation location) throws SQLException {
        WarLocation position1 = this.getPosition("position1");
        return position1.add(location);
    }

    /**
     * Converts a real world position to a database position. Calculated from the difference between the location and
     * the zone's position1.
     *
     * @param location location to convert.
     * @return database location.
     * @throws SQLException
     */
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
    WarLocation getPosition(String name) throws SQLException {
        if (positionCache.containsKey(name)) {
            return positionCache.get(name);
        }
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT x, y, z, pitch, yaw, world FROM coordinates WHERE name = ?")) {
            stmt.setString(1, name);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    WarLocation warLocation = new WarLocation(resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"),
                            resultSet.getString("world"), resultSet.getDouble("pitch"), resultSet.getDouble("yaw"));
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

    /**
     * Check if a position exists in the zone storage.
     *
     * @param name position to check.
     * @return true if the database contains this position.
     * @throws SQLException
     */
    boolean hasPosition(String name) throws SQLException {
        return getPosition(name) != null;
    }

    /**
     * Gets a list of teams based on values in the positions database.
     *
     * @return list of teams
     * @throws SQLException
     */
    List<String> getTeams() throws SQLException {
        List<String> teams = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT name FROM coordinates")) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    if (name.startsWith("teamspawn")) {
                        teams.add(name.substring(9));
                    }
                }
            }
        }
        return teams;
    }

    /**
     * Get a list of gates to this warzone and their locations.
     *
     * @return gate names and locations
     * @throws SQLException
     */
    Map<WarLocation, String> getGates() throws SQLException {
        Map<WarLocation, String> gates = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT x, y, z, pitch, yaw, world, name FROM coordinates")) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    WarLocation warLocation = new WarLocation(resultSet.getDouble("x"), resultSet.getDouble("y"), resultSet.getDouble("z"),
                            resultSet.getString("world"), resultSet.getDouble("pitch"), resultSet.getDouble("yaw"));
                    if (!name.equals("position1")) {
                        warLocation = dbToWorld(warLocation);
                    }
                    if (name.startsWith("gate")) {
                        gates.put(warLocation, name.substring(4));
                    }
                }
            }
        }
        return gates;
    }

    /**
     * Set a position in the coordinates table to a location.
     *
     * @param name     Name of position in storage.
     * @param location Location to write to the storage.
     * @throws SQLException
     */
    void setPosition(String name, WarLocation location) throws SQLException {
        String sql;
        if (this.hasPosition(name)) {
            sql = "UPDATE coordinates SET x = ?, y = ?, z = ?, world = ?, pitch = ?, yaw = ? WHERE name = ?";
        } else {
            sql = "INSERT INTO coordinates (x, y, z, world, pitch, yaw, name) VALUES (?, ?, ?, ?, ?, ?, ?)";
        }
        if (!name.equals("position1")) {
            location = worldToDb(location);
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, location.getX());
            stmt.setDouble(2, location.getY());
            stmt.setDouble(3, location.getZ());
            stmt.setString(4, location.getWorld());
            stmt.setDouble(5, location.getPitch());
            stmt.setDouble(6, location.getYaw());
            stmt.setString(7, name);
            stmt.executeUpdate();
        }
        positionCache.clear();
    }

    /**
     * Remove a position from the coordinates table.
     *
     * @param name Name of position in storage.
     * @throws SQLException
     */
    void deletePosition(String name) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM coordinates WHERE name = ?")) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        }
    }

    /**
     * Load all blocks from the database into the world.
     * @throws SQLException
     */
    void loadBlocks() throws SQLException {
        Map<Integer, String> blockIds = new HashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, name FROM block_ids"
        )) {
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    blockIds.put(result.getInt("id"), result.getString("name"));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Exception fired while loading block IDs", e);
        }
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT x, y, z, id, meta, data FROM blocks"
        )) {
            try (ResultSet result = stmt.executeQuery()) {
                while (result.next()) {
                    String name = blockIds.get(result.getInt("id"));
                    String serialized = result.getString("data");
                    WarLocation loc = new WarLocation(result.getInt("x"), result.getInt("y"), result.getInt("z"),
                            this.getPosition("position1").getWorld());
                    loc = dbToWorld(loc);
                    short meta = result.getShort("meta");
                    WarBlock block = new WarBlock(name, null, serialized, meta);
                    plugin.setBlock(loc, block);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Exception fired while loading block data.", e);
        }
    }

    /**
     * Save blocks in the world to the database.
     * @throws SQLException
     */
    void saveBlocks() throws SQLException {
        long startTime = System.currentTimeMillis();
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM block_ids");
            stmt.executeUpdate("DELETE FROM blocks");
        } catch (SQLException e) {
            throw new SQLException("Failed to delete old warzone data from the database.", e);
        }
        Map<String, Integer> blockIds = new HashMap<>();
        int i = 0, changed = 0;
        connection.setAutoCommit(false);
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO blocks (x, y, z, id, meta, data) VALUES (?, ?, ?, ?, ?, ?)"
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
                stmt.setShort(5, block.getMeta());
                stmt.setString(6, block.getSerialized());
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
        } catch (SQLException e) {
            throw new SQLException("Failed to insert block information.", e);
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
        } catch (SQLException e) {
            throw new SQLException("Failed to insert block IDs.", e);
        }
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Save an inventory to the zone storage.
     *
     * @param name      Name of inventory to store.
     * @param inventory Name and contents of inventory to store.
     * @throws SQLException
     */
    void saveInventory(String name, WarInventory inventory) throws SQLException {
        int inv_id;
        if (!containsInventory(name)) {
            try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO inv_labels (id, label) VALUES (null, ?)")) {
                stmt.setString(1, name);
                stmt.executeUpdate();
            }
        }
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id FROM inv_labels WHERE label = ? LIMIT 1")) {
            stmt.setString(1, name);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    inv_id = resultSet.getInt("id");
                } else {
                    throw new SQLException("Failed to find label id.");
                }
            }
        }
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM inventories WHERE inv_id = ?")) {
            stmt.setInt(1, inv_id);
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO inventories (inv_id, item_id, name, data, size) VALUES (?, ?, ?, ?, ?)")) {
            WarItem[] contents = inventory.getContents();
            for (int i = 0; i < WarInventory.INVENTORY_LENGTH + 5; i++) {
                WarItem item;
                if (i < contents.length) {
                    item = contents[i];
                } else if (i - WarInventory.INVENTORY_LENGTH == 0) {
                    item = inventory.getHelmet();
                } else if (i - WarInventory.INVENTORY_LENGTH == 1) {
                    item = inventory.getChestplate();
                } else if (i - WarInventory.INVENTORY_LENGTH == 2) {
                    item = inventory.getLeggings();
                } else if (i - WarInventory.INVENTORY_LENGTH == 3) {
                    item = inventory.getBoots();
                } else if (i - WarInventory.INVENTORY_LENGTH == 4) {
                    item = inventory.getOffHand();
                } else {
                    continue;
                }
                if (item == null) {
                    continue;
                }
                stmt.setInt(1, inv_id);
                stmt.setInt(2, i);
                stmt.setString(3, item.getBlockName());
                stmt.setString(4, item.getSerialized());
                stmt.setInt(5, item.getCount());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    /**
     * Retrieve all items in inventory from the database.
     *
     * @param name name of inventory to lookup.
     * @return inventory, or null if not found.
     * @throws SQLException
     */
    WarInventory getInventory(String name) throws SQLException {
        int inv_id;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id FROM inv_labels WHERE label = ? LIMIT 1")) {
            stmt.setString(1, name);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    inv_id = resultSet.getInt("id");
                } else {
                    return null;
                }
            }
        }
        WarItem[] contents = new WarItem[WarInventory.INVENTORY_LENGTH];
        WarItem helmet = null, chestplate = null, leggings = null, boots = null, offHand = null;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT item_id, name, data, size FROM inventories WHERE inv_id = ?")) {
            stmt.setInt(1, inv_id);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    int i = resultSet.getInt("item_id");
                    WarItem item = new WarItem(resultSet.getString("name"), resultSet.getString("data"), resultSet.getInt("size"));
                    if (i - WarInventory.INVENTORY_LENGTH == 0) {
                        helmet = item;
                    } else if (i - WarInventory.INVENTORY_LENGTH == 1) {
                        chestplate = item;
                    } else if (i - WarInventory.INVENTORY_LENGTH == 2) {
                        leggings = item;
                    } else if (i - WarInventory.INVENTORY_LENGTH == 3) {
                        boots = item;
                    } else if (i - WarInventory.INVENTORY_LENGTH == 4) {
                        offHand = item;
                    } else if (i < WarInventory.INVENTORY_LENGTH) {
                        contents[i] = item;
                    }
                }
            }
        }
        return new WarInventory(contents, helmet, chestplate, leggings, boots, offHand);
    }

    /**
     * Check if the database contains an inventory.
     *
     * @param name Inventory name to check.
     * @return true if inventory exists, false otherwise.
     * @throws SQLException
     */
    boolean containsInventory(String name) throws SQLException {
        return getInventory(name) != null;
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
