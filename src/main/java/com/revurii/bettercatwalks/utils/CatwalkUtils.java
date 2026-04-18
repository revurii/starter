package com.revurii.bettercatwalks.utils;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.revurii.bettercatwalks.blocks.BlockCatwalk;

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
    public static Vec3 getCatwalkPlacementPosition(EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {

        // If sneaking or not looking at a catwalk, place normally
        // If the base was clicked, place based on where the player is facing
        // If the inner side or top of a railing was clicked, place based on where the player is facing
        // If the outer side of a railing was clicked, place normally

        if (!player.isSneaking() && world.getBlock(x, y, z) instanceof BlockCatwalk catwalk) {

            int meta = world.getBlockMetadata(x, y, z);
            CatwalkBit bit = getCatwalkPartHit(catwalk, player, meta, hitX, hitY, hitZ);

            if (bit != null) {
                AxisAlignedBB space = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
                if (bit == CatwalkBit.BASE
                    || (hitX > space.minX && hitX < space.maxX && hitZ > space.minZ && hitZ < space.maxZ)) {
                    ForgeDirection facing = getCardinalDirection(player);
                    x += facing.offsetX;
                    z += facing.offsetZ;
                    return Vec3.createVectorHelper(x, y, z);
                }
            }

        }

        ForgeDirection sideDirection = ForgeDirection.getOrientation(side);
        x += sideDirection.offsetX;
        y += sideDirection.offsetY;
        z += sideDirection.offsetZ;

        return Vec3.createVectorHelper(x, y, z);

    }

    public static CatwalkBit getCatwalkPartHit(BlockCatwalk catwalk, EntityPlayer player, int meta, float hitX,
        float hitY, float hitZ) {

        CatwalkBit closestBit = null;
        AxisAlignedBB closestBb = null;

        HashMap<CatwalkBit, AxisAlignedBB> bounds = catwalk.getCatwalkBoundsBasedOnState(meta, 0, true);

        for (Map.Entry<CatwalkBit, AxisAlignedBB> box : bounds.entrySet()) {

            AxisAlignedBB aabb = box.getValue();

            if (hitX >= aabb.minX && hitX <= aabb.maxX
                && hitY >= aabb.minY
                && hitY <= aabb.maxY
                && hitZ >= aabb.minZ
                && hitZ <= aabb.maxZ) {

                if (closestBb == null) {

                    closestBit = box.getKey();
                    closestBb = aabb;

                } else {

                    Vec3 hitVec = Vec3.createVectorHelper(hitX, hitY, hitZ);
                    double closestOppositeX = hitVec.xCoord;
                    double closestOppositeZ = hitVec.zCoord;
                    double currentOppositeX = hitVec.xCoord;
                    double currentOppositeZ = hitVec.zCoord;

                    switch (CatwalkUtils.getCardinalDirection(player)) {
                        case EAST -> {
                            closestOppositeX = closestBb.maxX;
                            currentOppositeX = aabb.maxX;
                        }
                        case WEST -> {
                            closestOppositeX = closestBb.minX;
                            currentOppositeX = aabb.minX;
                        }
                        case SOUTH -> {
                            closestOppositeZ = closestBb.maxZ;
                            currentOppositeZ = aabb.maxZ;
                        }
                        case NORTH -> {
                            closestOppositeZ = closestBb.minZ;
                            currentOppositeZ = aabb.minZ;
                        }
                    }

                    Vec3 closestOpposite = Vec3.createVectorHelper(closestOppositeX, hitVec.yCoord, closestOppositeZ);
                    Vec3 currentOpposite = Vec3.createVectorHelper(currentOppositeX, hitVec.yCoord, currentOppositeZ);

                    if (currentOpposite.distanceTo(hitVec) < closestOpposite.distanceTo(hitVec)) {
                        closestBit = box.getKey();
                        closestBb = aabb;
                    }

                }

            }
        }

        return closestBit;

    }

}
