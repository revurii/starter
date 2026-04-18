package com.revurii.bettercatwalks.events;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

import org.lwjgl.opengl.GL11;

import com.revurii.bettercatwalks.ModBlocks;
import com.revurii.bettercatwalks.blocks.BlockCatwalk;
import com.revurii.bettercatwalks.utils.CatwalkBit;
import com.revurii.bettercatwalks.utils.CatwalkUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CatwalkEvent {

    @SubscribeEvent
    public void onDrawCatwalkHighlight(DrawBlockHighlightEvent e) {

        World world = e.player.worldObj;
        EntityPlayer player = e.player;
        MovingObjectPosition target = e.target;
        Block targetBlock = world.getBlock(target.blockX, target.blockY, target.blockZ);

        // If the player is sneaking, not holding a catwalk, or not looking at a catwalk, do nothing
        if (player.isSneaking() || player.getHeldItem() == null
            || player.getHeldItem()
                .getItem() != ModBlocks.CATWALK.getItem()
            || !(targetBlock instanceof BlockCatwalk)) return;

        float dist = Minecraft.getMinecraft().playerController.getBlockReachDistance();

        Vec3 start = player.getPosition(0);
        Vec3 look = player.getLookVec();
        Vec3 end = Vec3.createVectorHelper(
            start.xCoord + look.xCoord * dist,
            start.yCoord + look.yCoord * dist,
            start.zCoord + look.zCoord * dist);

        MovingObjectPosition mop = targetBlock
            .collisionRayTrace(world, target.blockX, target.blockY, target.blockZ, start, end);
        if (mop == null) return;

        Vec3 placeVec = CatwalkUtils.getCatwalkPlacementPosition(
            player,
            world,
            target.blockX,
            target.blockY,
            target.blockZ,
            target.sideHit,
            (float) mop.hitVec.xCoord - target.blockX,
            (float) mop.hitVec.yCoord - target.blockY,
            (float) mop.hitVec.zCoord - target.blockZ);

        // If there is something in the way of where a catwalk would be placed, do nothing
        int px = MathHelper.floor_double(placeVec.xCoord);
        int py = MathHelper.floor_double(placeVec.yCoord);
        int pz = MathHelper.floor_double(placeVec.zCoord);
        Block blockInPlaceCoordinates = world.getBlock(px, py, pz);
        if (!blockInPlaceCoordinates.isReplaceable(world, px, py, pz)) return;

        // Draw a box around where the catwalk would be placed

        // I have no idea what this does
        double camX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) e.partialTicks;
        double camY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) e.partialTicks;
        double camZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) e.partialTicks;

        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);

        bb.offset(placeVec.xCoord, placeVec.yCoord, placeVec.zCoord);

        int targetMeta = world.getBlockMetadata(target.blockX, target.blockY, target.blockZ);
        if (CatwalkBit.isActive(targetMeta, CatwalkBit.IS_UPPER)) bb.offset(0, 0.8750, 0);

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

    @SubscribeEvent
    public void onPlayerOnTopOfCatwalk(LivingEvent.LivingUpdateEvent e) {
        EntityLivingBase entity = e.entityLiving;
        if (entity instanceof EntityClientPlayerMP player && entity.onGround) {
            Block block = entity.worldObj.getBlock(
                MathHelper.floor_double(entity.posX),
                MathHelper.floor_double(entity.boundingBox.minY - 0.001F),
                MathHelper.floor_double(entity.posZ));
            if (block instanceof BlockCatwalk) {
                if (player.motionX == 0 && player.motionZ == 0) return;
                if (player.isSneaking()) return;
                player.motionX *= 1.4;
                player.motionZ *= 1.4;
            }
        }
    }

}
