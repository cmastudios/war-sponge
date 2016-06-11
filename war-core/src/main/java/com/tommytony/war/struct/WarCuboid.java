package com.tommytony.war.struct;

import java.util.Iterator;
import java.util.List;

/**
 * Region of space containing locations.
 */
public class WarCuboid implements Iterable<WarLocation> {
    private final WarLocation corner1, corner2;
    private List<WarLocation> blocks;

    /**
     * Create a new cuboid from two locations. The locations do not have to be maximums or minimums; they may be any two
     * corners of the region.
     *
     * @param corner1 first position.
     * @param corner2 second position.
     */
    public WarCuboid(WarLocation corner1, WarLocation corner2) {
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    /**
     * Get a cuboid containing the entire world.
     *
     * @param world world.
     * @return cuboid.
     */
    public static WarCuboid entireWorld(String world) {
        return new WarCuboid(new WarLocation(-3000000, 0, -3000000, world), new WarLocation(3000000, 255, 3000000, world));
    }

    public WarLocation getCorner1() {
        return corner1;
    }

    public WarLocation getCorner2() {
        return corner2;
    }

    /**
     * Get the corner of the region which has the smallest value in the x, y, and z dimensions.
     *
     * @return location.
     */
    public WarLocation getMinBlock() {
        return new WarLocation(corner1.getX() < corner2.getX() ? corner1.getX() : corner2.getX(),
                corner1.getY() < corner2.getY() ? corner1.getY() : corner2.getY(),
                corner1.getZ() < corner2.getZ() ? corner1.getZ() : corner2.getZ(), corner1.getWorld());
    }

    /**
     * Get the corner of the region which has the greatest value in the x, y, and z dimensions.
     *
     * @return location.
     */
    public WarLocation getMaxBlock() {
        return new WarLocation(corner1.getX() > corner2.getX() ? corner1.getX() : corner2.getX(),
                corner1.getY() > corner2.getY() ? corner1.getY() : corner2.getY(),
                corner1.getZ() > corner2.getZ() ? corner1.getZ() : corner2.getZ(), corner1.getWorld());
    }

    /**
     * Get the length of the x axis.
     *
     * @return length.
     */
    public double getSizeX() {
        return getMaxBlock().getX() - getMinBlock().getX() + 1;
    }

    /**
     * Get the length of the y axis.
     *
     * @return length.
     */
    public double getSizeY() {
        return getMaxBlock().getY() - getMinBlock().getY() + 1;
    }

    /**
     * Get the length of the z axis.
     *
     * @return length.
     */
    public double getSizeZ() {
        return getMaxBlock().getZ() - getMinBlock().getZ() + 1;
    }

    /**
     * Get the volume of the cuboid.
     *
     * @return volume.
     */
    public double getSize() {
        return getSizeX() * getSizeY() * getSizeZ();
    }

    /**
     * Check if this cuboid contains a particular location.
     *
     * @param location location to check
     * @return true if location is contained within this cuboid.
     */
    public boolean contains(WarLocation location) {
        return location.getX() >= getMinBlock().getX() && location.getX() <= getMaxBlock().getX()
                && location.getY() >= getMinBlock().getY() && location.getY() <= getMaxBlock().getY()
                && location.getZ() >= getMinBlock().getZ() && location.getZ() <= getMaxBlock().getZ();
    }

    @Override
    public String toString() {
        return String.format("%dx%dx%d", (int) Math.floor(getSizeX()), (int) Math.floor(getSizeY()), (int) Math.floor(getSizeZ()));
    }

    /**
     * Iterate across all block locations contained in this cuboid.
     *
     * @return block iterator.
     */
    @Override

    public Iterator<WarLocation> iterator() {
        return new WarCuboidIterator(this);
    }

    private class WarCuboidIterator implements Iterator<WarLocation> {
        private final WarCuboid cuboid;
        private int i;

        WarCuboidIterator(WarCuboid cuboid) {
            this.cuboid = cuboid;
            i = 0;
        }

        @Override
        public boolean hasNext() {
            int li = 0;
            for (int x = cuboid.getMinBlock().getBlockX(); x <= cuboid.getMaxBlock().getBlockX(); x++) {
                for (int y = cuboid.getMinBlock().getBlockY(); y <= cuboid.getMaxBlock().getBlockY(); y++) {
                    for (int z = cuboid.getMinBlock().getBlockZ(); z <= cuboid.getMaxBlock().getBlockZ(); z++) {
                        if (li++ == i) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public WarLocation next() {
            int li = 0;
            for (int x = cuboid.getMinBlock().getBlockX(); x <= cuboid.getMaxBlock().getBlockX(); x++) {
                for (int y = cuboid.getMinBlock().getBlockY(); y <= cuboid.getMaxBlock().getBlockY(); y++) {
                    for (int z = cuboid.getMinBlock().getBlockZ(); z <= cuboid.getMaxBlock().getBlockZ(); z++) {
                        if (li++ == i) {
                            i++;
                            return new WarLocation(x, y, z, cuboid.getMinBlock().getWorld());
                        }
                    }
                }
            }
            throw new IllegalStateException("Iterator out");
        }
    }
}
