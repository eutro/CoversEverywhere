package eutros.coverseverywhere.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.INBTSerializable;

public interface ICoverHolder extends INBTSerializable<NBTTagCompound> {

    void put(EnumFacing side, ICover cover);

}
