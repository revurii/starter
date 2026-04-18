package com.revurii.bettercatwalks.blocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
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

    public BlockCatwalk(String unlocalizedName) {
        super(Material.iron);
        this.setBlockName(unlocalizedName);
        this.setStepSound(soundTypeMetal);
        this.setHardness(1.5F);
        this.setHarvestLevel("pickaxe", 0);
    }

    @Override
    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask,
        List<AxisAlignedBB> list, Entity collider) {

        // mask = bounding box of the entity colliding with the block (northwest bottom corner to southeast top corner)
        // list = add bounding boxes that will be active here

        int meta = worldIn.getBlockMetadata(x, y, z);

        for (AxisAlignedBB bb : getCatwalkBoundsBasedOnState(meta, 0.5, false).values()) {
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

        int meta = worldIn.getBlockMetadata(x, y, z);
        Collection<AxisAlignedBB> aabbs = getCatwalkBoundsBasedOnState(meta, 0, true).values();

        // Get the closest MOP hit
        MovingObjectPosition mop = null;

        for (AxisAlignedBB aabb : aabbs) {
            MovingObjectPosition hit = aabb.copy()
                .offset(x, y, z)
                .calculateIntercept(startVec, endVec);
            if (hit == null) continue;
            if (mop == null || hit.hitVec.distanceTo(startVec) < mop.hitVec.distanceTo(startVec)) mop = hit;
        }

        if (mop != null) {
            mop.blockX = x;
            mop.blockY = y;
            mop.blockZ = z;
        }

        return mop;

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

        int meta = worldIn.getBlockMetadata(x, y, z);
        Collection<AxisAlignedBB> aabbs = getCatwalkBoundsBasedOnState(meta, 0, true).values();

        AxisAlignedBB closestBb = null;
        MovingObjectPosition closestMop = null;

        for (AxisAlignedBB aabb : aabbs) {

            aabb.offset(x, y, z);
            MovingObjectPosition hit = aabb.calculateIntercept(start, end);

            if (hit != null) {

                if (closestBb == null || hit.hitVec.distanceTo(start) < closestMop.hitVec.distanceTo(start)) {
                    closestBb = aabb;
                    closestMop = hit;
                    continue;
                }

                // If two bounding boxes were hit,
                // use the box that has the closest face opposite the direction that the player is facing

                if (hit.hitVec.distanceTo(start) == closestMop.hitVec.distanceTo(start)) {

                    Vec3 hitVec = hit.hitVec;
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
                        closestBb = aabb;
                        closestMop = hit;
                    }

                }

            }
        }

        if (closestBb != null) return closestBb;
        return AxisAlignedBB.getBoundingBox(x, y, z, x, y, z);

    }

    /**
     * Retrieve a list of AABBs based on which half is active and which faces are active on the catwalk.
     * These AABBs are not yet positioned to the catwalk.
     *
     * @param railHeightIncrease will increase the height of the railings by the set amount, for use with collisions
     * @param forSelection       will force the base bounding box to appear, for use with selection
     */
    public HashMap<CatwalkBit, AxisAlignedBB> getCatwalkBoundsBasedOnState(int meta, double railHeightIncrease,
        boolean forSelection) {

        // List<AxisAlignedBB> aabbs = new ArrayList<>();
        HashMap<CatwalkBit, AxisAlignedBB> bounds = new HashMap<>();

        // Determine base bounds
        AxisAlignedBB base = CatwalkBit.BASE.getBounds(true);
        if (forSelection && !CatwalkBit.isActive(meta, CatwalkBit.BASE)) base = CatwalkBit.BASE.getBounds(false);
        if (forSelection || CatwalkBit.isActive(meta, CatwalkBit.BASE)) bounds.put(CatwalkBit.BASE, base);

        // Determine railing bounds
        for (CatwalkBit bit : CatwalkBit.getAllBounds(true)) {

            AxisAlignedBB aabb = bit.getBounds(true)
                .addCoord(0, railHeightIncrease, 0);

            // If not forSelection or base is active, add the railing bounds if it is active
            if (!forSelection || CatwalkBit.isActive(meta, CatwalkBit.BASE)) {
                if (CatwalkBit.isActive(meta, bit)) bounds.put(bit, aabb);
                continue;
            }

            // Otherwise (meaning forSelection and base is inactive)...
            if (CatwalkBit.isActive(meta, bit)) {
                // Extend railing downwards if enabled
                aabb.minY = base.minY;
            } else {
                // Use disabled bounds if disabled
                aabb = bit.getBounds(false);
            }

            bounds.put(bit, aabb);

        }

        // Move up all bounds by 14 pixels if catwalk is in the top half state
        if (CatwalkBit.isActive(meta, CatwalkBit.IS_UPPER)) bounds.forEach((bit, aabb) -> aabb.offset(0, 0.8750, 0));

        return bounds;

    }

    public List<AxisAlignedBB> getCatwalkBoundsOnPlace(boolean isUpper, boolean base, boolean south, boolean north,
        boolean east, boolean west, double railHeightIncrease) {

        List<AxisAlignedBB> aabbs = new ArrayList<>();
        if (south) aabbs.add(
            CatwalkBit.SOUTH.getBounds(true)
                .addCoord(0, railHeightIncrease, 0));
        if (north) aabbs.add(
            CatwalkBit.NORTH.getBounds(true)
                .addCoord(0, railHeightIncrease, 0));
        if (east) aabbs.add(
            CatwalkBit.EAST.getBounds(true)
                .addCoord(0, railHeightIncrease, 0));
        if (west) aabbs.add(
            CatwalkBit.WEST.getBounds(true)
                .addCoord(0, railHeightIncrease, 0));
        if (base) aabbs.add(CatwalkBit.BASE.getBounds(true));

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
            BlockState state = BlockPropertyRegistry.getBlockState(worldIn, x, y, z);

            double heightAdjustment = CatwalkBit.isActive(meta, CatwalkBit.IS_UPPER) ? 0.8750 : 0;

            // Determine base bounds
            AxisAlignedBB base = CatwalkBit.BASE.getBounds(true)
                .offset(0, heightAdjustment, 0);
            if (!CatwalkBit.isActive(meta, CatwalkBit.BASE)) {
                base = CatwalkBit.BASE.getBounds(false)
                    .offset(0, heightAdjustment, 0);
            }

            // If the base was right-clicked, toggle parts based on the position of the hit
            if (subX >= base.minX && subX <= base.maxX
                && subY >= base.minY
                && subY <= base.maxY
                && subZ >= base.minZ
                && subZ <= base.maxZ) {

                if (player.isSneaking()) {

                    // Only toggle the base if the player is sneaking
                    state.setPropertyValue(CatwalkBit.BASE.toString(), !CatwalkBit.isActive(meta, CatwalkBit.BASE));

                } else if (CatwalkBit.isActive(meta, CatwalkBit.BASE)) {

                    // Determine which railing to toggle based on the position of the hit relative to the center

                    float xRelativeToCenter = subX - 0.5f;
                    float zRelativeToCenter = subZ - 0.5f;

                    if (MathHelper.abs(xRelativeToCenter) > MathHelper.abs((zRelativeToCenter))) {
                        if (xRelativeToCenter < 0) {
                            state.setPropertyValue(
                                CatwalkBit.WEST.toString(),
                                !CatwalkBit.isActive(meta, CatwalkBit.WEST));
                        } else {
                            state.setPropertyValue(
                                CatwalkBit.EAST.toString(),
                                !CatwalkBit.isActive(meta, CatwalkBit.EAST));
                        }
                    } else {
                        if (zRelativeToCenter < 0) {
                            state.setPropertyValue(
                                CatwalkBit.NORTH.toString(),
                                !CatwalkBit.isActive(meta, CatwalkBit.NORTH));
                        } else {
                            state.setPropertyValue(
                                CatwalkBit.SOUTH.toString(),
                                !CatwalkBit.isActive(meta, CatwalkBit.SOUTH));
                        }
                    }

                }

            } else {

                // Determine which railing to toggle
                // Nearly the same logic as getSelectedBoundingBoxFromPool() except we already have the hit vector

                HashMap<CatwalkBit, AxisAlignedBB> bounds = getCatwalkBoundsBasedOnState(meta, 0, true);

                CatwalkBit bit = null;
                AxisAlignedBB closestBb = null;

                for (Map.Entry<CatwalkBit, AxisAlignedBB> box : bounds.entrySet()) {

                    AxisAlignedBB aabb = box.getValue();

                    if (subX >= aabb.minX && subX <= aabb.maxX
                        && subY >= aabb.minY
                        && subY <= aabb.maxY
                        && subZ >= aabb.minZ
                        && subZ <= aabb.maxZ) {

                        if (closestBb == null) {

                            bit = box.getKey();
                            closestBb = aabb;

                        } else {

                            Vec3 hitVec = Vec3.createVectorHelper(subX, subY, subZ);
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

                            Vec3 closestOpposite = Vec3
                                .createVectorHelper(closestOppositeX, hitVec.yCoord, closestOppositeZ);
                            Vec3 currentOpposite = Vec3
                                .createVectorHelper(currentOppositeX, hitVec.yCoord, currentOppositeZ);

                            if (currentOpposite.distanceTo(hitVec) < closestOpposite.distanceTo(hitVec)) {
                                bit = box.getKey();
                                closestBb = aabb;
                            }

                        }

                    }

                }

                if (bit == null) return false;
                state.setPropertyValue(bit.toString(), !CatwalkBit.isActive(meta, bit));

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

                // If looking at a catwalk, copy its half, base, and perpendicular railing properties
                int lookMeta = world.getBlockMetadata(x, y, z);
                isUpper = CatwalkBit.isActive(lookMeta, CatwalkBit.IS_UPPER);
                base = CatwalkBit.isActive(lookMeta, CatwalkBit.BASE);

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
