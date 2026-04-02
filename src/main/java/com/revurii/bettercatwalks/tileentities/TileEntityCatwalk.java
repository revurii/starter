package com.revurii.bettercatwalks.tileentities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import com.revurii.bettercatwalks.utils.CatwalkConstants;

public class TileEntityCatwalk extends TileEntity {

    private boolean south = true;
    private boolean north = true;
    private boolean east = false;
    private boolean west = false;
    private String half = CatwalkConstants.PROPERTY_HALF_BOTTOM;

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean(CatwalkConstants.PROPERTY_SOUTH, this.south);
        compound.setBoolean(CatwalkConstants.PROPERTY_NORTH, this.north);
        compound.setBoolean(CatwalkConstants.PROPERTY_EAST, this.east);
        compound.setBoolean(CatwalkConstants.PROPERTY_WEST, this.west);
        compound.setString(CatwalkConstants.PROPERTY_HALF, this.half);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.south = compound.getBoolean(CatwalkConstants.PROPERTY_SOUTH);
        this.north = compound.getBoolean(CatwalkConstants.PROPERTY_NORTH);
        this.east = compound.getBoolean(CatwalkConstants.PROPERTY_EAST);
        this.west = compound.getBoolean(CatwalkConstants.PROPERTY_WEST);
        this.half = compound.getString(CatwalkConstants.PROPERTY_HALF);

        // TODO: Confirm if this is a good spot to put this to instantly redraw the model after updating it
        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }

    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }

    @Override
    public void validate() {
        super.validate();

        if (this.half == null || this.half.isEmpty()) {
            this.half = CatwalkConstants.PROPERTY_HALF_BOTTOM;
        }

        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public boolean getFaceActive(String face) {
        return switch (face) {
            case CatwalkConstants.PROPERTY_SOUTH -> this.south;
            case CatwalkConstants.PROPERTY_NORTH -> this.north;
            case CatwalkConstants.PROPERTY_EAST -> this.east;
            case CatwalkConstants.PROPERTY_WEST -> this.west;
            default -> false;
        };
    }

    public void updateFace(String face, boolean flag) {

        switch (face) {
            case CatwalkConstants.PROPERTY_SOUTH -> this.south = flag;
            case CatwalkConstants.PROPERTY_NORTH -> this.north = flag;
            case CatwalkConstants.PROPERTY_EAST -> this.east = flag;
            case CatwalkConstants.PROPERTY_WEST -> this.west = flag;
        }

        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

    }

    public String getHalf() {
        if (this.half != null) return this.half;
        return CatwalkConstants.PROPERTY_HALF_BOTTOM;
    }

    public void updateHalf(String half) {
        switch (half) {
            case CatwalkConstants.PROPERTY_HALF_BOTTOM, CatwalkConstants.PROPERTY_HALF_TOP -> this.half = half;
            default -> this.half = CatwalkConstants.PROPERTY_HALF_BOTTOM;
        }

        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

}
