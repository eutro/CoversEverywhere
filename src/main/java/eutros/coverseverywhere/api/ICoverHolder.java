package eutros.coverseverywhere.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Holds and dispatches to the {@link ICover}s on a tile entity.
 */
public interface ICoverHolder extends INBTSerializable<NBTTagCompound> {

    /**
     * Put a cover on the side of this holder.
     *
     * @param side The side to put the cover on.
     * @param cover The cover itself.
     */
    void put(EnumFacing side, ICover cover);

    /**
     * Get the cover of a given type on the given side of this holder.
     *
     * @param side The side the cover is on.
     * @param type The type of the cover.
     * @return The cover that is on the side, or null if there is no such cover.
     */
    @Nullable
    ICover get(EnumFacing side, ICoverType type);

    /**
     * Remove the cover of a given type from the given side of the holder.
     *
     * @param side The side to remove the cover from.
     * @param type The type of the cover to remove.
     * @param drop Whether the cover's {@link ICover#getDrops()} should be dropped.
     * @return The cover that was removed, or null if no cover was removed.
     */
    @Nullable
    ICover remove(EnumFacing side, ICoverType type, boolean drop);

    /**
     * Get the set of cover types that exist on a given side of the holder.
     *
     * @param side The side of the holder to check.
     * @return The set of cover types on that side.
     */
    Set<ICoverType> getTypes(EnumFacing side);

}
