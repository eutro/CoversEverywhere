package eutros.coverseverywhere.api;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public interface ICover extends INBTSerializable<NBTTagCompound> {

    ICoverType getType();

    default void tick() {
    }

    @SideOnly(Side.CLIENT)
    void render();

    List<ItemStack> getDrops();

}
