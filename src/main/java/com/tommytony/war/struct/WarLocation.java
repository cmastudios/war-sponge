package com.tommytony.war.struct;

public class WarLocation {
    private double x, y, z;
    private String world;

    public WarLocation(double x, double y, double z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }

    public int getBlockX() {
        return (int) Math.floor(x);
    }

    public int getBlockY() {
        return (int) Math.floor(y);
    }

    public int getBlockZ() {
        return (int) Math.floor(z);
    }

    public WarLocation getBlockLoc() {
        return new WarLocation(getBlockX(), getBlockY(), getBlockZ(), world);
    }

    @Override
    public String toString() {
        return String.format("x: %d, y: %d, z: %d", getBlockX(), getBlockY(), getBlockZ());
    }
}
