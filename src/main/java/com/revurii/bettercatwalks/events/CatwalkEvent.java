package com.revurii.bettercatwalks.events;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import org.lwjgl.opengl.GL11;

import com.revurii.bettercatwalks.ModBlocks;
import com.revurii.bettercatwalks.utils.CatwalkUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CatwalkEvent {

    List<Double> currentBounds = new ArrayList<>();

    // @SubscribeEvent
    public void onInteract(PlayerInteractEvent e) {
        // System.out.println("onInteract()");
    }

    // @SubscribeEvent
    public void onPlace(BlockEvent.PlaceEvent e) {
        World world = e.player.worldObj;
        Block targetBlock = e.placedAgainst;
        if (targetBlock == ModBlocks.CATWALK.get()) {
            return;
        }
    }

    @SubscribeEvent
    public void onDrawCatwalkHighlight(DrawBlockHighlightEvent e) {

        World world = e.player.worldObj;
        EntityPlayer player = e.player;
        MovingObjectPosition target = e.target;

        // If the player is sneaking, not holding a catwalk, or not looking at a catwalk, do nothing
        if (player.isSneaking() || player.getHeldItem() == null
            || player.getHeldItem()
                .getItem() != ModBlocks.CATWALK.getItem()
            || world.getBlock(target.blockX, target.blockY, target.blockZ) != ModBlocks.CATWALK.get()) return;

        Vec3 placeVec = CatwalkUtils
            .getCatwalkPlacementPosition(player, world, target.blockX, target.blockY, target.blockZ, target.sideHit);

        int px = (int) placeVec.xCoord;
        int py = (int) placeVec.yCoord;
        int pz = (int) placeVec.zCoord;

        if (!world.isAirBlock(px, py, pz)) return;

        // I have no idea what this does
        double camX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) e.partialTicks;
        double camY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) e.partialTicks;
        double camZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) e.partialTicks;

        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
        bb.offset(px, py, pz);
        bb = bb.getOffsetBoundingBox(-camX, -camY, -camZ);
        bb = bb.contract(0.001, 0.001, 0.001);

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        Tessellator tess = Tessellator.instance;

        tess.startDrawing(GL11.GL_LINE_STRIP);
        tess.setColorRGBA(0, 0, 0, 63);
        tess.addVertex(bb.minX, bb.minY, bb.minZ);
        tess.addVertex(bb.maxX, bb.minY, bb.minZ);
        tess.addVertex(bb.maxX, bb.minY, bb.maxZ);
        tess.addVertex(bb.minX, bb.minY, bb.maxZ);
        tess.addVertex(bb.minX, bb.minY, bb.minZ);
        tess.draw();

        tess.startDrawing(GL11.GL_LINE_STRIP);
        tess.setColorRGBA(0, 0, 0, 63);
        tess.addVertex(bb.minX, bb.maxY, bb.minZ);
        tess.addVertex(bb.maxX, bb.maxY, bb.minZ);
        tess.addVertex(bb.maxX, bb.maxY, bb.maxZ);
        tess.addVertex(bb.minX, bb.maxY, bb.maxZ);
        tess.addVertex(bb.minX, bb.maxY, bb.minZ);
        tess.draw();

        tess.startDrawing(GL11.GL_LINES);
        tess.setColorRGBA(0, 0, 0, 63);
        tess.addVertex(bb.minX, bb.minY, bb.minZ);
        tess.addVertex(bb.minX, bb.maxY, bb.minZ);
        tess.addVertex(bb.maxX, bb.minY, bb.minZ);
        tess.addVertex(bb.maxX, bb.maxY, bb.minZ);
        tess.addVertex(bb.maxX, bb.minY, bb.maxZ);
        tess.addVertex(bb.maxX, bb.maxY, bb.maxZ);
        tess.addVertex(bb.minX, bb.minY, bb.maxZ);
        tess.addVertex(bb.minX, bb.maxY, bb.maxZ);
        tess.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

    }

}
