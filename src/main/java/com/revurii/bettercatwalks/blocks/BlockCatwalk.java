package com.revurii.bettercatwalks.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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
import com.revurii.bettercatwalks.items.ItemBlowtorch;
import com.revurii.bettercatwalks.utils.CatwalkBit;
import com.revurii.bettercatwalks.utils.CatwalkUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCatwalk extends Block {

    // Each bit in the metadata will represent whether a railing on a specific side is active or not
    // ex. 0b000000 -> IS_UPPER, BASE, SOUTH, NORTH, EAST, WEST

    private static final AxisAlignedBB BASE_BOUNDS = AxisAlignedBB
        .getBoundingBox(0.0001, 0.0000, 0.0000, 1.0000, 0.1250, 1.0000);
    private static final AxisAlignedBB SOUTH_BOUNDS = AxisAlignedBB
        .getBoundingBox(0.0000, 0.1250, 0.8750, 1.0000, 1.0000, 1.0000);
    private static final AxisAlignedBB NORTH_BOUNDS = AxisAlignedBB
        .getBoundingBox(0.0000, 0.1250, 0.0000, 1.0000, 1.0000, 0.1250);
    private static final AxisAlignedBB EAST_BOUNDS = AxisAlignedBB
        .getBoundingBox(0.8750, 0.1250, 0.0000, 1.0000, 1.0000, 1.0000);
    private static final AxisAlignedBB WEST_BOUNDS = AxisAlignedBB
        .getBoundingBox(0.0000, 0.1250, 0.0000, 0.1250, 1.0000, 1.0000);

    public BlockCatwalk(String unlocalizedName) {
        super(Material.iron);
        this.setBlockName(unlocalizedName);
        this.setCreativeTab(CreativeTabs.tabMisc);
        this.setStepSound(soundTypeMetal);
        this.setHardness(1.5F);
        this.setHarvestLevel("pickaxe", 0);
    }

    @Override
    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask,
        List<AxisAlignedBB> list, Entity collider) {

        // mask = bounding box of the entity colliding with the block (northwest bottom corner to southeast top corner)
        // list = add bounding boxes that will be active here

        for (AxisAlignedBB bb : getCatwalkBoundsBasedOnState(worldIn.getBlockMetadata(x, y, z), 0.5)) {
            this.setBlockBounds(
                (float) bb.minX,
                (float) bb.minY,
                (float) bb.minZ,
                (float) bb.maxX,
                (float) bb.maxY,
                (float) bb.maxZ);
            super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        }

        // Reset Bounds
        this.setBlockBounds(0, 0, 0, 1, 2, 1);

    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, int x, int y, int z, Vec3 startVec, Vec3 endVec) {

        // startVec = point on the bounding box that the player is looking at, based on min x, y, z to max x, y, z
        // endVec = farthest point the player can reach if not obstructed

        // Get the first AABB in the player's line of sight
        Map.Entry<AxisAlignedBB, MovingObjectPosition> aabbMop = CatwalkUtils.getFirstInterceptedAABBandMOP(
            getCatwalkBoundsBasedOnState(worldIn.getBlockMetadata(x, y, z), 0),
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

        Map.Entry<AxisAlignedBB, MovingObjectPosition> aabbMop = CatwalkUtils.getFirstInterceptedAABBandMOP(
            getCatwalkBoundsBasedOnState(worldIn.getBlockMetadata(x, y, z), 0),
            x,
            y,
            z,
            start,
            end);

        if (aabbMop != null) return aabbMop.getKey();
        return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);

    }

    /**
     * Retrieve a list of AABBs based on which half is active and which faces are active on the catwalk.
     * These AABBs are not yet positioned to the catwalk.
     *
     * @param railHeightIncrease will increase the height of the railings by the set amount, for use with collisions
     */
    public List<AxisAlignedBB> getCatwalkBoundsBasedOnState(int meta, double railHeightIncrease) {

        // Determine list of possible bounding boxes to be selected based on the state of the catwalk
        List<AxisAlignedBB> aabbs = new ArrayList<>();

        if (CatwalkBit.isActive(meta, CatwalkBit.BASE)) aabbs.add(BASE_BOUNDS.copy());

        if (CatwalkBit.isActive(meta, CatwalkBit.SOUTH)) aabbs.add(
            SOUTH_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));
        if (CatwalkBit.isActive(meta, CatwalkBit.NORTH)) aabbs.add(
            NORTH_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));
        if (CatwalkBit.isActive(meta, CatwalkBit.EAST)) aabbs.add(
            EAST_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));
        if (CatwalkBit.isActive(meta, CatwalkBit.WEST)) aabbs.add(
            WEST_BOUNDS.copy()
                .addCoord(0, railHeightIncrease, 0));

        // Move up all boxes by 14 pixels if catwalk is in the top half state
        if (CatwalkBit.isActive(meta, CatwalkBit.IS_UPPER)) aabbs.forEach(aabb -> aabb.offset(0, 0.8750, 0));

        return aabbs;

    }

    public List<AxisAlignedBB> getCatwalkBoundsOnPlace(boolean isUpper, boolean base, boolean south, boolean north,
        boolean east, boolean west, double railHeightIncrease) {

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
        if (base) aabbs.add(BASE_BOUNDS.copy());

        // Move up all boxes by 14 pixels if catwalk is in the top half state
        if (isUpper) aabbs.forEach(aabb -> aabb.offset(0, 0.8750, 0));

        return aabbs;

    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {

        // Disable the touching railings of any adjacent catwalks if they have the same half property as the placed
        // catwalk

        boolean isUpper = CatwalkBit.isActive(worldIn.getBlockMetadata(x, y, z), CatwalkBit.IS_UPPER);

        if (worldIn.getBlock(x, y, z + 1) == ModBlocks.CATWALK.get()) {
            BlockState southState = BlockPropertyRegistry.getBlockState(worldIn, x, y, z + 1);
            if (southState.getPropertyValue(CatwalkBit.IS_UPPER.toString()) instanceof Boolean b && b == isUpper) {
                southState.setPropertyValue(CatwalkBit.NORTH.toString(), false);
                southState.place(worldIn, x, y, z + 1);
                southState.close();
            }
        }

        if (worldIn.getBlock(x, y, z - 1) == ModBlocks.CATWALK.get()) {
            BlockState northState = BlockPropertyRegistry.getBlockState(worldIn, x, y, z - 1);
            if (northState.getPropertyValue(CatwalkBit.IS_UPPER.toString()) instanceof Boolean b && b == isUpper) {
                northState.setPropertyValue(CatwalkBit.SOUTH.toString(), false);
                northState.place(worldIn, x, y, z - 1);
                northState.close();
            }
        }

        if (worldIn.getBlock(x + 1, y, z) == ModBlocks.CATWALK.get()) {
            BlockState eastState = BlockPropertyRegistry.getBlockState(worldIn, x + 1, y, z);
            if (eastState.getPropertyValue(CatwalkBit.IS_UPPER.toString()) instanceof Boolean b && b == isUpper) {
                eastState.setPropertyValue(CatwalkBit.WEST.toString(), false);
                eastState.place(worldIn, x + 1, y, z);
                eastState.close();
            }
        }

        if (worldIn.getBlock(x - 1, y, z) == ModBlocks.CATWALK.get()) {
            BlockState westState = BlockPropertyRegistry.getBlockState(worldIn, x - 1, y, z);
            if (westState.getPropertyValue(CatwalkBit.IS_UPPER.toString()) instanceof Boolean b && b == isUpper) {
                westState.setPropertyValue(CatwalkBit.EAST.toString(), false);
                westState.place(worldIn, x - 1, y, z);
                westState.close();
            }
        }

    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {

        if (player.getHeldItem() != null && player.getHeldItem()
            .getItem() instanceof ItemBlowtorch) {

            int meta = worldIn.getBlockMetadata(x, y, z);

            double adjustHeight = 0;
            if (CatwalkBit.isActive(meta, CatwalkBit.IS_UPPER)) adjustHeight = 0.8750;

            AxisAlignedBB baseBb = BASE_BOUNDS.copy()
                .offset(0, adjustHeight, 0);
            AxisAlignedBB southBb = SOUTH_BOUNDS.copy()
                .offset(0, adjustHeight, 0);
            AxisAlignedBB northBb = NORTH_BOUNDS.copy()
                .offset(0, adjustHeight, 0);
            AxisAlignedBB eastBb = EAST_BOUNDS.copy()
                .offset(0, adjustHeight, 0);
            AxisAlignedBB westBb = WEST_BOUNDS.copy()
                .offset(0, adjustHeight, 0);

            BlockState state = BlockPropertyRegistry.getBlockState(worldIn, x, y, z);

            if (subX >= southBb.minX && subX <= southBb.maxX
                && subY >= southBb.minY
                && subY <= southBb.maxY
                && subZ >= southBb.minZ
                && subZ <= southBb.maxZ) {
                state.setPropertyValue(CatwalkBit.SOUTH.toString(), !CatwalkBit.isActive(meta, CatwalkBit.SOUTH));
            }

            if (subX >= northBb.minX && subX <= northBb.maxX
                && subY >= northBb.minY
                && subY <= northBb.maxY
                && subZ >= northBb.minZ
                && subZ <= northBb.maxZ) {
                state.setPropertyValue(CatwalkBit.NORTH.toString(), !CatwalkBit.isActive(meta, CatwalkBit.NORTH));
            }

            if (subX >= eastBb.minX && subX <= eastBb.maxX
                && subY >= eastBb.minY
                && subY <= eastBb.maxY
                && subZ >= eastBb.minZ
                && subZ <= eastBb.maxZ) {
                state.setPropertyValue(CatwalkBit.EAST.toString(), !CatwalkBit.isActive(meta, CatwalkBit.EAST));
            }

            if (subX >= westBb.minX && subX <= westBb.maxX
                && subY >= westBb.minY
                && subY <= westBb.maxY
                && subZ >= westBb.minZ
                && subZ <= westBb.maxZ) {
                state.setPropertyValue(CatwalkBit.WEST.toString(), !CatwalkBit.isActive(meta, CatwalkBit.WEST));
            }

            state.place(worldIn, x, y, z);
            state.close();

        }

        return false;
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
    public Item getItemDropped(int meta, Random random, int fortune) {
        return ModBlocks.CATWALK.getItem();
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random random) {
        return 1;
    }

    public static class ItemCatwalk extends ItemBlock {

        BlockCatwalk catwalk;

        public ItemCatwalk(Block block) {
            super(block);
            this.catwalk = (BlockCatwalk) block;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack) {
            // Move entity collision checks to onItemUse()
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
            boolean isUpper = false;
            boolean base = true;
            boolean south = true;
            boolean north = true;
            boolean east = true;
            boolean west = true;

            Block blockLook = world.getBlock(x, y, z);
            ForgeDirection facing = CatwalkUtils.getCardinalDirection(player);

            if (blockLook instanceof BlockCatwalk) {

                // If looking at a catwalk, copy its half and perpendicular railing properties
                int lookMeta = world.getBlockMetadata(x, y, z);
                isUpper = CatwalkBit.isActive(lookMeta, CatwalkBit.IS_UPPER);

                switch (facing) {
                    case SOUTH, NORTH -> {
                        east = CatwalkBit.isActive(lookMeta, CatwalkBit.EAST);
                        west = CatwalkBit.isActive(lookMeta, CatwalkBit.WEST);
                    }
                    case EAST, WEST -> {
                        south = CatwalkBit.isActive(lookMeta, CatwalkBit.SOUTH);
                        north = CatwalkBit.isActive(lookMeta, CatwalkBit.NORTH);
                    }
                }

                // If a catwalk perpendicular to the catwalk the player is looking at has its closest perpendicular
                // railing to where the catwalk will be placed enabled, also enable the railing perpendicular to the
                // player to "continue" the railings

                Block southLook = world.getBlock(x, y, z + 1);
                Block northLook = world.getBlock(x, y, z - 1);
                Block eastLook = world.getBlock(x + 1, y, z);
                Block westLook = world.getBlock(x - 1, y, z);
                int southLookMeta = world.getBlockMetadata(x, y, z + 1);
                int northLookMeta = world.getBlockMetadata(x, y, z - 1);
                int eastLookMeta = world.getBlockMetadata(x + 1, y, z);
                int westLookMeta = world.getBlockMetadata(x - 1, y, z);

                ForgeDirection railingToCheck = player.isSneaking() ? facing.getOpposite() : facing;

                switch (facing) {
                    case SOUTH, NORTH -> {
                        if (eastLook instanceof BlockCatwalk
                            && CatwalkBit.isActive(eastLookMeta, CatwalkBit.IS_UPPER) == isUpper
                            && CatwalkBit.isActive(eastLookMeta, railingToCheck)) east = true;
                        if (westLook instanceof BlockCatwalk
                            && CatwalkBit.isActive(westLookMeta, CatwalkBit.IS_UPPER) == isUpper
                            && CatwalkBit.isActive(westLookMeta, railingToCheck)) west = true;
                    }
                    case EAST, WEST -> {
                        if (southLook instanceof BlockCatwalk
                            && CatwalkBit.isActive(southLookMeta, CatwalkBit.IS_UPPER) == isUpper
                            && CatwalkBit.isActive(southLookMeta, railingToCheck)) south = true;
                        if (northLook instanceof BlockCatwalk
                            && CatwalkBit.isActive(northLookMeta, CatwalkBit.IS_UPPER) == isUpper
                            && CatwalkBit.isActive(northLookMeta, railingToCheck)) north = true;
                    }
                }

            } else {

                // If not looking at a catwalk, set the half property based on the hitY value
                // and the side that the player is looking at; do not place railings along the axis that
                // the player is facing

                isUpper = hitY >= 0.5 && side != 0 && side != 1;

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

            // Finally, always disable railings that are adjacent to catwalks with the same isUpper property
            if (world.getBlock(placeX, placeY, placeZ + 1) instanceof BlockCatwalk
                && CatwalkBit.isActive(world.getBlockMetadata(placeX, placeY, placeZ + 1), CatwalkBit.IS_UPPER)
                    == isUpper)
                south = false;
            if (world.getBlock(placeX, placeY, placeZ - 1) instanceof BlockCatwalk
                && CatwalkBit.isActive(world.getBlockMetadata(placeX, placeY, placeZ - 1), CatwalkBit.IS_UPPER)
                    == isUpper)
                north = false;
            if (world.getBlock(placeX + 1, placeY, placeZ) instanceof BlockCatwalk
                && CatwalkBit.isActive(world.getBlockMetadata(placeX + 1, placeY, placeZ), CatwalkBit.IS_UPPER)
                    == isUpper)
                east = false;
            if (world.getBlock(placeX - 1, placeY, placeZ) instanceof BlockCatwalk
                && CatwalkBit.isActive(world.getBlockMetadata(placeX - 1, placeY, placeZ), CatwalkBit.IS_UPPER)
                    == isUpper)
                west = false;

            // Determine if the catwalk will collide with anything before placing
            List<AxisAlignedBB> aabbs = catwalk.getCatwalkBoundsOnPlace(isUpper, base, south, north, east, west, 0.5);
            for (AxisAlignedBB aabb : aabbs) {
                aabb.offset(placeX, placeY, placeZ);
                if (!world.checkNoEntityCollision(aabb)) return false;
            }

            int meta = 0;
            meta = CatwalkBit.update(meta, CatwalkBit.IS_UPPER, isUpper);
            meta = CatwalkBit.update(meta, CatwalkBit.BASE, base);
            meta = CatwalkBit.update(meta, CatwalkBit.SOUTH, south);
            meta = CatwalkBit.update(meta, CatwalkBit.NORTH, north);
            meta = CatwalkBit.update(meta, CatwalkBit.EAST, east);
            meta = CatwalkBit.update(meta, CatwalkBit.WEST, west);

            if (super.placeBlockAt(stack, player, world, placeX, placeY, placeZ, side, hitX, hitY, hitZ, meta)) {
                world.playSoundEffect(
                    placeX + 0.5,
                    placeY + 0.5,
                    placeZ + 0.5,
                    this.catwalk.stepSound.func_150496_b(),
                    (this.catwalk.stepSound.getVolume() + 1.0F) / 2.0F,
                    this.catwalk.stepSound.getPitch() * 0.8F);
                --stack.stackSize;
            }
            return true;
        }

    }

}
