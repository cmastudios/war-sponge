package com.tommytony.war.struct;

import junit.framework.TestCase;

public class WarCuboidTest extends TestCase {

    public void testGetMinBlock() throws Exception {
        // basic
        WarLocation c1 = new WarLocation(1.0, 1.0, 1.0, "world");
        WarLocation c2 = new WarLocation(5.0, 5.0, 5.0, "world");
        WarCuboid cuboid = new WarCuboid(c1, c2);

        WarLocation min = cuboid.getMinBlock();

        assertEquals(min, c1);

        // repeatability
        c1 = new WarLocation(5.0, 38.0, 987.0, "world");
        c2 = new WarLocation(26.0, 35.0, 678.0, "world");
        cuboid = new WarCuboid(c1, c2);
        WarCuboid cuboid2 = new WarCuboid(cuboid.getMinBlock(), cuboid.getMaxBlock());

        assertEquals(cuboid.getMinBlock(), new WarLocation(5.0, 35.0, 678.0, "world"));
        assertEquals(cuboid.getMaxBlock(), new WarLocation(26.0, 38.0, 987.0, "world"));
        assertEquals(cuboid2.getMinBlock(), cuboid2.getCorner1());
        assertEquals(cuboid2.getMaxBlock(), cuboid2.getCorner2());
    }

    public void testGetMaxBlock() throws Exception {

    }
}