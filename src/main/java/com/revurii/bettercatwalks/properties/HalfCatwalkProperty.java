package com.revurii.bettercatwalks.properties;

import java.lang.reflect.Type;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.blockstate.core.BlockProperty;
import com.gtnewhorizon.gtnhlib.blockstate.core.BlockPropertyTrait;
import com.revurii.bettercatwalks.tileentities.TileEntityCatwalk;
import com.revurii.bettercatwalks.utils.CatwalkConstants;

public class HalfCatwalkProperty implements BlockProperty<String> {

    @Override
    public String getName() {
        return CatwalkConstants.PROPERTY_HALF;
    }

    @Override
    public Type getType() {
        return String.class;
    }

    @Override
    public boolean hasTrait(BlockPropertyTrait trait) {
        return switch (trait) {
            case SupportsWorld, WorldMutable, StackMutable, SupportsStacks -> true;
            default -> false;
        };
    }

    @Override
    public String getValue(IBlockAccess world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityCatwalk teCatwalk) {
            return teCatwalk.getHalf();
        }
        return CatwalkConstants.PROPERTY_HALF_BOTTOM;
    }

    @Override
    public void setValue(World world, int x, int y, int z, String half) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityCatwalk teCatwalk) {
            teCatwalk.updateHalf(half);
        }
    }

    @Override
    public String getValue(ItemStack stack) {
        // Always show the bottom-half version in item form
        return CatwalkConstants.PROPERTY_HALF_BOTTOM;
    }
}
