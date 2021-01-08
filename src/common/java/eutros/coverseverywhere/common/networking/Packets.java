package eutros.coverseverywhere.common.networking;

import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import static eutros.coverseverywhere.common.Constants.prefixString;

public class Packets {

    public static final SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(prefixString("main"));

    public static final int SYNCHRONIZE_DISCRIMINATOR = 0;
    public static final int CROWBAR_DISCRIMINATOR = 1;
    public static final int SCREWDRIVER_DISCRIMINATOR = 2;
    public static final int FILTER_DISCRIMINATOR = 3;
}
