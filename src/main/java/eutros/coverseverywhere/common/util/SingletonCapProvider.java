package eutros.coverseverywhere.common.util;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SingletonCapProvider<C> implements ICapabilityProvider {

    private final Capability<? super C> target;
    private final C implementation;

    public SingletonCapProvider(Capability<? super C> target, C implementation) {
        this.target = target;
        this.implementation = implementation;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == target;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == target ? target.cast(getImplementation()) : null;
    }

    protected C getImplementation() {
        return implementation;
    }

}
