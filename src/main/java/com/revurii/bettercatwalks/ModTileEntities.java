package com.revurii.bettercatwalks;

import com.gtnewhorizon.gtnhlib.blockstate.registry.BlockPropertyRegistry;
import com.revurii.bettercatwalks.properties.FaceCatwalkProperty;
import com.revurii.bettercatwalks.properties.HalfCatwalkProperty;
import com.revurii.bettercatwalks.tileentities.TileEntityCatwalk;
import com.revurii.bettercatwalks.utils.CatwalkConstants;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModTileEntities {

    public static void init() {
        GameRegistry.registerTileEntity(TileEntityCatwalk.class, "TileEntityCatwalk");
        BlockPropertyRegistry
            .registerProperty(ModBlocks.CATWALK.get(), new FaceCatwalkProperty(CatwalkConstants.PROPERTY_SOUTH));
        BlockPropertyRegistry
            .registerProperty(ModBlocks.CATWALK.get(), new FaceCatwalkProperty(CatwalkConstants.PROPERTY_NORTH));
        BlockPropertyRegistry
            .registerProperty(ModBlocks.CATWALK.get(), new FaceCatwalkProperty(CatwalkConstants.PROPERTY_EAST));
        BlockPropertyRegistry
            .registerProperty(ModBlocks.CATWALK.get(), new FaceCatwalkProperty(CatwalkConstants.PROPERTY_WEST));
        BlockPropertyRegistry.registerProperty(
            ModBlocks.CATWALK.getItemBlock(),
            new FaceCatwalkProperty(CatwalkConstants.PROPERTY_NORTH));
        BlockPropertyRegistry.registerProperty(
            ModBlocks.CATWALK.getItemBlock(),
            new FaceCatwalkProperty(CatwalkConstants.PROPERTY_SOUTH));
        BlockPropertyRegistry.registerProperty(ModBlocks.CATWALK.get(), new HalfCatwalkProperty());
        BlockPropertyRegistry.registerProperty(ModBlocks.CATWALK.getItemBlock(), new HalfCatwalkProperty());
    }
}
