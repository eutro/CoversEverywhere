package eutros.coverseverywhere.compat.gregtech;

import eutros.coverseverywhere.api.ICoverType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class GTCECompat {

    private static void init() {
        MinecraftForge.EVENT_BUS.register(GTCECompat.class);
        GregTechItemCompatHandler.init();
    }

    public static void check() {
        if(Loader.isModLoaded("gregtech")) init();
    }

    @SubscribeEvent
    public static void registerCovers(RegistryEvent.Register<ICoverType> evt) {
        IForgeRegistry<ICoverType> r = evt.getRegistry();

        r.register(GregTechCoverType.INSTANCE);
    }

}
