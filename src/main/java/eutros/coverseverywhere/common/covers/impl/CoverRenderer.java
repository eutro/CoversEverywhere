package eutros.coverseverywhere.common.covers.impl;

import eutros.coverseverywhere.api.ICover;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class CoverRenderer {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(CoverRenderer.class);
    }

    private static Map<World, Integer> renderCache = new WeakHashMap<>();

    private static int getRenderCache() {
        return renderCache.getOrDefault(Minecraft.getMinecraft().world, -1);
    }

    private static void setRenderCache(int list) {
        renderCache.put(Minecraft.getMinecraft().world, list);
    }

    private static void refreshRenderCache() {
        clearRenderCache();

        int list = GLAllocation.generateDisplayLists(1);
        GlStateManager.glNewList(list, GL11.GL_COMPILE);
        setRenderCache(list);

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buff = tes.getBuffer();
        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for(ICover cover : CoverManager.get(Minecraft.getMinecraft().world).getCovers()) cover.render(buff);

        tes.draw();

        GlStateManager.glEndList();
    }

    private static void clearRenderCache() {
        int list = getRenderCache();
        if(list != -1) {
            GlStateManager.glDeleteLists(list, 1);
            setRenderCache(-1);
        }
    }

    private static void onDirty() {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.isCallingFromMinecraftThread()) clearRenderCache();
        else mc.addScheduledTask(CoverRenderer::clearRenderCache);
    }

    private static Set<World> listening = Collections.newSetFromMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void render(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        CoverManager manager = CoverManager.get(mc.world);
        if(listening.add(mc.world)) manager.onDirty(CoverRenderer::onDirty);
        manager.poll();

        if(getRenderCache() == -1) refreshRenderCache();

        Entity entity = mc.getRenderViewEntity();
        if(entity == null) {
            entity = mc.player;
            if(entity == null) return;
        }

        double tx = entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * event.getPartialTicks());
        double ty = entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * event.getPartialTicks());
        double tz = entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * event.getPartialTicks());

        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.enableLighting();
        GlStateManager.color(1, 1, 1, 1);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-tx, -ty, -tz);

        GlStateManager.callList(getRenderCache());

        GlStateManager.popMatrix();

        GlStateManager.disableLighting();
    }

}
