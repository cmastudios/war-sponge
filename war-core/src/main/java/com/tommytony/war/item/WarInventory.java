package com.tommytony.war.item;

public class WarInventory {
    public static final int INVENTORY_LENGTH = 36;
    private WarItem[] contents;
    private WarItem helmet;
    private WarItem chestplate;
    private WarItem leggings;
    private WarItem boots;
    private WarItem offHand;

    public WarInventory() {
        this(new WarItem[36], null, null, null, null, null);
    }

    public WarInventory(WarItem[] contents, WarItem helmet, WarItem chestplate, WarItem leggings, WarItem boots, WarItem offHand) {
        this.contents = contents;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.offHand = offHand;
    }

    public WarItem[] getContents() {
        return contents;
    }

    public WarItem getHelmet() {
        return helmet;
    }

    public WarItem getChestplate() {
        return chestplate;
    }

    public WarItem getLeggings() {
        return leggings;
    }

    public WarItem getBoots() {
        return boots;
    }

    public WarItem getOffHand() {
        return offHand;
    }
}
