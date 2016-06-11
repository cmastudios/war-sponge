package com.tommytony.war.zone;

import com.tommytony.war.WarConfig;
import com.tommytony.war.struct.WarCuboid;
import com.tommytony.war.struct.WarLocation;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZoneValidatorTest extends TestCase {
    private ZoneValidator validator;
    private WarConfig config;

    public void setUp() throws Exception {
        super.setUp();
        config = mock(WarConfig.class);
        when(config.getInt(WarConfig.WarSetting.MAXZONESIZE)).thenReturn(1_000_000);
        validator = new ZoneValidator(config);
    }

    public void testValidateName() throws Exception {
        assertEquals(validator.validateName("ziggy"), ZoneValidator.ValidationStatus.VALID);
        assertEquals(validator.validateName("/zone"), ZoneValidator.ValidationStatus.INVALID);
        assertEquals(validator.validateName(".."), ZoneValidator.ValidationStatus.INVALID);
    }

    public void testValidateDimensions() throws Exception {
        WarCuboid tooSmall = new WarCuboid(new WarLocation(0, 0, 0, ""), new WarLocation(3, 3, 3, ""));
        assertEquals(validator.validateDimensions(tooSmall), ZoneValidator.ValidationStatus.INVALID);

        WarCuboid okay = new WarCuboid(new WarLocation(0, 0, 0, ""), new WarLocation(25, 64, 25, ""));
        assertEquals(validator.validateDimensions(okay), ZoneValidator.ValidationStatus.VALID);

        WarCuboid tooBig = new WarCuboid(new WarLocation(0, 0, 0, ""), new WarLocation(1000, 256, 1000, ""));
        assertEquals(validator.validateDimensions(tooBig), ZoneValidator.ValidationStatus.INVALID);
    }

    public void testValidateLocation() throws Exception {
        List<Warzone> zones = new ArrayList<>();
        Warzone zone1 = mock(Warzone.class);
        when(zone1.getCuboid()).thenReturn(new WarCuboid(new WarLocation(0, 0, 0, ""), new WarLocation(50, 128, 50, "")));
        zones.add(zone1);

        WarCuboid far = new WarCuboid(new WarLocation(300, 50, 300, ""), new WarLocation(350, 80, 350, ""));
        assertEquals(validator.validateLocation(far, zones), ZoneValidator.ValidationStatus.VALID);

        WarCuboid stacked = new WarCuboid(new WarLocation(0, 150, 0, ""), new WarLocation(50, 200, 50, ""));
        assertEquals(validator.validateLocation(stacked, zones), ZoneValidator.ValidationStatus.VALID);

        WarCuboid clipped = new WarCuboid(new WarLocation(30, 100, 0, ""), new WarLocation(70, 128, 40, ""));
        assertEquals(validator.validateLocation(clipped, zones), ZoneValidator.ValidationStatus.INVALID);
    }

}