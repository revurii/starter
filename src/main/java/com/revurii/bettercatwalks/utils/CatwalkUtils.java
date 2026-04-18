package com.revurii.bettercatwalks.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.revurii.bettercatwalks.ModBlocks;

public class CatwalkUtils {

    public static ForgeDirection getCardinalDirection(EntityPlayer player) {

        // Translate yaw to the value seen in F3 (-180 to 180)
        float yaw = player.rotationYaw;
        yaw %= 360;

        if (yaw > 180) {
            yaw -= 360;
        } else if (yaw < -180) {
            yaw += 360;
        }

        if (yaw >= -45 && yaw < 45) {
            return ForgeDirection.SOUTH;
        } else if (yaw >= 45 && yaw < 135) {
            return ForgeDirection.WEST;
        } else if (yaw >= 135 && yaw <= 180 || yaw >= -180 && yaw < -135) {
            return ForgeDirection.NORTH;
        } else if (yaw >= -135 && yaw < -45) {
            return ForgeDirection.EAST;
        }

        return ForgeDirection.UNKNOWN;
    }

    /**
     * Get the position that a catwalk would be placed in given the position and side of the block the player
     * right-clicked while holding a catwalk
     */
    public static Vec3 getCatwalkPlacementPosition(EntityPlayer player, World world, int x, int y, int z, int side) {

        // If the player is sneaking or not looking at a catwalk, follow normal block placement behavior
        // Otherwise, the catwalk will be placed based on the cardinal direction that the player is facing

        if (player.isSneaking() || world.getBlock(x, y, z) != ModBlocks.CATWALK.get()) {

            switch (side) {
                case 0 -> y--; // BOTTOM
                case 1 -> y++; // TOP
                case 2 -> z--; // NORTH
                case 3 -> z++; // SOUTH
                case 4 -> x--; // WEST
                case 5 -> x++; // EAST
            }

        } else {

            // Translate yaw to the value seen in F3 (-180 to 180)
            float yaw = player.rotationYaw;
            yaw %= 360;

            if (yaw > 180) {
                yaw -= 360;
            } else if (yaw < -180) {
                yaw += 360;
            }

            if (yaw >= -45 && yaw < 45) {
                z++; // SOUTH
            } else if (yaw >= 45 && yaw < 135) {
                x--; // WEST
            } else if (yaw >= 135 && yaw <= 180 || yaw >= -180 && yaw < -135) {
                z--; // NORTH
            } else if (yaw >= -135 && yaw < -45) {
                x++; // EAST
            }

        }

        return Vec3.createVectorHelper(x, y, z);
    }

}
