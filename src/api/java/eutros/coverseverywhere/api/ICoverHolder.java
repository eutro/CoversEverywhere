package eutros.coverseverywhere.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

/**
 * Holds all the {@link ICover}s on a tile entity.
 */
public interface ICoverHolder extends INBTSerializable<NBTTagCompound> {

    /**
     * Get the covers on the given side of this holder.
     * <p>
     * Changes to the list will update the underlying cover holder,
     * so removals and additions can be done in this way.
     *
     * @param side The side of the holder.
     * @return The collection of covers on the given side.
     */
    List<ICover> get(EnumFacing side);

}
