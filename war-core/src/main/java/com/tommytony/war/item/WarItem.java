package com.tommytony.war.item;

public class WarItem {
    private final String blockName;
    private final String serialized;
    private final int count;

    public WarItem(String blockName, String serialized, int count) {
        this.blockName = blockName;
        this.serialized = serialized;
        this.count = count;
    }

    public String getBlockName() {
        return blockName;
    }

    public String getSerialized() {
        return serialized;
    }

    public int getCount() {
        return count;
    }

    public String getDisplayName() {
        String name = blockName;
        if (name.startsWith("minecraft:")) {
            name = name.substring(10);
        }
        name = name.replace("_", " ").toLowerCase();
        if (name.startsWith("a") || name.startsWith("e") || name.startsWith("i") || name.startsWith("o") || name.startsWith("u")) {
            name = "an " + name;
        } else {
            name = "a " + name;
        }
        return name;
    }
}
