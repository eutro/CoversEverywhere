package eutros.coverseverywhere.common.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class NbtSerializableStorage<T extends INBTSerializable<N>, N extends NBTBase> implements Capability.IStorage<T> {

    @Nullable
    @Override
    public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
        return instance.serializeNBT();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
        instance.deserializeNBT((N) nbt);
    }

}
