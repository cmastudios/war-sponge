package com.tommytony.war.zone;

import com.tommytony.war.WarConfig;
import com.tommytony.war.struct.WarCuboid;

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

    public enum ValidationStatus {
        INVALID,
        VALID
    }


}
