package com.tommytony.war.struct;

import java.util.Iterator;
import java.util.List;

public class WarCuboid implements Iterable<WarLocation> {
    private WarLocation corner1, corner2;
    private List<WarLocation> blocks;

    public WarCuboid(WarLocation corner1, WarLocation corner2) {
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public WarLocation getCorner1() {
        return corner1;
    }

    public WarLocation getCorner2() {
        return corner2;
    }

    public WarLocation getMinBlock() {
        return new WarLocation(corner1.getX() < corner2.getX() ? corner1.getX() : corner2.getX(),
                corner1.getY() < corner2.getY() ? corner1.getY() : corner2.getY(),
                corner1.getZ() < corner2.getZ() ? corner1.getZ() : corner2.getZ(), corner1.getWorld());
    }

    public WarLocation getMaxBlock() {
        return new WarLocation(corner1.getX() > corner2.getX() ? corner1.getX() : corner2.getX(),
                corner1.getY() > corner2.getY() ? corner1.getY() : corner2.getY(),
                corner1.getZ() > corner2.getZ() ? corner1.getZ() : corner2.getZ(), corner1.getWorld());
    }

    public double getSizeX() {
        return getMaxBlock().getX() - getMinBlock().getX() + 1;
    }

    public double getSizeY() {
        return getMaxBlock().getY() - getMinBlock().getY() + 1;
    }

    public double getSizeZ() {
        return getMaxBlock().getZ() - getMinBlock().getZ() + 1;
    }

    public double getSize() {
        return getSizeX() * getSizeY() * getSizeZ();
    }

    @Override
    public String toString() {
        return String.format("%dx%dx%d", (int) Math.floor(getSizeX()), (int) Math.floor(getSizeY()), (int) Math.floor(getSizeZ()));
    }

    @Override
    public Iterator<WarLocation> iterator() {
        return new WarCuboidIterator(this);
    }

    public class WarCuboidIterator implements Iterator<WarLocation> {
        private final WarCuboid cuboid;
        private int i;

        public WarCuboidIterator(WarCuboid cuboid) {
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
