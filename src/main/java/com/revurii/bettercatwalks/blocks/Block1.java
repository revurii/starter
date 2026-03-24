package com.revurii.bettercatwalks.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

import com.revurii.bettercatwalks.BetterCatwalks;

public class Block1 extends Block {

    public IIcon[] icons = new IIcon[6];

    private Item drop;
    private int meta;
    private int least_quantity;
    private int most_quantity;

    public Block1(String unlocalizedName, Material materialIn, Item drop, int meta, int least_quantity,
        int most_quantity) {
        super(materialIn);

        this.setBlockName(unlocalizedName);
        this.setBlockTextureName(BetterCatwalks.MODID + ":" + unlocalizedName);
        this.setCreativeTab(CreativeTabs.tabBlock);
        this.setHardness(2.0F);
        this.setResistance(6.0F);
        this.setLightLevel(0.0F);
        this.setHarvestLevel("pickaxe", 3);
        this.setStepSound(soundTypeMetal);

        this.drop = drop;
        this.meta = meta;
        this.least_quantity = least_quantity;
        this.most_quantity = most_quantity;
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        for (int i = 0; i < icons.length; i++) {
            this.icons[i] = reg.registerIcon(this.textureName + "_" + i);
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return this.icons[side];
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return this.drop;
    }

    @Override
    public int damageDropped(int meta) {
        return this.meta;
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random random) {
        // if (this.least_quantity >= this.most_quantity) {
        // return this.least_quantity;
        // }
        // return this.least_quantity + random.nextInt(this.most_quantity - this.least_quantity + fortune + 1);
        return 3;
    }
}
