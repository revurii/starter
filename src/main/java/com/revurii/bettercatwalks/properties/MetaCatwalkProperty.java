package com.revurii.bettercatwalks.properties;

import java.lang.reflect.Type;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.blockstate.core.BlockProperty;
import com.gtnewhorizon.gtnhlib.blockstate.core.BlockPropertyTrait;
import com.revurii.bettercatwalks.blocks.BlockCatwalk;
import com.revurii.bettercatwalks.utils.CatwalkBit;

public class MetaCatwalkProperty implements BlockProperty<Boolean> {

    private final CatwalkBit bit;

    public MetaCatwalkProperty(CatwalkBit bit) {
        this.bit = bit;
    }

    @Override
    public String getName() {
        return this.bit.toString();
    }

    @Override
    public Type getType() {
        return Boolean.class;
    }

    @Override
    public boolean hasTrait(BlockPropertyTrait trait) {
        return switch (trait) {
            case SupportsWorld, WorldMutable, StackMutable, SupportsStacks -> true;
            default -> false;
        };
    }

    @Override
    public Boolean getValue(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        return block instanceof BlockCatwalk && CatwalkBit.isActive(meta, this.bit);
    }

    @Override
    public void setValue(World world, int x, int y, int z, Boolean b) {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        if (block instanceof BlockCatwalk) {
            meta = CatwalkBit.update(meta, this.bit, b);
            world.setBlockMetadataWithNotify(x, y, z, meta, 2);
        }
    }

    @Override
    public Boolean getValue(ItemStack stack) {
        // Always enable base, south, and north railings in item form
        return switch (this.bit) {
            case BASE, SOUTH, NORTH -> true;
            default -> false;
        };
    }

}
