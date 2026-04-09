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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.gtnhlib.blockstate.core.BlockState;
import com.gtnewhorizon.gtnhlib.blockstate.registry.BlockPropertyRegistry;
import com.gtnewhorizon.gtnhlib.client.model.ModelISBRH;
import com.revurii.bettercatwalks.ModBlocks;
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
        this.setBlockBounds(0, 0, 0, 1, 2, 1);

    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        return super.getCollisionBoundingBoxFromPool(worldIn, x, y, z);
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, int x, int y, int z, Vec3 startVec, Vec3 endVec) {

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
        List<AxisAlignedBB> aabbs = new ArrayList<>();

        TileEntity te = worldIn.getTileEntity(x, y, z);
        if (te instanceof TileEntityCatwalk teCatwalk) {

            if (teCatwalk.getRailing(CatwalkConstants.PROPERTY_SOUTH)) aabbs.add(
                SOUTH_BOUNDS.copy()
                    .addCoord(0, railHeightIncrease, 0));
            if (teCatwalk.getRailing(CatwalkConstants.PROPERTY_NORTH)) aabbs.add(
                NORTH_BOUNDS.copy()
                    .addCoord(0, railHeightIncrease, 0));
            if (teCatwalk.getRailing(CatwalkConstants.PROPERTY_EAST)) aabbs.add(
                EAST_BOUNDS.copy()
                    .addCoord(0, railHeightIncrease, 0));
            if (teCatwalk.getRailing(CatwalkConstants.PROPERTY_WEST)) aabbs.add(
                WEST_BOUNDS.copy()
                    .addCoord(0, railHeightIncrease, 0));

            aabbs.add(BASE_BOUNDS.copy());

            // Move up all boxes by 14 pixels if catwalk is in the top half state
            if (teCatwalk.getHalf()
                .equals(CatwalkConstants.PROPERTY_HALF_TOP)) aabbs.forEach(aabb -> aabb.offset(0, 0.8750, 0));

        }

        // int meta = 0b1000;
        // if ((meta & 0b1000) >> 3 == 1) aabbs.add(SOUTH_BOUNDS.copy().addCoord(0, railHeightIncrease, 0));
        // if ((meta & 0b0100) >> 3 == 1) aabbs.add(NORTH_BOUNDS.copy().addCoord(0, railHeightIncrease, 0));
        // if ((meta & 0b0010) >> 3 == 1) aabbs.add(EAST_BOUNDS.copy().addCoord(0, railHeightIncrease, 0));
        // if ((meta & 0b0001) >> 3 == 1) aabbs.add(WEST_BOUNDS.copy().addCoord(0, railHeightIncrease, 0));
        // aabbs.add(BASE_BOUNDS.copy());
        //
        // // Move up all boxes by 14 pixels if catwalk is in the top half state
        // if (this.isUpper) aabbs.forEach(aabb -> aabb.offset(0, 0.8750, 0));

        return aabbs;
    }

    public List<AxisAlignedBB> getCatwalkBoundsOnPlace(String half, boolean south, boolean north, boolean east,
        boolean west, double railHeightIncrease) {

        List<AxisAlignedBB> aabbs = new ArrayList<>();
        if (south) aabbs.add(
            SOUTH_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));
        if (north) aabbs.add(
            NORTH_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));
        if (east) aabbs.add(
            EAST_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));
        if (west) aabbs.add(
            WEST_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));
        aabbs.add(BASE_BOUNDS.copy());

        // Move up all boxes by 14 pixels if catwalk is in the top half state
        if (half.equals(CatwalkConstants.PROPERTY_HALF_TOP)) aabbs.forEach(aabb -> aabb.offset(0, 0.8750, 0));

        return aabbs;

    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {

        BlockState state = BlockPropertyRegistry.getBlockState(worldIn, x, y, z);
        NBTTagCompound tag = itemIn.getTagCompound();

        state.setPropertyValue(CatwalkConstants.PROPERTY_HALF, tag.getString(CatwalkConstants.PROPERTY_HALF));
        state.setPropertyValue(CatwalkConstants.PROPERTY_SOUTH, tag.getBoolean(CatwalkConstants.PROPERTY_SOUTH));
        state.setPropertyValue(CatwalkConstants.PROPERTY_NORTH, tag.getBoolean(CatwalkConstants.PROPERTY_NORTH));
        state.setPropertyValue(CatwalkConstants.PROPERTY_EAST, tag.getBoolean(CatwalkConstants.PROPERTY_EAST));
        state.setPropertyValue(CatwalkConstants.PROPERTY_WEST, tag.getBoolean(CatwalkConstants.PROPERTY_WEST));
        state.place(worldIn, x, y, z);
        state.close();

        // Disable the touching railings of any adjacent catwalks if they have the same half property as the placed
        // catwalk
        String half = tag.getString(CatwalkConstants.PROPERTY_HALF);

        if (worldIn.getBlock(x, y, z + 1) == ModBlocks.CATWALK.get()) {
            BlockState southState = BlockPropertyRegistry.getBlockState(worldIn, x, y, z + 1);
            if (southState.getPropertyValue(CatwalkConstants.PROPERTY_HALF) instanceof String s && s.equals(half)) {
                southState.setPropertyValue(CatwalkConstants.PROPERTY_NORTH, false);
                southState.place(worldIn, x, y, z + 1);
                southState.close();
            }
        }

        if (worldIn.getBlock(x, y, z - 1) == ModBlocks.CATWALK.get()) {
            BlockState northState = BlockPropertyRegistry.getBlockState(worldIn, x, y, z - 1);
            if (northState.getPropertyValue(CatwalkConstants.PROPERTY_HALF) instanceof String s && s.equals(half)) {
                northState.setPropertyValue(CatwalkConstants.PROPERTY_SOUTH, false);
                northState.place(worldIn, x, y, z - 1);
                northState.close();
            }
        }

        if (worldIn.getBlock(x + 1, y, z) == ModBlocks.CATWALK.get()) {
            BlockState eastState = BlockPropertyRegistry.getBlockState(worldIn, x + 1, y, z);
            if (eastState.getPropertyValue(CatwalkConstants.PROPERTY_HALF) instanceof String s && s.equals(half)) {
                eastState.setPropertyValue(CatwalkConstants.PROPERTY_WEST, false);
                eastState.place(worldIn, x + 1, y, z);
                eastState.close();
            }
        }

        if (worldIn.getBlock(x - 1, y, z) == ModBlocks.CATWALK.get()) {
            BlockState westState = BlockPropertyRegistry.getBlockState(worldIn, x - 1, y, z);
            if (westState.getPropertyValue(CatwalkConstants.PROPERTY_HALF) instanceof String s && s.equals(half)) {
                westState.setPropertyValue(CatwalkConstants.PROPERTY_EAST, false);
                westState.place(worldIn, x - 1, y, z);
                westState.close();
            }
        }

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
         * This method apparently checks for potential collisions before onItemUse() is called, which has been moved
         * to onItemUse()
         */
        @Override
        @SideOnly(Side.CLIENT)
        public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack) {
            return true;
        }

        @Override
        public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {

            Vec3 placeVec = CatwalkUtils.getCatwalkPlacementPosition(player, world, x, y, z, side);
            int placeX = MathHelper.floor_double(placeVec.xCoord);
            int placeY = MathHelper.floor_double(placeVec.yCoord);
            int placeZ = MathHelper.floor_double(placeVec.zCoord);

            // Determine if the catwalk can be placed based on some additional conditions
            if (stack.stackSize == 0 || !player.canPlayerEdit(placeX, placeY, placeZ, side, stack)
                || y == 255
                || !world.getBlock(placeX, placeY, placeZ)
                    .isReplaceable(world, x, y, z)
                || !catwalk.canReplace(world, placeX, placeY, placeZ, side, stack)) return false;

            // Default state: bottom half and enable all railings
            String half = CatwalkConstants.PROPERTY_HALF_BOTTOM;
            boolean south = true;
            boolean north = true;
            boolean east = true;
            boolean west = true;

            TileEntity teLook = world.getTileEntity(x, y, z);
            ForgeDirection facing = CatwalkUtils.getCardinalDirection(player);

            if (teLook instanceof TileEntityCatwalk tecLook) {

                // If looking at a catwalk, copy its half and perpendicular railing properties
                half = tecLook.getHalf();

                switch (facing) {
                    case SOUTH, NORTH -> {
                        east = tecLook.getRailing(CatwalkConstants.PROPERTY_EAST);
                        west = tecLook.getRailing(CatwalkConstants.PROPERTY_WEST);
                    }
                    case EAST, WEST -> {
                        south = tecLook.getRailing(CatwalkConstants.PROPERTY_SOUTH);
                        north = tecLook.getRailing(CatwalkConstants.PROPERTY_NORTH);
                    }
                }

                // If a catwalk perpendicular to the catwalk the player is looking at has its closest perpendicular
                // railing to where the catwalk will be placed enabled, also enable the railing perpendicular to the
                // player to "continue" the railings

                TileEntity southLookTile = world.getTileEntity(x, y, z + 1);
                TileEntity northLookTile = world.getTileEntity(x, y, z - 1);
                TileEntity eastLookTile = world.getTileEntity(x + 1, y, z);
                TileEntity westLookTile = world.getTileEntity(x - 1, y, z);

                if (player.isSneaking()) {
                    switch (facing) {
                        case SOUTH -> {
                            if (eastLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isNorthActive()) east = true;
                            if (westLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isNorthActive()) west = true;
                        }
                        case NORTH -> {
                            if (eastLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isSouthActive()) east = true;
                            if (westLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isSouthActive()) west = true;
                        }
                        case EAST -> {
                            if (southLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isWestActive()) south = true;
                            if (northLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isWestActive()) north = true;
                        }
                        case WEST -> {
                            if (southLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isEastActive()) south = true;
                            if (northLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isEastActive()) north = true;
                        }
                    }
                } else {
                    switch (facing) {
                        case SOUTH -> {
                            if (eastLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isSouthActive()) east = true;
                            if (westLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isSouthActive()) west = true;
                        }
                        case NORTH -> {
                            if (eastLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isNorthActive()) east = true;
                            if (westLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isNorthActive()) west = true;
                        }
                        case EAST -> {
                            if (southLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isEastActive()) south = true;
                            if (northLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isEastActive()) north = true;
                        }
                        case WEST -> {
                            if (southLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isWestActive()) south = true;
                            if (northLookTile instanceof TileEntityCatwalk tec && tec.getHalf()
                                .equals(half) && tec.isWestActive()) north = true;
                        }
                    }
                }

            } else {

                // If not looking at a catwalk, set the half property based on the hitY value
                // and the side that the player is looking at; do not place railings along the axis that
                // the player is facing

                if (hitY >= 0.5 && side != 0 && side != 1) half = CatwalkConstants.PROPERTY_HALF_TOP;

                switch (facing) {
                    case SOUTH, NORTH -> {
                        south = false;
                        north = false;
                    }
                    case EAST, WEST -> {
                        east = false;
                        west = false;
                    }
                }

            }

            // Finally, always disable railings that are adjacent to catwalks with the same half property
            if (world.getTileEntity(placeX, placeY, placeZ + 1) instanceof TileEntityCatwalk tecSouth
                && tecSouth.getHalf()
                    .equals(half))
                south = false;
            if (world.getTileEntity(placeX, placeY, placeZ - 1) instanceof TileEntityCatwalk tecNorth
                && tecNorth.getHalf()
                    .equals(half))
                north = false;
            if (world.getTileEntity(placeX + 1, placeY, placeZ) instanceof TileEntityCatwalk tecEast
                && tecEast.getHalf()
                    .equals(half))
                east = false;
            if (world.getTileEntity(placeX - 1, placeY, placeZ) instanceof TileEntityCatwalk tecWest
                && tecWest.getHalf()
                    .equals(half))
                west = false;

            // Determine if the catwalk will collide with anything before placing
            List<AxisAlignedBB> aabbs = catwalk.getCatwalkBoundsOnPlace(half, south, north, east, west, 0.5);
            for (AxisAlignedBB aabb : aabbs) {
                aabb.offset(placeX, placeY, placeZ);
                if (!world.checkNoEntityCollision(aabb)) return false;
            }

            NBTTagCompound tag = new NBTTagCompound();
            tag.setString(CatwalkConstants.PROPERTY_HALF, half);
            tag.setBoolean(CatwalkConstants.PROPERTY_SOUTH, south);
            tag.setBoolean(CatwalkConstants.PROPERTY_NORTH, north);
            tag.setBoolean(CatwalkConstants.PROPERTY_EAST, east);
            tag.setBoolean(CatwalkConstants.PROPERTY_WEST, west);

            ItemStack fake = stack.copy();
            fake.stackSize = 1;
            fake.stackTagCompound = tag;

            if (super.placeBlockAt(fake, player, world, placeX, placeY, placeZ, side, hitX, hitY, hitZ, 0)) {
                world.playSoundEffect(
                    placeX + 0.5,
                    placeY + 0.5,
                    placeZ + 0.5,
                    this.catwalk.stepSound.func_150496_b(),
                    (this.catwalk.stepSound.getVolume() + 1.0F) / 2.0F,
                    this.catwalk.stepSound.getPitch() * 0.8F);
                --stack.stackSize;
                --fake.stackSize;
            }
            return true;
        }

    }

}
