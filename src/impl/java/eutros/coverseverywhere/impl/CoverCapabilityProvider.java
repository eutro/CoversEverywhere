package eutros.coverseverywhere.impl;

import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverHolder;
import eutros.coverseverywhere.api.ICoverRevealer;
import eutros.coverseverywhere.common.Constants;
import eutros.coverseverywhere.common.Initialize;
import eutros.coverseverywhere.common.util.NbtSerializableStorage;
import eutros.coverseverywhere.common.util.NoOpStorage;
import eutros.coverseverywhere.common.util.SingletonCapProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.List;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class CoverCapabilityProvider
        extends SingletonCapProvider<CoverHolder>
        implements INBTSerializable<NBTTagCompound> {

    public CoverCapabilityProvider(TileEntity tile) {
        super(getApi().getHolderCapability(), new CoverHolder(tile));
    }

    @Initialize
    public static void init() {
        MinecraftForge.EVENT_BUS.register(CoverCapabilityProvider.class);
        CapabilityManager.INSTANCE.register(ICoverHolder.class, new NbtSerializableStorage<>(), NoOpCoverHolder::new);
        CapabilityManager.INSTANCE.register(ICoverRevealer.class, new NoOpStorage<>(), NoOpCoverRevealer::new);
    }

    private static final ResourceLocation NAME = new ResourceLocation(Constants.MOD_ID, "covers");

    @SubscribeEvent
    public static void onTile(AttachCapabilitiesEvent<TileEntity> event) {
        event.addCapability(NAME, new CoverCapabilityProvider(event.getObject()));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return getImplementation().serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        getImplementation().deserializeNBT(nbt);
    }

    private static class NoOpCoverHolder implements ICoverHolder {

        @Override
        public List<ICover> get(EnumFacing side) {
            return Collections.emptyList();
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return new NBTTagCompound();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
        }

    }

    private static class NoOpCoverRevealer implements ICoverRevealer {

    }

}
