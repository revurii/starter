package com.revurii.bettercatwalks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import com.revurii.bettercatwalks.blocks.Block1;
import com.revurii.bettercatwalks.blocks.BlockCatwalk;
import com.revurii.bettercatwalks.blocks.MetaBlock;
import com.revurii.bettercatwalks.items.ItemBlockMetaBlock;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static Block block1;
    public static Block metablock;
    public static Block catwalk;

    public static void init() {
        GameRegistry.registerBlock(block1 = new Block1("block1", Material.iron, ModItems.item1, 0, 1, 3), "block1");
        GameRegistry.registerBlock(
            metablock = new MetaBlock("metablock", Material.iron),
            ItemBlockMetaBlock.class,
            "metablock");
        GameRegistry.registerBlock(catwalk = new BlockCatwalk("catwalk"), "catwalk");
    }

}
