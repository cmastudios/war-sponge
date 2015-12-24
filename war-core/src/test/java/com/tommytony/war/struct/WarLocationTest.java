package com.tommytony.war.struct;

import junit.framework.TestCase;

public class WarLocationTest extends TestCase {

    public void testAdd() throws Exception {
        WarLocation pos1 = new WarLocation(100, 100, 100, "");
        WarLocation pos2 = new WarLocation(50, 10, 50, "");
        WarLocation expected = new WarLocation(150, 110, 150, "");

        assertEquals(pos1.add(pos2), expected);
        assertEquals(pos2.add(pos1), expected);
    }

    public void testSub() throws Exception {
        WarLocation pos1 = new WarLocation(100, 100, 100, "");
        WarLocation pos2 = new WarLocation(150, 110, 150, "");
        WarLocation expected2Minus1 = new WarLocation(50, 10, 50, "");
        WarLocation expected1Minus2 = new WarLocation(-50, -10, -50, "");

        assertEquals(pos1.sub(pos2), expected1Minus2);
        assertEquals(pos2.sub(pos1), expected2Minus1);
    }
}