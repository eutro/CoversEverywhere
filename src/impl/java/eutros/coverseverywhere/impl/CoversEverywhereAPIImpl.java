package eutros.coverseverywhere.impl;

import eutros.coverseverywhere.api.CoversEverywhereAPI;
import eutros.coverseverywhere.api.ICoverHolder;
import eutros.coverseverywhere.api.ICoverRevealer;
import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.common.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class CoversEverywhereAPIImpl implements CoversEverywhereAPI {

    // soft dependencies shouldn't use this, they can @CapabilityInject themselves.
    @CapabilityInject(ICoverHolder.class)
    public static final Capability<ICoverHolder> COVER_HOLDER_CAPABILITY = null;
    @CapabilityInject(ICoverRevealer.class)
    public static final Capability<ICoverRevealer> COVER_REVEALER_CAPABILITY = null;

    private static IForgeRegistry<ICoverType> REGISTRY;

    // fires before pre-init *grumble grumble*
    @SubscribeEvent
    public static void registerRegistriesEvent(RegistryEvent.NewRegistry evt) {
        REGISTRY = new RegistryBuilder<ICoverType>()
                .setName(new ResourceLocation(Constants.MOD_ID, "covers"))
                .setType(ICoverType.class)
                .create();
    }

    @Override
    public IForgeRegistry<ICoverType> getRegistry() {
        return REGISTRY;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Capability<ICoverHolder> getHolderCapability() {
        return COVER_HOLDER_CAPABILITY;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Capability<ICoverRevealer> getRevealerCapability() {
        return COVER_REVEALER_CAPABILITY;
    }

}
