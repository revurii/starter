package com.revurii.bettercatwalks.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
        AxisAlignedBB.getBoundingBox(0, 0.0001, 0, 1, 14 * PX, 2 * PX),
        AxisAlignedBB.getBoundingBox(0, 0.0001, 14 * PX, 1, 14 * PX, 16 * PX),
        AxisAlignedBB.getBoundingBox(0.0001, 0, 0.0001, 0.9999, 2 * PX, 0.9999) };

    public BlockCatwalk(String unlocalizedName) {
        super(Material.iron);
        this.setBlockName(unlocalizedName);
        this.setCreativeTab(CreativeTabs.tabBlock);
        this.setStepSound(soundTypeMetal);
    }

    // @Override
    // public TileEntityCatwalk createNewTileEntity(World worldIn, int meta) {
    // System.out.println("creating tile entity " + meta);
    // return new TileEntityCatwalk();
    // }

    // @Override
    // public boolean hasTileEntity(int metadata) {
    // return true;
    // }

    @Override
    public int onBlockPlaced(World worldIn, int x, int y, int z, int side, float subX, float subY, float subZ,
        int meta) {
        System.out.println("onBlockPlaced()");
        return super.onBlockPlaced(worldIn, x, y, z, side, subX, subY, subZ, meta);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        return super.onBlockActivated(worldIn, x, y, z, player, side, subX, subY, subZ);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
    }

    @Override
    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask,
        List<AxisAlignedBB> list, Entity collider) {
        // Railing 1
        this.setBlockBounds(0, 0, 0, 1, 14 * PX, 2 * PX);
        super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        // Railing 2
        this.setBlockBounds(0, 0, 14 * PX, 1, 14 * PX, 16 * PX);
        super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
        // Base
        this.setBlockBounds(0, 0, 0, 1, 2 * PX, 1);
        super.addCollisionBoxesToList(worldIn, x, y, z, mask, list, collider);
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
        AxisAlignedBB bb = getSelectedBoundingBoxFromPool(worldIn, x, y, z);

        if (bb.minX == 0 && bb.minY == 0 & bb.minZ == 0 && bb.maxX == 0 && bb.maxY == 0 && bb.maxZ == 0) {
            // AABB is at 0 min and max at each axis when the player is not looking at any of the listed AABBs
            setBlockBounds(0, 0, 0, 0, 0, 0);
            return null;
        } else {
            // Offset by the negated coordinates of the block to "normalize" the bounding box to coordinates relative to
            // the position of the block
            bb.offset(-x, -y, -z);
            setBlockBounds(
                (float) bb.minX,
                (float) bb.minY,
                (float) bb.minZ,
                (float) bb.maxX,
                (float) bb.maxY,
                (float) bb.maxZ);
            return super.collisionRayTrace(worldIn, x, y, z, startVec, endVec);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        // Position of the player's eyes in the world
        Vec3 start = player.getPosition(0);

        // Position of a point relative to the player's eyes used to determine their line of sight (-1 to 1 on each
        // axis)
        Vec3 look = player.getLookVec();

        float dist = Minecraft.getMinecraft().playerController.getBlockReachDistance();

        // By adding the look vector multiplied by the player's reach to the player's eye level, we get the farthest
        // point that the player can interact with
        Vec3 end = Vec3.createVectorHelper(
            start.xCoord + look.xCoord * dist,
            start.yCoord + look.yCoord * dist,
            start.zCoord + look.zCoord * dist);

        AxisAlignedBB pooled = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

        // For each defined bounds, set it to the block's position then check if the box is in the player's line of
        // sight
        HashMap<AxisAlignedBB, Double> bbDistanceMap = new HashMap<>();
        for (AxisAlignedBB bb : CATWALK_BOUNDS) {
            pooled.setBB(bb);
            pooled.offset(x, y, z);

            // Get the first point between start and end vector that is within the bounds of the box
            MovingObjectPosition hit = pooled.calculateIntercept(start, end);
            if (hit != null) {
                bbDistanceMap.put(pooled.copy(), hit.hitVec.distanceTo(start));
            }
        }

        // Return the closest box
        if (!bbDistanceMap.isEmpty()) {
            return bbDistanceMap.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .get()
                .getKey();
        }

        return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
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

        @Override
        public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {
            System.out.println("onItemUseFirst()");
            return false;
            // return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
        }

        @Override
        public boolean func_150936_a(World world, int x, int y, int z, int side, EntityPlayer player, ItemStack stack) {
            System.out.println("func_150936_a()");
            return true;
            // return super.func_150936_a(p_150936_1_, p_150936_2_, p_150936_3_, p_150936_4_, p_150936_5_, p_150936_6_,
            // p_150936_7_);
        }

        @Override
        public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {
            System.out.println("onItemUse()");

            if (world.getBlock(x, y, z) != this.catwalk || player.isSneaking()) {
                // Use vanilla permission checks if the player is not looking at a catwalk or sneaking
                return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
            }

            // Otherwise...

            // Translate yaw to the value seen in F3
            float yaw = player.rotationYaw;
            yaw %= 360;
            if (yaw > 180) {
                yaw -= 360;
            } else if (yaw < -180) {
                yaw += 360;
            }

            int placeX = x;
            int placeY = y;
            int placeZ = z;

            // Get the coordinates for where the new catwalk will be placed based on the cardinal direction that the
            // player is facing
            if (yaw >= -45 && yaw < 45) {
                placeZ++; // SOUTH
            } else if (yaw >= 45 && yaw < 135) {
                placeX--; // WEST
            } else if (yaw >= 135 && yaw <= 180 || yaw >= -180 && yaw < -135) {
                placeZ--; // NORTH
            } else if (yaw >= -135 && yaw < -45) {
                placeX++; // EAST
            }

            // Determine if the catwalk can be placed based on some conditions
            if (stack.stackSize == 0) {
                return false;
            } else if (!player.canPlayerEdit(x, y, z, side, stack)) {
                return false;
            } else if (y == 255 && this.catwalk.getMaterial()
                .isSolid()) {
                    return false;
                } else
                if (world.canPlaceEntityOnSide(this.catwalk, placeX, placeY, placeZ, false, side, player, stack)) {
                    System.out.println("SPECIAL PLACE");
                    if (super.placeBlockAt(stack, player, world, placeX, placeY, placeZ, side, hitX, hitY, hitZ, 0)) {
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
                } else {
                    return false;
                }
        }

    }
}
