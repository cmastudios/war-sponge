package com.tommytony.war.struct;

public class WarCuboid {
    private WarLocation corner1, corner2;

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
}
