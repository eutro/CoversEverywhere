package eutros.coverseverywhere.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

/**
 * The type of a cover, used primarily for deserialization.
 *
 * These should be registered with {@link IForgeRegistry#register(IForgeRegistryEntry)}.
 */
public interface ICoverType extends IForgeRegistryEntry<ICoverType> {

    /**
     * Deserialize a cover for the side of the tile entity.
     *
     * @param tile The tile that the cover is on.
     * @param side The side that the cover is on.
     * @param nbt The NBT data of the cover, as serialized by {@link #serialize(ICover)}.
     * @return The deserialized cover, or null if no cover could be deserialized.
     */
    @Nullable
    ICover makeCover(TileEntity tile, EnumFacing side, NBTTagCompound nbt);

    /**
     * Serialize the given cover, such that {@link #makeCover(TileEntity, EnumFacing, NBTTagCompound)}
     * will be able to deserialize it.
     *
     * {@link ICover} implements {@link INBTSerializable} for convenience, so it can be delegated to.
     *
     * @param cover The cover to serialize.
     * @return The serialized cover.
     */
    NBTTagCompound serialize(ICover cover);

}
