package com.revurii.bettercatwalks.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
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

    /**
     * Get the first intercepted AABB between the start and end vector from a list of AABBs to be offset by the
     * given x, y, z values
     */
    public static Map.Entry<AxisAlignedBB, MovingObjectPosition> getFirstInterceptedAABBandMOP(
        List<AxisAlignedBB> aabbs, int x, int y, int z, Vec3 start, Vec3 end) {

        // Build a HashMap of AABBs and the MOP containing the position of the intercept
        HashMap<AxisAlignedBB, MovingObjectPosition> aabbMopMap = new HashMap<>();
        for (AxisAlignedBB aabb : aabbs) {

            // Add x, y, z to min and max of bounds on each axis to position it to the coordinates of the block
            AxisAlignedBB currentBb = aabb.copy();
            currentBb.offset(x, y, z);

            // Get the first point between the start and end vector that is on the bounds of the box
            MovingObjectPosition hit = currentBb.calculateIntercept(start, end);
            if (hit != null) aabbMopMap.put(currentBb, hit);

        }

        // Return the first intercepted AABB and its MOP
        if (!aabbMopMap.isEmpty()) return Collections
            .min(aabbMopMap.entrySet(), Comparator.comparingDouble(o -> o.getValue().hitVec.distanceTo(start)));

        return null;
    }

    /**
     * Get the first intercepted AABB between the start and end vector from a list of AABBs
     */
    public static Map.Entry<AxisAlignedBB, MovingObjectPosition> getFirstInterceptedAABBandMOP(
        List<AxisAlignedBB> aabbs, Vec3 start, Vec3 end) {
        return getFirstInterceptedAABBandMOP(aabbs, 0, 0, 0, start, end);
    }

}
