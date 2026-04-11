package com.revurii.bettercatwalks.utils;

import net.minecraftforge.common.util.ForgeDirection;

public enum CatwalkBit {

    WEST("west"),
    EAST("east"),
    NORTH("north"),
    SOUTH("south"),
    BASE("base"),
    IS_UPPER("isUpper");

    public final String name;

    CatwalkBit(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static boolean isActive(int meta, CatwalkBit b) {
        return (meta & 1 << b.ordinal()) >> b.ordinal() == 1;
    }

    public static boolean isActive(int meta, ForgeDirection d) {
        return switch (d) {
            case SOUTH -> isActive(meta, CatwalkBit.SOUTH);
            case NORTH -> isActive(meta, CatwalkBit.NORTH);
            case EAST -> isActive(meta, CatwalkBit.EAST);
            case WEST -> isActive(meta, CatwalkBit.WEST);
            default -> false;
        };
    }

    public static int update(int meta, CatwalkBit bit, Boolean b) {
        return b ? meta | (1 << bit.ordinal()) : meta & ~(1 << bit.ordinal());
    }

}
