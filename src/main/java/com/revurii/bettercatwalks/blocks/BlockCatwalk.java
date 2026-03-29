package com.revurii.bettercatwalks.blocks;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.client.model.ModelISBRH;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCatwalk extends Block {

    private static final float PX = 0.0625f;

    private static final AxisAlignedBB[] CATWALK_BOUNDS = {
        // Railing 1
        AxisAlignedBB.getBoundingBox(0, 0.0001, 0, 1, 14 * PX, 2 * PX),
        // Railing 2
        AxisAlignedBB.getBoundingBox(0, 0.0001, 14 * PX, 1, 14 * PX, 16 * PX),
        // Base
        AxisAlignedBB.getBoundingBox(0.0001, 0, 0.0001, 0.9999, 2 * PX, 0.9999) };

    public BlockCatwalk(String unlocalizedName) {
        super(Material.iron);
        this.setBlockName(unlocalizedName);
        this.setCreativeTab(CreativeTabs.tabBlock);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask,
        List<AxisAlignedBB> list, Entity collider) {

        for (AxisAlignedBB bb : CATWALK_BOUNDS) {
            this.setBlockBounds(
                (float) bb.minX,
                (float) bb.minY,
                (float) bb.minZ,
                (float) bb.maxX,
                (float) bb.maxY,
                (float) bb.maxZ);
            super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        }

        // Reset
        this.setBlockBounds(0, 0, 0, 1, 1, 1);

    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, int x, int y, int z, Vec3 startVec, Vec3 endVec) {

        // startVec = point on the bounding box that the player is looking at, based on setBlockBounds()
        // endVec = farthest point the player can reach if not obstructed

        // Get the first AABB in the player's line of sight
        Map.Entry<AxisAlignedBB, MovingObjectPosition> aabbMop = getFirstInterceptedAABBandMOP(
            CATWALK_BOUNDS,
            x,
            y,
            z,
            startVec,
            endVec);
        if (aabbMop != null) {
            MovingObjectPosition mop = aabbMop.getValue();
            mop.blockX = x;
            mop.blockY = y;
            mop.blockZ = z;
            return mop;
        }

        return null;

    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        // Position of the player's eyes in the world
        Vec3 start = player.getPosition(0);

        // Point relative to the player's eyes used to determine their line of sight (-1 to 1 on each axis)
        Vec3 look = player.getLookVec();

        float dist = Minecraft.getMinecraft().playerController.getBlockReachDistance();

        // Farthest point that the player can interact with if not obstructed
        Vec3 end = Vec3.createVectorHelper(
            start.xCoord + look.xCoord * dist,
            start.yCoord + look.yCoord * dist,
            start.zCoord + look.zCoord * dist);

        Map.Entry<AxisAlignedBB, MovingObjectPosition> aabbMop = getFirstInterceptedAABBandMOP(
            CATWALK_BOUNDS,
            x,
            y,
            z,
            start,
            end);

        if (aabbMop != null) return aabbMop.getKey();
        return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);

    }

    // TODO: Prioritize AABB based on face instead of slightly adjusting bounds to prevent flickering at intersections
    protected Map.Entry<AxisAlignedBB, MovingObjectPosition> getFirstInterceptedAABBandMOP(AxisAlignedBB[] aabbs, int x,
        int y, int z, Vec3 start, Vec3 end) {

        // Build a HashMap of AABBs and the MOP that the player is looking at
        HashMap<AxisAlignedBB, MovingObjectPosition> aabbMopMap = new HashMap<>();
        for (AxisAlignedBB aabb : aabbs) {

            // Add x, y, z to min and max of bounds on each axis to position it to the coordinates of the block
            AxisAlignedBB currentBb = aabb.copy();
            currentBb.offset(x, y, z);

            // Get the first point between the start and end vector that is on the bounds of the box
            MovingObjectPosition hit = currentBb.calculateIntercept(start, end);
            if (hit != null) aabbMopMap.put(currentBb, hit);

        }

        // Return the first intercepted AABB and MOP
        if (!aabbMopMap.isEmpty()) return Collections
            .min(aabbMopMap.entrySet(), Comparator.comparingDouble(o -> o.getValue().hitVec.distanceTo(start)));

        return null;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return ModelISBRH.JSON_ISBRH_ID;
    }

    public static class ItemCatwalk extends ItemBlock {

        Block catwalk;

        public ItemCatwalk(Block block) {
            super(block);
            this.catwalk = block;
        }

        /**
         * This method apparently checks for potential collisions before onItemUse() is called
         */
        @Override
        @SideOnly(Side.CLIENT)
        public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack) {

            Vec3 placeVec = getCatwalkPlacementPosition(player, world, x, y, z, side);

            // Check entity collisions for each defined AABB instead of using the superclass method
            for (AxisAlignedBB origBb : CATWALK_BOUNDS) {
                AxisAlignedBB bb = origBb.copy();
                bb.offset(placeVec.xCoord, placeVec.yCoord, placeVec.zCoord);
                if (!world.checkNoEntityCollision(bb)) return false;
            }

            return true;

        }

        @Override
        public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {

            Vec3 placeVec = getCatwalkPlacementPosition(player, world, x, y, z, side);

            // Determine if the catwalk can be placed based on some additional conditions
            if (stack.stackSize == 0) {
                return false;
            } else if (!player
                .canPlayerEdit((int) placeVec.xCoord, (int) placeVec.yCoord, (int) placeVec.zCoord, side, stack)) {
                    return false;
                } else if (y == 255 && this.catwalk.getMaterial()
                    .isSolid()) {
                        return false;
                    } else {
                        if (super.placeBlockAt(
                            stack,
                            player,
                            world,
                            (int) placeVec.xCoord,
                            (int) placeVec.yCoord,
                            (int) placeVec.zCoord,
                            side,
                            hitX,
                            hitY,
                            hitZ,
                            0)) {
                            world.playSoundEffect(
                                placeVec.xCoord + 0.5,
                                placeVec.yCoord + 0.5,
                                placeVec.zCoord + 0.5,
                                this.catwalk.stepSound.func_150496_b(),
                                (this.catwalk.stepSound.getVolume() + 1.0F) / 2.0F,
                                this.catwalk.stepSound.getPitch() * 0.8F);
                            --stack.stackSize;
                        }
                        return true;
                    }
        }

        // TODO: Change this to place based on the position of the cursor on the AABB instead
        protected Vec3 getCatwalkPlacementPosition(EntityPlayer player, World world, int x, int y, int z, int side) {

            // If the player is sneaking or looking at a catwalk, follow normal block placement behavior
            // Otherwise, the catwalk will be placed based on the cardinal direction that the player is facing

            if (player.isSneaking() || world.getBlock(x, y, z) != this.catwalk) {

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

    private static String msg = "";

    private static void log(String s) {
        if (!msg.equals(s)) {
            msg = s;
            System.out.println("LOG: " + msg);
        }
    }

}
