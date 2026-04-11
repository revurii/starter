package com.revurii.bettercatwalks.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import com.revurii.bettercatwalks.BetterCatwalks;

public class ItemBlowtorch extends Item {

    public ItemBlowtorch(String unlocalizedName) {
        this.setUnlocalizedName(unlocalizedName);
        this.setCreativeTab(CreativeTabs.tabMisc);
        this.setTextureName(BetterCatwalks.MODID + ":" + unlocalizedName);
    }
}
