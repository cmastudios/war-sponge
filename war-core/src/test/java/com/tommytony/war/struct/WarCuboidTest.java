package com.tommytony.war.struct;

import junit.framework.TestCase;

import java.util.ArrayList;

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
        // basic
        WarLocation c1 = new WarLocation(1.0, 1.0, 1.0, "world");
        WarLocation c2 = new WarLocation(5.0, 5.0, 5.0, "world");
        WarCuboid cuboid = new WarCuboid(c1, c2);

        WarLocation max = cuboid.getMaxBlock();

        assertEquals(max, c2);

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

    public void testIterator() throws Exception {
        WarLocation c1 = new WarLocation(1.0, 1.0, 1.0, "world");
        WarLocation c2 = new WarLocation(5.0, 5.0, 5.0, "world");
        WarCuboid cuboid = new WarCuboid(c1, c2);

        ArrayList<WarLocation> locations = new ArrayList<>();
        int i = 0;
        for (WarLocation block : cuboid) {
            assertTrue(block.getBlockX() >= 1 && block.getBlockX() <= 5);
            assertTrue(block.getBlockY() >= 1 && block.getBlockY() <= 5);
            assertTrue(block.getBlockZ() >= 1 && block.getBlockZ() <= 5);
            i++;
            assertFalse(locations.contains(block));
            locations.add(block);
        }
        assertEquals(i, (int) cuboid.getSize());
    }
}