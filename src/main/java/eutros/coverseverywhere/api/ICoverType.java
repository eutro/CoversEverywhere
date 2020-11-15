package eutros.coverseverywhere.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public interface ICoverType extends IForgeRegistryEntry<ICoverType> {

    @Nullable
    ICover makeCover(TileEntity tile, EnumFacing side, NBTTagCompound nbt);

}
