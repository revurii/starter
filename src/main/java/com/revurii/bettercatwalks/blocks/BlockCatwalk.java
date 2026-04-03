package com.revurii.bettercatwalks.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.blockstate.core.BlockState;
import com.gtnewhorizon.gtnhlib.blockstate.registry.BlockPropertyRegistry;
import com.gtnewhorizon.gtnhlib.client.model.ModelISBRH;
import com.revurii.bettercatwalks.tileentities.TileEntityCatwalk;
import com.revurii.bettercatwalks.utils.CatwalkConstants;
import com.revurii.bettercatwalks.utils.CatwalkUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCatwalk extends BlockContainer {

    private static final AxisAlignedBB BASE_BOUNDS = AxisAlignedBB
        .getBoundingBox(0.0001, 0, 0.0001, 0.9999, 0.1250, 0.9999);
    private static final AxisAlignedBB SOUTH_BOUNDS = AxisAlignedBB
        .getBoundingBox(0.0000, 0.0001, 0.8750, 1.0000, 1, 1.0000);
    private static final AxisAlignedBB NORTH_BOUNDS = AxisAlignedBB
        .getBoundingBox(0.0000, 0.0001, 0.0000, 1.0000, 1, 0.1250);
    private static final AxisAlignedBB EAST_BOUNDS = AxisAlignedBB
        .getBoundingBox(0.8750, 0.0001, 0.0000, 1.0000, 1, 1.000);
    private static final AxisAlignedBB WEST_BOUNDS = AxisAlignedBB
        .getBoundingBox(0.0000, 0.0001, 0.0000, 0.1250, 1, 1.000);

    public BlockCatwalk(String unlocalizedName) {
        super(Material.iron);
        this.setBlockName(unlocalizedName);
        this.setCreativeTab(CreativeTabs.tabBlock);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask,
        List<AxisAlignedBB> list, Entity collider) {

        // mask = bounding box of the entity colliding with the block (northwest bottom corner to southeast top corner)
        // list = add bounding boxes that will be active here

        this.setBlockBoundsBasedOnState(worldIn, x, y, z);

        for (AxisAlignedBB bb : getCatwalkBoundsBasedOnState(worldIn, x, y, z, 0.5)) {
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
        this.setBlockBoundsBasedOnState(worldIn, x, y, z);

    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        return super.getCollisionBoundingBoxFromPool(worldIn, x, y, z);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        BlockState state = BlockPropertyRegistry.getBlockState(worldIn, x, y, z);
        if (state.getPropertyValue(CatwalkConstants.PROPERTY_HALF) instanceof String s) {
            if (s.equals(CatwalkConstants.PROPERTY_HALF_TOP)) {
                this.setBlockBounds(0, 0, 0, 1, 3, 1);
                return;
            }
        }

        this.setBlockBounds(0, 0, 0, 1, 1, 1);
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, int x, int y, int z, Vec3 startVec, Vec3 endVec) {

        // TODO: Fix railing collision ray trace when catwalk is in top half state

        // startVec = point on the bounding box that the player is looking at, based on min x, y, z to max x, y, z
        // endVec = farthest point the player can reach if not obstructed

        // Get the first AABB in the player's line of sight
        Map.Entry<AxisAlignedBB, MovingObjectPosition> aabbMop = CatwalkUtils.getFirstInterceptedAABBandMOP(
            getCatwalkBoundsBasedOnState(worldIn, x, y, z, 0),
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

        // TODO: Fix railing selection box display when catwalk is in top half state

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
            .getFirstInterceptedAABBandMOP(getCatwalkBoundsBasedOnState(worldIn, x, y, z, 0), x, y, z, start, end);

        if (aabbMop != null) return aabbMop.getKey();
        return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);

    }

    /**
     * Retrieve a list of AABBs based on which half is active and which faces are active on the catwalk.
     * These AABBs are not yet positioned to the catwalk.
     *
     * @param railHeightIncrease will increase the bounds on the railings by the set amount, for use with collision
     *                           bounds
     */
    public List<AxisAlignedBB> getCatwalkBoundsBasedOnState(World worldIn, int x, int y, int z,
        double railHeightIncrease) {

        // Determine list of possible bounding boxes to be selected based on the state of the catwalk
        BlockState state = BlockPropertyRegistry.getBlockState(worldIn, x, y, z);

        List<AxisAlignedBB> aabbs = new ArrayList<>();
        if (state.getPropertyValue(CatwalkConstants.PROPERTY_SOUTH) instanceof Boolean south && south) aabbs.add(
            SOUTH_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));
        if (state.getPropertyValue(CatwalkConstants.PROPERTY_NORTH) instanceof Boolean south && south) aabbs.add(
            NORTH_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));
        if (state.getPropertyValue(CatwalkConstants.PROPERTY_EAST) instanceof Boolean south && south) aabbs.add(
            EAST_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));
        if (state.getPropertyValue(CatwalkConstants.PROPERTY_WEST) instanceof Boolean south && south) aabbs.add(
            WEST_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));

        if (state.getPropertyValue(CatwalkConstants.PROPERTY_HALF) instanceof String s) {

            aabbs.add(BASE_BOUNDS.copy());

            // Move up all boxes by 14 pixels if catwalk is in the top half state
            if (s.equals(CatwalkConstants.PROPERTY_HALF_TOP)) {
                aabbs.forEach(aabb -> aabb.offset(0, 0.8750, 0));
            }

        }

        return aabbs;
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

    // Tile Entity-related methods

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityCatwalk();
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
            // TODO: Check block replaceability (?) since this currently deletes blocks in the way
            Vec3 placeVec = CatwalkUtils.getCatwalkPlacementPosition(player, world, x, y, z, side);

            // Check entity collisions for each defined AABB instead of using the superclass method
            for (AxisAlignedBB origBb : catwalk.getCatwalkBoundsBasedOnState(world, x, y, z, 0)) {
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
