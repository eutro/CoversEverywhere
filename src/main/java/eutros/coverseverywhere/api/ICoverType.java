package eutros.coverseverywhere.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public interface ICoverType extends IForgeRegistryEntry<ICoverType> {

    ICover makeCover(TileEntity tile, NBTTagCompound nbt);

}
