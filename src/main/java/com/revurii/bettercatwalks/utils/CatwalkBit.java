package com.revurii.bettercatwalks.utils;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public enum CatwalkBit {

    WEST("west", ForgeDirection.WEST, AxisAlignedBB.getBoundingBox(0.0000, 0.1250, 0.0000, 0.1250, 1.0000, 1.0000),
        AxisAlignedBB.getBoundingBox(0.0000, 0.0000, 0.0000, 0.1250, 0.1250, 1.0000)),
    EAST("east", ForgeDirection.EAST, AxisAlignedBB.getBoundingBox(0.8750, 0.1250, 0.0000, 1.0000, 1.0000, 1.0000),
        AxisAlignedBB.getBoundingBox(0.8750, 0.0000, 0.0000, 1.0000, 0.1250, 1.0000)),
    NORTH("north", ForgeDirection.NORTH, AxisAlignedBB.getBoundingBox(0.0000, 0.1250, 0.0000, 1.0000, 1.0000, 0.1250),
        AxisAlignedBB.getBoundingBox(0.0000, 0.0000, 0.0000, 1.0000, 0.1250, 0.1250)),
    SOUTH("south", ForgeDirection.SOUTH, AxisAlignedBB.getBoundingBox(0.0000, 0.1250, 0.8750, 1.0000, 1.0000, 1.0000),
        AxisAlignedBB.getBoundingBox(0.0000, 0.0000, 0.8750, 1.0000, 0.1250, 1.0000)),
    BASE("base", AxisAlignedBB.getBoundingBox(0.0000, 0.0000, 0.0000, 1.0000, 0.1250, 1.0000),
        AxisAlignedBB.getBoundingBox(0.3750, 0.0000, 0.3750, 0.6250, 0.1250, 0.6250)),
    IS_UPPER("isUpper");

    private final String name;
    private final ForgeDirection direction;
    private final AxisAlignedBB enabledAabb;
    private final AxisAlignedBB disabledAabb;

    CatwalkBit(String name, ForgeDirection direction, AxisAlignedBB enabledAabb, AxisAlignedBB disabledAabb) {
        this.name = name;
        this.direction = direction;
        this.enabledAabb = enabledAabb;
        this.disabledAabb = disabledAabb;
    }

    CatwalkBit(String name, AxisAlignedBB enabledAabb, AxisAlignedBB disabledAabb) {
        this.name = name;
        this.direction = null;
        this.enabledAabb = enabledAabb;
        this.disabledAabb = disabledAabb;
    }

    CatwalkBit(String name) {
        this.name = name;
        this.direction = null;
        this.enabledAabb = null;
        this.disabledAabb = null;
    }

    public String toString() {
        return this.name;
    }

    public boolean isRailing() {
        return direction != null;
    }

    public AxisAlignedBB getBounds(boolean isEnabled) {
        if (isEnabled && this.enabledAabb != null) return this.enabledAabb.copy();
        if (this.disabledAabb != null) return this.disabledAabb.copy();
        return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
    }

    public static List<CatwalkBit> getAllBounds(boolean railingsOnly) {
        List<CatwalkBit> list = new ArrayList<>();
        for (CatwalkBit bit : values()) {
            if ((!railingsOnly || bit.isRailing()) && bit.enabledAabb != null) list.add(bit);
        }
        return list;
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
