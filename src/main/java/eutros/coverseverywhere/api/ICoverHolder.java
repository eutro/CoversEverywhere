package eutros.coverseverywhere.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Collection;
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
     * Get the covers on the given side of this holder.
     *
     * Changes to the collection will update the underlying cover holder,
     * so removals can be done in this way.
     *
     * @param side The side of the holder.
     * @return The collection of covers on the given side.
     */
    Collection<ICover> get(EnumFacing side);

    /**
     * Drop the {@link ICover#getDrops()} from the given side of this holder.
     *
     * This method is called for each cover when the underlying tile entity is removed.
     *
     * @param side The side to drop from.
     * @param cover The cover whose drops to drop.
     */
    void drop(EnumFacing side, ICover cover);

}
