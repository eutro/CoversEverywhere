package eutros.coverseverywhere.modules.gregtech;

import eutros.coverseverywhere.common.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class Networking {

    public static final SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(Constants.MOD_ID + ":gregtech");
    public static int discriminator;

    public static void init() {
        TileWrapper.initNetworking(NETWORK);
    }

}
