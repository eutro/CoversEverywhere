package eutros.coverseverywhere.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface ICoverType extends IForgeRegistryEntry<ICoverType> {

    ICover makeCover(NBTTagCompound nbt);

}
