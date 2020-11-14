package eutros.coverseverywhere.api;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public interface ICover extends INBTSerializable<NBTTagCompound> {

    ICoverType getType();

    default void tick(@Nonnull TileEntity tile) {
    }

    @SideOnly(Side.CLIENT)
    void render(BufferBuilder buff, BlockPos pos);

}
