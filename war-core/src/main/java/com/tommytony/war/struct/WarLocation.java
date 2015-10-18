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

    public WarLocation add(WarLocation two) {
        return new WarLocation(this.x + two.x, this.y + two.y, this.z + two.z, this.world);
    }

    public WarLocation sub(WarLocation two) {
        return new WarLocation(this.x - two.x, this.y - two.y, this.z - two.z, this.world);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WarLocation that = (WarLocation) o;

        if (Double.compare(that.x, x) != 0) return false;
        if (Double.compare(that.y, y) != 0) return false;
        if (Double.compare(that.z, z) != 0) return false;
        return world.equals(that.world);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + world.hashCode();
        return result;
    }
}
