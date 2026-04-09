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

public class FaceCatwalkProperty implements BlockProperty<Boolean> {

    private final String FACE;

    public FaceCatwalkProperty(String face) {
        FACE = face;
    }

    @Override
    public String getName() {
        return FACE;
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
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityCatwalk teCatwalk) {
            return teCatwalk.getRailing(FACE);
        }
        return false;
    }

    @Override
    public void setValue(World world, int x, int y, int z, Boolean flag) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityCatwalk teCatwalk) {
            teCatwalk.updateFace(FACE, flag);
        }
    }

    @Override
    public Boolean getValue(ItemStack stack) {
        // Always enable north and south face in item form
        return switch (FACE) {
            case CatwalkConstants.PROPERTY_SOUTH, CatwalkConstants.PROPERTY_NORTH -> true;
            default -> false;
        };
    }

}
