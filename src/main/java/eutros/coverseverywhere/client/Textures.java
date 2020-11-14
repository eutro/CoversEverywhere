package eutros.coverseverywhere.client;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.common.covers.types.ConveyorCover;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = CoversEverywhere.MOD_ID, value = Side.CLIENT)
public class Textures {

    public static TextureAtlasSprite COVER_SPRITE;

    @SubscribeEvent
    public static void stitchTextures(TextureStitchEvent.Pre evt) {
        TextureMap map = evt.getMap();
        COVER_SPRITE = map.registerSprite(transform(ConveyorCover.NAME));
    }

    private static ResourceLocation transform(ResourceLocation loc) {
        return new ResourceLocation(loc.getResourceDomain(), "covers/" + loc.getResourcePath());
    }

}
