package com.tommytony.war.struct;

/**
 * Represents the position of a block or player in a world.
 */
public class WarLocation {
    private double x, y, z;
    private String world;
    private double pitch, yaw;

    /**
     * Create a new location based on player orientation.
     *
     * @param x     position in X dimension.
     * @param y     position in Y dimension.
     * @param z     position in Z dimension.
     * @param world world containing the location.
     * @param pitch head pitch of player.
     * @param yaw   head yaw of player.
     */
    public WarLocation(double x, double y, double z, String world, double pitch, double yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    /**
     * Create a new location based on block position.
     *
     * @param x position in X dimension.
     * @param y position in Y dimension.
     * @param z position in Z dimension.
     * @param world world containing the block.
     */
    public WarLocation(double x, double y, double z, String world) {
        this(x, y, z, world, 0, 0);
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

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
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

    /**
     * Get location of the block rounded to integer values.
     *
     * @return location.
     */
    public WarLocation getBlockLoc() {
        return new WarLocation(getBlockX(), getBlockY(), getBlockZ(), world, 0, 0);
    }

    @Override
    public String toString() {
        return String.format("x: %d, y: %d, z: %d", getBlockX(), getBlockY(), getBlockZ());
    }

    /**
     * Add all components of both locations together.
     *
     * @param two second location.
     * @return arithmetic sum of two locations.
     */
    public WarLocation add(WarLocation two) {
        return new WarLocation(this.x + two.x, this.y + two.y, this.z + two.z, this.world, pitch + two.pitch, yaw + two.yaw);
    }

    /**
     * Subtract the components of location two from this location.
     * @param two subtrahend.
     * @return difference between both locations.
     */
    public WarLocation sub(WarLocation two) {
        return new WarLocation(this.x - two.x, this.y - two.y, this.z - two.z, this.world, pitch - two.pitch, yaw - two.yaw);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WarLocation that = (WarLocation) o;

        if (Double.compare(that.x, x) != 0) return false;
        if (Double.compare(that.y, y) != 0) return false;
        if (Double.compare(that.z, z) != 0) return false;
        if (Double.compare(that.pitch, pitch) != 0) return false;
        if (Double.compare(that.yaw, yaw) != 0) return false;
        return world != null ? world.equals(that.world) : that.world == null;

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
        result = 31 * result + (world != null ? world.hashCode() : 0);
        temp = Double.doubleToLongBits(pitch);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(yaw);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
