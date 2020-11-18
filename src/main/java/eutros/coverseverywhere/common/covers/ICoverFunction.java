package eutros.coverseverywhere.common.covers;

import eutros.coverseverywhere.api.ICoverHolder;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public interface ICoverFunction extends ICoverHolder {

    @Nullable
    TileEntity getTile();

    void invalidate();

}
