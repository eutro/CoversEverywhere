package eutros.coverseverywhere.common;

import eutros.coverseverywhere.api.CoversEverywhereAPI;
import eutros.coverseverywhere.api.ICoverHolder;
import eutros.coverseverywhere.api.ICoverRevealer;
import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.common.covers.Covers;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CoversEverywhereAPIImpl implements CoversEverywhereAPI {

    // soft dependencies shouldn't use this, they can @CapabilityInject themselves.
    @CapabilityInject(ICoverHolder.class)
    public static final Capability<ICoverHolder> COVER_HOLDER_CAPABILITY = null;
    @CapabilityInject(ICoverRevealer.class)
    public static final Capability<ICoverRevealer> COVER_REVEALER_CAPABILITY = null;


    @Override
    public IForgeRegistry<ICoverType> getRegistry() {
        return Covers.REGISTRY;
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
