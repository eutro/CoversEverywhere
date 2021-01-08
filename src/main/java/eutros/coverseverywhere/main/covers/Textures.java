package eutros.coverseverywhere.main.covers;

import eutros.coverseverywhere.common.Initialize;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public class Textures {

    public static TextureAtlasSprite CONVEYOR_SPRITE;
    public static TextureAtlasSprite FILTER_SPRITE_INPUT;
    public static TextureAtlasSprite FILTER_SPRITE_OUTPUT;

    @Initialize(sides = { Side.CLIENT })
    public static void init() {
        MinecraftForge.EVENT_BUS.register(Textures.class);
    }

    @SubscribeEvent
    public static void stitchTextures(TextureStitchEvent.Pre evt) {
        TextureMap map = evt.getMap();
        if (map != Minecraft.getMinecraft().getTextureMapBlocks()) return;
        CONVEYOR_SPRITE = map.registerSprite(transform(ConveyorCover.NAME));
        FILTER_SPRITE_INPUT = map.registerSprite(transform(FilterCover.IN_NAME));
        FILTER_SPRITE_OUTPUT = map.registerSprite(transform(FilterCover.OUT_NAME));
    }

    private static ResourceLocation transform(ResourceLocation loc) {
        return new ResourceLocation(loc.getResourceDomain(), "covers/" + loc.getResourcePath());
    }

}
