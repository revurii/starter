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

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void updateEntity() {
        if (xCoord == -73 && yCoord == 56 && zCoord == 44) {
            System.out.println("updateEntity(): " + xCoord + ", " + yCoord + ", " + zCoord);
        }
        super.updateEntity();
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean(CatwalkConstants.PROPERTY_SOUTH, this.south);
        compound.setBoolean(CatwalkConstants.PROPERTY_NORTH, this.north);
        compound.setBoolean(CatwalkConstants.PROPERTY_EAST, this.east);
        compound.setBoolean(CatwalkConstants.PROPERTY_WEST, this.west);

        if (xCoord == -73 && yCoord == 56 && zCoord == 44) {
            System.out.println("writeToNBT(): " + xCoord + ", " + yCoord + ", " + zCoord);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.south = compound.getBoolean(CatwalkConstants.PROPERTY_SOUTH);
        this.north = compound.getBoolean(CatwalkConstants.PROPERTY_NORTH);
        this.east = compound.getBoolean(CatwalkConstants.PROPERTY_EAST);
        this.west = compound.getBoolean(CatwalkConstants.PROPERTY_WEST);

        if (xCoord == -73 && yCoord == 56 && zCoord == 44) {
            System.out.println("readFromNBT(): " + xCoord + ", " + yCoord + ", " + zCoord);
        }

        markDirty();
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }

    }

    @Override
    public Packet getDescriptionPacket() {
        if (xCoord == -73 && yCoord == 56 && zCoord == 44) {
            System.out.println("getDescriptionPacket(): " + xCoord + ", " + yCoord + ", " + zCoord);
        }
        NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        if (xCoord == -73 && yCoord == 56 && zCoord == 44) {
            System.out.println("onDataPacket(): " + xCoord + ", " + yCoord + ", " + zCoord);
        }
        readFromNBT(pkt.func_148857_g());
    }

    @Override
    public void validate() {
        if (xCoord == -73 && yCoord == 56 && zCoord == 44) {
            System.out.println("validate(): " + xCoord + ", " + yCoord + ", " + zCoord);
        }
        super.validate();
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public boolean isFaceActive(String face) {
        return switch (face) {
            case CatwalkConstants.PROPERTY_SOUTH -> south;
            case CatwalkConstants.PROPERTY_NORTH -> north;
            case CatwalkConstants.PROPERTY_EAST -> east;
            case CatwalkConstants.PROPERTY_WEST -> west;
            default -> false;
        };
    }

    public void updateFace(String face, boolean flag) {

        if (xCoord == -73 && yCoord == 56 && zCoord == 44) {
            System.out.println("updateFace(): " + xCoord + ", " + yCoord + ", " + zCoord);
        }

        switch (face) {
            case CatwalkConstants.PROPERTY_SOUTH -> south = flag;
            case CatwalkConstants.PROPERTY_NORTH -> north = flag;
            case CatwalkConstants.PROPERTY_EAST -> east = flag;
            case CatwalkConstants.PROPERTY_WEST -> west = flag;
        }

        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

    }

}
