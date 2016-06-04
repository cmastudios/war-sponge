package com.tommytony.war.item;

/**
 * Represents a stack of items.
 *
 * @see com.tommytony.war.struct.WarBlock
 */
public class WarItem {
    private final String blockName;
    private final String serialized;
    private final int count;

    /**
     * Creates an item.
     *
     * @param blockName  name of the item, according to the preferred format of the implementation.
     * @param serialized serialized implementation data providing extended information.
     * @param count      number of items in the stack.
     */
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

    /**
     * Generate a name acceptable for viewing by players.
     *
     * @return formatted name.
     */
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
