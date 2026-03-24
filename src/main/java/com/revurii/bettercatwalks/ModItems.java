package com.revurii.bettercatwalks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModItems {

    public static Item item1;

    public static void init() {
        item1 = new Item().setUnlocalizedName("item1")
            .setCreativeTab(CreativeTabs.tabMisc)
            .setTextureName(BetterCatwalks.MODID + ":item1");
        GameRegistry.registerItem(item1, "item1");
    }
}
