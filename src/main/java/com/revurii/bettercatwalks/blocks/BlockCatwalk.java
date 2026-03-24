package com.revurii.bettercatwalks.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.gtnewhorizon.gtnhlib.client.model.ModelISBRH;
import com.revurii.bettercatwalks.BetterCatwalks;

public class BlockCatwalk extends Block {

    public BlockCatwalk(String unlocalizedName) {
        super(Material.iron);
        this.setBlockName(unlocalizedName);
        this.setCreativeTab(CreativeTabs.tabBlock);
        this.setStepSound(soundTypeMetal);
        this.setBlockTextureName(BetterCatwalks.MODID + ":" + unlocalizedName);
        // this.setBlockBounds(0, 0, 0, 1, 0.1250f, 1);
    }

    // @Override
    // public TileEntityCatwalk createNewTileEntity(World worldIn, int meta) {
    // System.out.println("creating tile entity " + meta);
    // return new TileEntityCatwalk();
    // }

    // @Override
    // public boolean hasTileEntity(int metadata) {
    // return true;
    // }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
        // worldIn.removeTileEntity(x, y, z);
        // System.out.println("removed tile entity " + meta);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return ModelISBRH.JSON_ISBRH_ID;
    }

    // private static final IIcon[] icons = new IIcon[1];

    // @Override
    // public void registerBlockIcons(IIconRegister reg) {
    // icons[0] = reg.registerIcon("bettercatwalks:tileEntity1");
    // super.registerBlockIcons(reg);
    // }
    //
    // @Override
    // public IIcon getIcon(int side, int meta) {
    // return icons[0];
    // }
}
