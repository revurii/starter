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

    private String face;

    public FaceCatwalkProperty(String face) {
        this.face = face;
    }

    @Override
    public String getName() {
        return this.face;
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
            return teCatwalk.isFaceActive(this.face);
        }
        return false;
    }

    @Override
    public void setValue(World world, int x, int y, int z, Boolean flag) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityCatwalk teCatwalk) {
            teCatwalk.updateFace(this.face, flag);
        }
    }

    @Override
    public Boolean getValue(ItemStack stack) {
        // Always enable north and south face in item form
        return switch (face) {
            case CatwalkConstants.PROPERTY_SOUTH, CatwalkConstants.PROPERTY_NORTH -> true;
            default -> false;
        };
    }

}
