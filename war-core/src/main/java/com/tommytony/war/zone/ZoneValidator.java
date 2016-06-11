package com.tommytony.war.zone;

import com.tommytony.war.WarConfig;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates aspects of new or changed warzones.
 */
public class ZoneValidator {
    private static final Pattern zoneName = Pattern.compile("[./%]+");
    private final WarConfig config;


    public ZoneValidator(WarConfig config) {
        this.config = config;
    }

    /**
     * Check if the specified name is valid for a zone.
     * Specifically, characters that could be part of a file path or web URL.
     * '.', '/', and '%' are invalid.
     *
     * @param name Potential zone name to check.
     * @return ValidationStatus
     */
    public ValidationStatus validateName(String name) {
        Matcher m = zoneName.matcher(name);
        return m.find() ? ValidationStatus.INVALID : ValidationStatus.VALID;
    }

    /**
     * Check if the dimensions of the warzone are valid.
     * The zone has minimum size limitations as well as a maximum total volume limitation imposed by the administrator.
     *
     * @param cuboid Potential zone region to check.
     * @return valid if size matches.
     */
    public ValidationStatus validateDimensions(WarCuboid cuboid) {
        if (cuboid.getSizeX() < 5 || cuboid.getSizeY() < 4 || cuboid.getSizeZ() < 5
                || cuboid.getSize() > config.getInt(WarConfig.WarSetting.MAXZONESIZE)) {
            return ValidationStatus.INVALID;
        }
        return ValidationStatus.VALID;
    }

    /**
     * Check if the cuboid candidate for the warzone encompasses any other warzones.
     *
     * @param cuboid Candidate cuboid.
     * @param zones List of loaded zones.
     * @return valid if the cuboid contains no part of another warzone.
     */
    public ValidationStatus validateLocation(WarCuboid cuboid, Collection<Warzone> zones) {
        List<WarCuboid> cuboids = new ArrayList<>();
        zones.forEach(z -> cuboids.add(z.getCuboid()));
//        for (WarLocation loc : cuboid) {
//            for (WarCuboid check : cuboids) {
//                if (check.contains(loc)) {
//                    return ValidationStatus.INVALID;
//                }
//            }
//        }
        // this structure below is equivalent to the simpler code above, but is much faster
        // the following code essentially "inlines" all of this
        // I cannot explain why the above code runs slower. I thought it was due to the object creation. I tried to
        //  optimise this by adding a cache on min/max block methods and in the iterator, but those made it SLOWER.
        // Investigate this in the future.
        for (int x = cuboid.getMinBlock().getBlockX(); x <= cuboid.getMaxBlock().getBlockX(); x++) {
            for (int y = cuboid.getMinBlock().getBlockY(); y <= cuboid.getMaxBlock().getBlockY(); y++) {
                for (int z = cuboid.getMinBlock().getBlockZ(); z <= cuboid.getMaxBlock().getBlockZ(); z++) {
                    for (WarCuboid check : cuboids) {
                        if (x >= (check.getCorner1().getX() < check.getCorner2().getX() ? check.getCorner1().getX() : check.getCorner2().getX())
                                && x <= (check.getCorner1().getX() > check.getCorner2().getX() ? check.getCorner1().getX() : check.getCorner2().getX())
                                && y >= (check.getCorner1().getY() < check.getCorner2().getY() ? check.getCorner1().getY() : check.getCorner2().getY())
                                && y <= (check.getCorner1().getY() > check.getCorner2().getY() ? check.getCorner1().getY() : check.getCorner2().getY())
                                && z >= (check.getCorner1().getZ() < check.getCorner2().getZ() ? check.getCorner1().getZ() : check.getCorner2().getZ())
                                && z <= (check.getCorner1().getZ() > check.getCorner2().getZ() ? check.getCorner1().getZ() : check.getCorner2().getZ())) {
                            return ValidationStatus.INVALID;
                        }
                    }
                }
            }
        }
        return ValidationStatus.VALID;
    }

    public enum ValidationStatus {
        INVALID,
        VALID
    }


}
