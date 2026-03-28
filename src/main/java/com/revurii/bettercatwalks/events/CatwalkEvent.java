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

public class CatwalkEvent {

    public static final int SIDE_BOTTOM = 0;
    public static final int SIDE_TOP = 1;
    public static final int SIDE_EAST = 2;
    public static final int SIDE_WEST = 3;
    public static final int SIDE_NORTH = 4;
    public static final int SIDE_SOUTH = 5;
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

    // @SubscribeEvent
    public void onCatwalkLook(DrawBlockHighlightEvent e) {
        World world = e.player.worldObj;
        MovingObjectPosition target = e.target;

        EntityPlayer player = e.player;
        double camX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) e.partialTicks;
        double camY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) e.partialTicks;
        double camZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) e.partialTicks;
        Block block = world.getBlock(target.blockX, target.blockY, target.blockZ);
        Vec3 lookVec = e.player.getLookVec();

        // System.out.println(lookVec.xCoord + ", " + lookVec.yCoord + ", " + lookVec.zCoord);
        // System.out.println(target.hitVec.xCoord + ", " + target.hitVec.yCoord + ", " + target.hitVec.zCoord);

        if (block == ModBlocks.CATWALK.get()) {
            AxisAlignedBB invisbb = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
            AxisAlignedBB bb = block.getSelectedBoundingBoxFromPool(world, target.blockX, target.blockY, target.blockZ);
            if (bb.minX == 0 && bb.minY == 0 & bb.minZ == 0 && bb.maxX == 0 && bb.maxY == 0 && bb.maxZ == 0) {
                block.setBlockBounds(0, 0, 0, 0, 0, 0);
            }
            // e.setCanceled(true);

            // ModBlocks.CATWALK.get().setBlockBounds(0, 0, 0, 1, 0.1250f, 1);
            // block.getCollisionBoundingBoxFromPool()
            // block.setBlockBoundsBasedOnState(world, target.blockX, target.blockY, target.blockZ);
            // AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(world, target.blockX, target.blockY,
            // target.blockZ);
            // AxisAlignedBB box = block.getSelectedBoundingBoxFromPool(world, target.blockX, target.blockY,
            // target.blockZ);
            // box = box.expand(1, 1, 1);
            // box.contract(1, 1, 1);
            // box = box.getOffsetBoundingBox(-camX, -camY, -camZ);
            //// box.
            // RenderGlobal.drawOutlinedBoundingBox(box, -1);
            // System.out.println(box.minX + "->" + box.maxX);
            // AxisAlignedBB box2 = new AxisAlignedBB();
            // System.out.println(box.minX + ", " + box.minY + ", " + box.minZ);

            // drawBoxOutline(box.minX, box.minY+0.1250, box.minZ, box.maxX, box.maxY-0.1250, box.minZ+0.1250);
            // drawBoxOutline(box.minX, box.minY+0.1250, box.maxZ-0.1250, box.maxX, box.maxY-0.1250, box.maxZ);
            // drawBoxOutline(box.minX, box.minY, box.minZ, box.maxX, box.minY+0.1250, box.maxZ);

            // double x = box.minX;
            // double y = box.minY;
            // double z = box.minZ;
            //
            // Tessellator tess = Tessellator.instance;
            //
            // tess.startDrawing(GL11.GL_LINE_STRIP);
            // tess.setColorRGBA(0, 0, 0, 127);
            // tess.addVertex(x, y, z);
            // tess.addVertex(x, y+0.8750, z);
            // tess.addVertex(x+0.1250, y+0.8750, z);
            // tess.addVertex(x+0.1250, y+0.1250, z);
            // tess.addVertex(x+0.8750, y+0.1250, z);
            // tess.addVertex(x+0.8750, y+0.8750, z);
            // tess.addVertex(x+1.0000, y+0.8750, z);
            // tess.addVertex(x+1.0000, y, z);
            // tess.addVertex(x, y, z);
            // tess.draw();

        } else {
            ModBlocks.CATWALK.get()
                .setBlockBounds(0, 0, 0, 1, 1, 1);
        }

        // RenderGlobal.drawOutlinedBoundingBox();

        // switch (target.sideHit) {
        // case SIDE_EAST:
        // System.out.println("You are looking at the EAST side" + target.sideHit);
        // setOrRetainBounds(block, 0, 0, 1, 1, 0.8750f, 0.8750f);
        // break;
        // case SIDE_WEST:
        // System.out.println("You are looking at the WEST side" + target.sideHit);
        // setOrRetainBounds(block, 0, 0, 0.1250f, 1, 0.8750f, 0);
        // break;
        // case SIDE_NORTH:
        // System.out.println("You are looking at the NORTH side" + target.sideHit);
        // setOrRetainBounds(block, 0, 0, 0, 0.1250f, 0.8750f, 1);
        // break;
        // case SIDE_SOUTH:
        // System.out.println("You are looking at the SOUTH side" + target.sideHit);
        // setOrRetainBounds(block, 0.8750f, 0, 0, 1, 0.8750f, 1);
        // break;
        // // case SIDE_TOP:
        // case SIDE_BOTTOM:
        // setOrRetainBounds(block, 0, 0, 0, 1, 0.1250f, 1);
        // break;
        //
        // }

    }

    private void drawBoxOutline(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {

        int red = 0;
        int green = 0;
        int blue = 0;
        int alpha = 63;

        Tessellator tess = Tessellator.instance;

        tess.startDrawing(GL11.GL_LINE_STRIP);
        tess.setColorRGBA(red, green, blue, alpha);
        tess.addVertex(minX, minY, minZ);
        tess.addVertex(maxX, minY, minZ);
        tess.addVertex(maxX, minY, maxZ);
        tess.addVertex(minX, minY, maxZ);
        tess.addVertex(minX, minY, minZ);
        tess.draw();

        tess.startDrawing(GL11.GL_LINE_STRIP);
        tess.setColorRGBA(red, green, blue, alpha);
        tess.addVertex(minX, maxY, minZ);
        tess.addVertex(maxX, maxY, minZ);
        tess.addVertex(maxX, maxY, maxZ);
        tess.addVertex(minX, maxY, maxZ);
        tess.addVertex(minX, maxY, minZ);
        tess.draw();

        tess.startDrawing(GL11.GL_LINES);
        tess.setColorRGBA(red, green, blue, alpha);
        tess.addVertex(minX, minY, minZ);
        tess.addVertex(minX, maxY, minZ);
        tess.addVertex(maxX, minY, minZ);
        tess.addVertex(maxX, maxY, minZ);
        tess.addVertex(maxX, minY, maxZ);
        tess.addVertex(maxX, maxY, maxZ);
        tess.addVertex(minX, minY, maxZ);
        tess.addVertex(minX, maxY, maxZ);
        tess.draw();

    }
}
