package eutros.coverseverywhere.common.covers.impl;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.ICoverHolder;
import eutros.coverseverywhere.api.ICoverRevealer;
import eutros.coverseverywhere.common.util.NbtSerializableStorage;
import eutros.coverseverywhere.common.util.NoOpStorage;
import eutros.coverseverywhere.common.util.SingletonCapProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class CoversCapabilityProvider extends SingletonCapProvider<CoverHolder> implements INBTSerializable<NBTTagCompound> {

    public CoversCapabilityProvider(TileEntity tile) {
        super(getApi().getHolderCapability(), new CoverHolder(tile));
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(CoversCapabilityProvider.class);
        CapabilityManager.INSTANCE.register(ICoverHolder.class,
                new NbtSerializableStorage<>(),
                () -> new CoverHolder(null));
        CapabilityManager.INSTANCE.register(ICoverRevealer.class,
                new NoOpStorage<>(),
                () -> new ICoverRevealer() {
                });
    }

    private static final ResourceLocation NAME = new ResourceLocation(CoversEverywhere.MOD_ID, "covers");

    @SubscribeEvent
    public static void onTile(AttachCapabilitiesEvent<TileEntity> event) {
        event.addCapability(NAME, new CoversCapabilityProvider(event.getObject()));
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return getImplementation().serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        getImplementation().deserializeNBT(nbt);
    }

}
