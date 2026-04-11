package com.revurii.bettercatwalks;

import com.revurii.bettercatwalks.items.ItemBlowtorch;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModItems {

    public static ItemBlowtorch blowtorch;

    public static void init() {
        blowtorch = new ItemBlowtorch("blowtorch");
        GameRegistry.registerItem(blowtorch, blowtorch.getUnlocalizedName());
    }
}
