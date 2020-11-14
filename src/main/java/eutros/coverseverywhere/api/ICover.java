package eutros.coverseverywhere.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

public interface ICover extends INBTSerializable<NBTTagCompound> {

    ICoverType getType();

    default void tick(@Nonnull TileEntity tile) {
    }

}
