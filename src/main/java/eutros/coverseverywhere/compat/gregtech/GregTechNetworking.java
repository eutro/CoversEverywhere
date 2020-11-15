package eutros.coverseverywhere.compat.gregtech;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import static eutros.coverseverywhere.CoversEverywhere.MOD_ID;

public class GregTechNetworking {

    public static final SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(MOD_ID + ":gregtech");
    public static int discriminator;

    public static void init() {
        TileWrapper.initNetworking(NETWORK);
    }

}
