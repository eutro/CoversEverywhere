package eutros.coverseverywhere.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

/**
 * The type of a cover, used primarily for deserialization.
 * Only one of each cover type can be on a given side of a tile entity.
 *
 * These should be registered with {@link IForgeRegistry#register(IForgeRegistryEntry)}.
 */
public interface ICoverType extends IForgeRegistryEntry<ICoverType> {

    /**
     * Deserialize a cover for the side of the tile entity.
     *
     * @param tile The tile that the cover is on.
     * @param side The side that the cover is on.
     * @param nbt The NBT data of the cover, as serialized by {@link ICover#serializeNBT()}.
     * @return The deserialized cover, or null if no cover could be deserialized.
     */
    @Nullable
    ICover makeCover(TileEntity tile, EnumFacing side, NBTTagCompound nbt);

}
