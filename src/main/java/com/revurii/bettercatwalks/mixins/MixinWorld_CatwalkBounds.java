package com.revurii.bettercatwalks.mixins;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.revurii.bettercatwalks.ModBlocks;
import com.revurii.bettercatwalks.tileentities.TileEntityCatwalk;
import com.revurii.bettercatwalks.utils.CatwalkConstants;

@Mixin(EntityLivingBase.class)
public abstract class MixinWorld_CatwalkBounds extends Entity {

    @Shadow
    public abstract Vec3 getPosition(float p_70666_1_);

    @Shadow
    public abstract Vec3 getLook(float p_70676_1_);

    public MixinWorld_CatwalkBounds(World worldIn) {
        super(worldIn);
    }

    /**
     * Perform a raytrace again but shift the vectors down by one block to look for catwalks in the top half state
     * which may have railings that are on the cursor of the player.
     */

    @Inject(
        method = "rayTrace(DF)Lnet/minecraft/util/MovingObjectPosition;",
        at = @At(value = "RETURN"),
        cancellable = true)
    private void betterCatwalks_rayTraceExtraCatwalkBlock(double reach, float partialTickTime,
        CallbackInfoReturnable<MovingObjectPosition> cir) {

        Vec3 realStart = this.getPosition(partialTickTime);
        Vec3 look = this.getLook(partialTickTime);
        Vec3 realEnd = realStart.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);

        Vec3 extraStart = realStart.addVector(0, -1, 0);
        Vec3 extraEnd = realEnd.addVector(0, -1, 0);

        // Call a modified copy of func_147447_a() from the World class to get catwalks in the top half state using the
        // lowered start and end vectors
        MovingObjectPosition newMop = betterCatwalks_func_147447_a(extraStart, extraEnd, realStart, realEnd);
        MovingObjectPosition origMop = cir.getReturnValue();

        if (newMop != null) {
            if (origMop != null) {
                if (newMop.hitVec.distanceTo(realStart) <= origMop.hitVec.distanceTo(realStart)) {
                    cir.setReturnValue(newMop);
                }
            }
        }

    }

    /**
     * This is a copy of func_147447_a() modified to exclude blocks that are not catwalks in the top half state
     * and with some variables renamed for readability.
     * I am not exactly sure of what happens inside the while loop, but it works :P
     */

    @Unique
    private MovingObjectPosition betterCatwalks_func_147447_a(Vec3 start, Vec3 end, Vec3 realStart, Vec3 realEnd) {

        int endX = MathHelper.floor_double(end.xCoord);
        int endY = MathHelper.floor_double(end.yCoord);
        int endZ = MathHelper.floor_double(end.zCoord);
        int startX = MathHelper.floor_double(start.xCoord);
        int startY = MathHelper.floor_double(start.yCoord);
        int startZ = MathHelper.floor_double(start.zCoord);

        Block block = worldObj.getBlock(startX, startY, startZ);
        TileEntity te = worldObj.getTileEntity(startX, startY, startZ);
        // BlockState state = BlockPropertyRegistry.getBlockState(worldObj, startX, startY, startZ);

        if (block == ModBlocks.CATWALK.get() && te instanceof TileEntityCatwalk teCatwalk
            && teCatwalk.getHalf()
                .equals(CatwalkConstants.PROPERTY_HALF_TOP)) {
            MovingObjectPosition movingobjectposition = block
                .collisionRayTrace(worldObj, startX, startY, startZ, realStart, realEnd);
            if (movingobjectposition != null) return movingobjectposition;
        }

        int k1 = 200;
        while (k1-- >= 0) {

            if (startX == endX && startY == endY && startZ == endZ) return null;

            boolean startAndEndXAreNotEqual = true;
            boolean startAndEndYAreNotEqual = true;
            boolean startAndEndZAreNotEqual = true;

            double x1 = 999.0D;
            double y1 = 999.0D;
            double z1 = 999.0D;

            if (endX > startX) {
                x1 = (double) startX + 1.0D;
            } else if (endX < startX) {
                x1 = (double) startX + 0.0D;
            } else {
                startAndEndXAreNotEqual = false;
            }

            if (endY > startY) {
                y1 = (double) startY + 1.0D;
            } else if (endY < startY) {
                y1 = (double) startY + 0.0D;
            } else {
                startAndEndYAreNotEqual = false;
            }

            if (endZ > startZ) {
                z1 = (double) startZ + 1.0D;
            } else if (endZ < startZ) {
                z1 = (double) startZ + 0.0D;
            } else {
                startAndEndZAreNotEqual = false;
            }

            double x2 = 999.0D;
            double y2 = 999.0D;
            double z2 = 999.0D;

            double displacementX = end.xCoord - start.xCoord;
            double displacementY = end.yCoord - start.yCoord;
            double displacementZ = end.zCoord - start.zCoord;

            if (startAndEndXAreNotEqual) x2 = (x1 - start.xCoord) / displacementX;
            if (startAndEndYAreNotEqual) y2 = (y1 - start.yCoord) / displacementY;
            if (startAndEndZAreNotEqual) z2 = (z1 - start.zCoord) / displacementZ;

            byte b0;

            if (x2 < y2 && x2 < z2) {

                if (endX > startX) {
                    b0 = 4;
                } else {
                    b0 = 5;
                }

                start.xCoord = x1;
                start.yCoord += displacementY * x2;
                start.zCoord += displacementZ * x2;

            } else if (y2 < z2) {

                if (endY > startY) {
                    b0 = 0;
                } else {
                    b0 = 1;
                }

                start.xCoord += displacementX * y2;
                start.yCoord = y1;
                start.zCoord += displacementZ * y2;

            } else {

                if (endZ > startZ) {
                    b0 = 2;
                } else {
                    b0 = 3;
                }

                start.xCoord += displacementX * z2;
                start.yCoord += displacementY * z2;
                start.zCoord = z1;

            }

            Vec3 tempVec = Vec3.createVectorHelper(start.xCoord, start.yCoord, start.zCoord);
            startX = (int) (tempVec.xCoord = MathHelper.floor_double(start.xCoord));

            if (b0 == 5) {
                --startX;
                ++tempVec.xCoord;
            }

            startY = (int) (tempVec.yCoord = MathHelper.floor_double(start.yCoord));

            if (b0 == 1) {
                --startY;
                ++tempVec.yCoord;
            }

            startZ = (int) (tempVec.zCoord = MathHelper.floor_double(start.zCoord));

            if (b0 == 3) {
                --startZ;
                ++tempVec.zCoord;
            }

            Block block1 = worldObj.getBlock(startX, startY, startZ);
            TileEntity te1 = worldObj.getTileEntity(startX, startY, startZ);
            // BlockState state1 = BlockPropertyRegistry.getBlockState(worldObj, startX, startY, startZ);

            if (block1 == ModBlocks.CATWALK.get() && te1 instanceof TileEntityCatwalk teCatwalk
                && teCatwalk.getHalf()
                    .equals(CatwalkConstants.PROPERTY_HALF_TOP)) {
                MovingObjectPosition mop = block1
                    .collisionRayTrace(worldObj, startX, startY, startZ, realStart, realEnd);
                if (mop != null) return mop;
            }

        }

        return null;

    }

}
