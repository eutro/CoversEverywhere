package eutros.coverseverywhere.compat.gregtech;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.common.util.SingletonCapProvider;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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

    @SubscribeEvent
    public static void onTile(AttachCapabilitiesEvent<TileEntity> evt) {
        if(!(evt.getObject() instanceof MetaTileEntityHolder)) {
            evt.addCapability(new ResourceLocation(CoversEverywhere.MOD_ID, "coverable"),
                    new SingletonCapProvider<>(GregtechTileCapabilities.CAPABILITY_COVERABLE,
                            new TileWrapper(evt.getObject())));
        }
    }

}
