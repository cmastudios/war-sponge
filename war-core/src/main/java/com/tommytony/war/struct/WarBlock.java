package com.tommytony.war.struct;

import java.util.Map;

public class WarBlock {
    private String blockName;
    private Map<String, Object> data;
    private String serialized;

    public WarBlock(String blockName, Map<String, Object> data, String serialized) {
        this.blockName = blockName;
        this.data = data;
        this.serialized = serialized;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getSerialized() {
        return serialized;
    }

    public void setSerialized(String serialized) {
        this.serialized = serialized;
    }
}
