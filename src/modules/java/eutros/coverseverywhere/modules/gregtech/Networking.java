package eutros.coverseverywhere.modules.gregtech;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import static eutros.coverseverywhere.common.Constants.prefixString;

public class Networking {

    public static final SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(prefixString("gregtech"));
    public static int discriminator;

    public static void init() {
        TileWrapper.initNetworking(NETWORK);
    }

}
