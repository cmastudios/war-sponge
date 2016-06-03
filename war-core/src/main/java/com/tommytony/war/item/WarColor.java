package com.tommytony.war.item;

public enum WarColor {
    NAVY(0x1),
    BLUE(0x9),
    CYAN(0x3),
    AQUA(0xb),
    FIRE(0x4),
    RED(0xc),
    YELLOW(0xe),
    GOLD(0x6),
    LIME(0x2),
    GREEN(0xa),
    PURPLE(0x5),
    PINK(0xd),
    WHITE(0xf),
    SILVER(0x7),
    GRAY(0x8),
    BLACK(0x0);

    private final int code;

    WarColor(int i) {
        this.code = i;
    }

    public String getCode() {
        return String.format("§%x", code);
    }

    @Override
    public String toString() {
        return getCode();
    }
}
