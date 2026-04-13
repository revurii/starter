package com.revurii.bettercatwalks.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import com.revurii.bettercatwalks.BetterCatwalks;

public class ItemBlowtorch extends Item {

    public ItemBlowtorch(String unlocalizedName) {
        this.setUnlocalizedName(unlocalizedName);
        this.setCreativeTab(CreativeTabs.tabMisc);
        this.setTextureName(BetterCatwalks.MODID + ":" + unlocalizedName);
    }

    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
        return true;
    }
}
