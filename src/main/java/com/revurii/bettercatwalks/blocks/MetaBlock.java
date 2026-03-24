package com.revurii.bettercatwalks.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.revurii.bettercatwalks.BetterCatwalks;

public class MetaBlock extends Block {

    private IIcon[] textures = new IIcon[3];

    public MetaBlock(String unlocalizedName, Material materialIn) {
        super(materialIn);
        this.setBlockName(unlocalizedName);
        this.setBlockTextureName(BetterCatwalks.MODID + ":" + unlocalizedName);
        this.setCreativeTab(CreativeTabs.tabBlock);
        this.setHardness(2.0F);
        this.setResistance(6.0F);
        this.setLightLevel(0.0F);
        this.setHarvestLevel("pickaxe", 3);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        for (int i = 0; i < textures.length; i++) {
            this.textures[i] = reg.registerIcon(this.textureName + "_" + i);
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (meta > textures.length) {
            meta = 0;
        }
        return textures[meta];
    }

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (int i = 0; i < textures.length; i++) {
            list.add(new ItemStack(itemIn, 1, i));
        }
    }
}
