package eutros.coverseverywhere.modules.gregtech;

import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.common.Initialize;
import eutros.coverseverywhere.common.util.SingletonCapProvider;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import static eutros.coverseverywhere.common.Constants.prefix;

public class GTCECompat {

    @Initialize(requiredMods = { "gregtech" })
    public static void init() {
        MinecraftForge.EVENT_BUS.register(GTCECompat.class);
        ItemCompatHandler.init();
        Networking.init();
    }

    @SubscribeEvent
    public static void registerCovers(RegistryEvent.Register<ICoverType> evt) {
        IForgeRegistry<ICoverType> r = evt.getRegistry();

        r.register(GregTechCoverType.INSTANCE);
    }

    @SubscribeEvent
    public static void onTile(AttachCapabilitiesEvent<TileEntity> evt) {
        if (!(evt.getObject() instanceof MetaTileEntityHolder)) {
            evt.addCapability(prefix("coverable"),
                    new SingletonCapProvider<>(GregtechTileCapabilities.CAPABILITY_COVERABLE,
                            new TileWrapper(evt.getObject())));
        }
    }

}
