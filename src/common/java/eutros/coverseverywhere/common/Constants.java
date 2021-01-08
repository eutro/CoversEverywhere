package eutros.coverseverywhere.common;

import net.minecraft.util.ResourceLocation;

public interface Constants {
    String MOD_ID = "covers_everywhere";
    String MOD_NAME = "Covers Everywhere";
    String VERSION = "GRADLE:VERSION";

    static ResourceLocation prefix(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    static String prefixString(String path) {
        return MOD_ID + ":" + path;
    }
}
