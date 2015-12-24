package com.tommytony.war.zone;

import com.tommytony.war.struct.WarLocation;
import junit.framework.TestCase;

import static org.mockito.Mockito.*;

public class ZoneStorageTest extends TestCase {
    public void testRelativePositionsReversibility() throws Exception {
        WarLocation pos1 = new WarLocation(100, 100, 100, "");
        WarLocation pos2 = new WarLocation(150, 110, 150, "");

        ZoneStorage storage = mock(ZoneStorage.class);
        when(storage.getPosition("position1")).thenReturn(pos1);
        when(storage.dbToWorld(anyObject())).thenCallRealMethod();
        when(storage.worldToDb(anyObject())).thenCallRealMethod();

        assertEquals(pos2, storage.dbToWorld(storage.worldToDb(pos2)));
    }
}