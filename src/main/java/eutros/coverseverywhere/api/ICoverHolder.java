package eutros.coverseverywhere.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public interface ICoverHolder extends INBTSerializable<NBTTagCompound> {

    void put(EnumFacing side, ICover cover);

    @Nullable
    ICover get(EnumFacing side, ICoverType type);

    @Nullable
    ICover remove(EnumFacing side, ICoverType type, boolean drop);

}
