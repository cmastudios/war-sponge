package com.tommytony.war;

import com.tommytony.war.item.WarEntity;
import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;
import com.tommytony.war.zone.Warzone;

import java.io.File;

public interface ServerAPI {
    /**
     * Get information from the server about the block at a specific location.
     * @param location Location of the block.
     * @param cheap true to skip loading extended data (chests, tile ent).
     * @return block suitable for use by war-core.
     */
    WarBlock getBlock(WarLocation location, boolean cheap);

    /**
     * Set a location in the world to a specific block. Does not update physics.
     * @param location Future location of block.
     * @param block Information on the block to place.
     */
    void setBlock(WarLocation location, WarBlock block);

    /**
     * Remove entities from the world in a specific area.
     * @param cuboid Region to affect.
     *               @see WarCuboid#entireWorld(String)
     * @param type Types of entities to remove.
     */
    void removeEntity(WarCuboid cuboid, WarEntity type);

    /**
     * Folder for War storage.
     * @return config folder.
     */
    File getDataDir();

    /**
     * Server wide War config and default ZoneConfig values.
     * @see com.tommytony.war.zone.ZoneConfig
     * @see com.tommytony.war.zone.ZoneSetting
     * @return server config.
     */
    WarConfig getWarConfig();

    /**
     * Print a message to the server console.
     * Format: [War] : *message*
     * @param message Message to print
     */
    void logInfo(String message);

    /**
     * Get infomation about a loaded warzone.
     *
     * @param zoneName Warzone currently in memory.
     * @return Warzone object or null if not found.
     */
    Warzone getZone(String zoneName);

    /**
     * Create a warzone.
     *
     * @param zoneName Name of the warzone to create.
     * @return Created warzone object.
     */
    Warzone createZone(String zoneName);

    /**
     * Delete a warzone.
     *
     * @param zoneName Name of the warzone to delete.
     * @return Path to deleted zone database on filesystem.
     */
    String deleteZone(String zoneName);
}
