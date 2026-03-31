package com.revurii.bettercatwalks.blocks;

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
import com.revurii.bettercatwalks.utils.CatwalkUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCatwalk extends Block {

    private static final float PX = 0.0625f;

    private static final AxisAlignedBB[] CATWALK_BOUNDS = {
        // North Railing
        AxisAlignedBB.getBoundingBox(0, 0.0001, 0, 1, 14 * PX, 2 * PX),
        // South Railing
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
        Map.Entry<AxisAlignedBB, MovingObjectPosition> aabbMop = CatwalkUtils
            .getFirstInterceptedAABBandMOP(CATWALK_BOUNDS, x, y, z, startVec, endVec);
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

        Map.Entry<AxisAlignedBB, MovingObjectPosition> aabbMop = CatwalkUtils
            .getFirstInterceptedAABBandMOP(CATWALK_BOUNDS, x, y, z, start, end);

        if (aabbMop != null) return aabbMop.getKey();
        return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);

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

        BlockCatwalk catwalk;

        public ItemCatwalk(Block block) {
            super(block);
            this.catwalk = (BlockCatwalk) block;
        }

        /**
         * This method apparently checks for potential collisions before onItemUse() is called
         */
        @Override
        @SideOnly(Side.CLIENT)
        public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack) {

            Vec3 placeVec = CatwalkUtils.getCatwalkPlacementPosition(player, world, x, y, z, side);

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

            Vec3 placeVec = CatwalkUtils.getCatwalkPlacementPosition(player, world, x, y, z, side);

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

    }

}
