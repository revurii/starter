package com.revurii.bettercatwalks.mixins;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizon.gtnhlib.blockstate.core.BlockState;
import com.gtnewhorizon.gtnhlib.blockstate.registry.BlockPropertyRegistry;
import com.revurii.bettercatwalks.ModBlocks;
import com.revurii.bettercatwalks.blocks.BlockCatwalk;
import com.revurii.bettercatwalks.utils.CatwalkConstants;

@Mixin(Block.class)
public class MixinBlock_AddCollisionBoxesToList {

    /**
     * Check also the block below this; if it is a catwalk in the top half state, add its collision boxes to the list.
     * Without this, an entity standing on the railing of a catwalk in the top half state would glitch out since the
     * collision list is cleared when they are more than one block away from the catwalk (or so I believe).
     */

    @Inject(
        method = "addCollisionBoxesToList(Lnet/minecraft/world/World;IIILnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V",
        at = @At(value = "TAIL"))
    private void betterCatwalks_addExtraCatwalkCollisionBoxesToList(World worldIn, int x, int y, int z,
        AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collider, CallbackInfo ci) {

        Block block = worldIn.getBlock(x, y - 1, z);

        if (block == ModBlocks.CATWALK.get()) {

            BlockState state = BlockPropertyRegistry.getBlockState(worldIn, x, y - 1, z);

            if (state.getPropertyValue(CatwalkConstants.PROPERTY_HALF) instanceof String half
                && half.equals(CatwalkConstants.PROPERTY_HALF_TOP)) {
                BlockCatwalk catwalk = (BlockCatwalk) block;
                for (AxisAlignedBB bb : catwalk.getCatwalkBoundsBasedOnState(worldIn, x, y - 1, z, 0.5)) {
                    AxisAlignedBB copy = bb.copy()
                        .offset(x, y - 1, z);
                    if (mask.intersectsWith(copy)) {
                        list.add(copy);
                    }
                }
            }

        }
    }

}
