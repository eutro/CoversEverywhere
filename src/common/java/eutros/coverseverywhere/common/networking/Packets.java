package eutros.coverseverywhere.common.networking;

import eutros.coverseverywhere.common.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class Packets {

    public static final SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(Constants.MOD_ID + ":main");

}
