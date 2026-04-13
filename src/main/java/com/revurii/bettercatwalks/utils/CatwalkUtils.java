package com.revurii.bettercatwalks.utils;

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

        AxisAlignedBB closestBb = null;
        MovingObjectPosition closestMop = null;

        for (AxisAlignedBB aabb : aabbs) {

            // Add x, y, z to min and max of bounds on each axis to position it to the coordinates of the block
            AxisAlignedBB currentBb = aabb.copy();
            currentBb.offset(x, y, z);

            // Get the first point between the start and end vector that is on the bounds of the box
            MovingObjectPosition hit = currentBb.calculateIntercept(start, end);
            if (hit != null) {

                if (closestBb == null) {
                    closestBb = currentBb;
                    closestMop = hit;
                    continue;
                }

                if (hit.hitVec.distanceTo(start) < closestMop.hitVec.distanceTo(start)) {
                    closestBb = currentBb;
                    closestMop = hit;
                    continue;
                }

                // If two bounding boxes were hit, use the box that has the closest opposite face
                // TODO: Use the box that has the closest opposite face based on the direction the player is looking
                // instead of the hit vector
                // force up down when pitch is +/- 60 deg
                // DOWN -> +y to -y
                // UP -> -y to +y
                // SOUTH -> -z to +z
                // NORTH -> +z to -z
                // EAST -> -x to +x
                // WEST -> +x to -x
                if (hit.hitVec.distanceTo(start) == closestMop.hitVec.distanceTo(start)) {

                    Vec3 hitVec = hit.hitVec;
                    double closestOppositeX = hitVec.xCoord;
                    double closestOppositeY = hitVec.yCoord;
                    double closestOppositeZ = hitVec.zCoord;
                    double currentOppositeX = hitVec.xCoord;
                    double currentOppositeY = hitVec.yCoord;
                    double currentOppositeZ = hitVec.zCoord;

                    if (hitVec.xCoord == closestBb.minX) closestOppositeX = closestBb.maxX;
                    if (hitVec.xCoord == closestBb.maxX) closestOppositeX = closestBb.minX;
                    if (hitVec.yCoord == closestBb.minY) closestOppositeY = closestBb.maxY;
                    if (hitVec.yCoord == closestBb.maxY) closestOppositeY = closestBb.minY;
                    if (hitVec.zCoord == closestBb.minZ) closestOppositeZ = closestBb.maxZ;
                    if (hitVec.zCoord == closestBb.maxZ) closestOppositeZ = closestBb.minZ;

                    if (hitVec.xCoord == currentBb.minX) currentOppositeX = currentBb.maxX;
                    if (hitVec.xCoord == currentBb.maxX) currentOppositeX = currentBb.minX;
                    if (hitVec.yCoord == currentBb.minY) currentOppositeY = currentBb.maxY;
                    if (hitVec.yCoord == currentBb.maxY) currentOppositeY = currentBb.minY;
                    if (hitVec.zCoord == currentBb.minZ) currentOppositeZ = currentBb.maxZ;
                    if (hitVec.zCoord == currentBb.maxZ) currentOppositeZ = currentBb.minZ;

                    Vec3 closestOpposite = Vec3
                        .createVectorHelper(closestOppositeX, closestOppositeY, closestOppositeZ);
                    Vec3 currentOpposite = Vec3
                        .createVectorHelper(currentOppositeX, currentOppositeY, currentOppositeZ);

                    if (currentOpposite.distanceTo(hitVec) < closestOpposite.distanceTo(hitVec)) {
                        closestBb = currentBb;
                        closestMop = hit;
                    }

                }

            }

        }

        // Return the first intercepted AABB and its MOP
        if (closestBb != null) {
            HashMap<AxisAlignedBB, MovingObjectPosition> closest = new HashMap<>();
            closest.put(closestBb, closestMop);
            return closest.entrySet()
                .stream()
                .findFirst()
                .get();
        }

        return null;
    }

}
