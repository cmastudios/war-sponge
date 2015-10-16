package com.tommytony.war;

import com.tommytony.war.struct.WarBlock;
import com.tommytony.war.struct.WarLocation;

import java.io.File;

public interface ServerAPI {
    WarBlock getBlock(WarLocation location);
    void setBlock(WarLocation location, WarBlock block);
    File getDataDir();
    WarConfig getConfig();
}
