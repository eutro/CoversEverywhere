package eutros.coverseverywhere.common.covers;

import eutros.coverseverywhere.api.ICover;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class CoversFunctionHandler {

    private static final ThreadLocal<Set<ICoverFunction>> providers =
            ThreadLocal.withInitial(() -> Collections.newSetFromMap(Collections.synchronizedMap(new WeakHashMap<>())));

    public static void init() {
        MinecraftForge.EVENT_BUS.register(CoversFunctionHandler.class);
    }

    public static void register(ICoverFunction provider) {
        providers.get().add(provider);
    }

    private static void destroy(ICoverFunction provider) {
        if(provider.getTile() != null) {
            for(EnumFacing side : EnumFacing.values()) {
                Collection<ICover> covers = provider.get(side);
                for(ICover cover : covers) {
                    cover.onRemoved();
                    provider.drop(side, cover);
                }
                covers.clear();
            }
            provider.invalidate();
        }
    }

    private static void clearInvalid() {
        Iterator<ICoverFunction> it = providers.get().iterator();
        while(it.hasNext()) {
            ICoverFunction provider = it.next();
            TileEntity tile = provider.getTile();
            if(tile == null || tile.isInvalid()) {
                destroy(provider);
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            clearInvalid();
            for(ICoverFunction provider : providers.get()) {
                for(EnumFacing side : EnumFacing.values()) {
                    for(ICover cover : provider.get(side)) {
                        cover.tick();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

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

        ForgeHooksClient.setRenderLayer(BlockRenderLayer.CUTOUT);
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buff = tes.getBuffer();
        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        clearInvalid();
        for(ICoverFunction provider : providers.get()) {
            for(EnumFacing side : EnumFacing.values()) {
                for(ICover cover : provider.get(side)) {
                    cover.render(buff);
                }
            }
        }

        tes.draw();

        GlStateManager.popMatrix();

        GlStateManager.disableLighting();
    }

}
