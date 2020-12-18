package eutros.coverseverywhere.modules.gregtech;

import eutros.coverseverywhere.api.AbstractCoverType;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.common.Constants;
import gregtech.api.cover.CoverDefinition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class GregTechCoverType extends AbstractCoverType {

    public static final ICoverType INSTANCE = new GregTechCoverType();

    private static final String TYPE_KEY = "type";
    private static final String DATA_KEY = "data";

    protected GregTechCoverType() {
        super(Constants.MOD_ID, "gregtech");
    }

    @Override
    public ICover makeCover(TileEntity tile, EnumFacing side, NBTTagCompound nbt) {
        CoverDefinition definition = CoverDefinition.getCoverById(new ResourceLocation(nbt.getString(TYPE_KEY)));
        if (definition == null) return null;
        GregTechCover cover = new GregTechCover(definition.createCoverBehavior(new TileWrapper(tile), side), tile, side);
        cover.deserializeNBT(nbt.getCompoundTag(DATA_KEY));
        return cover;
    }

    @Override
    public NBTTagCompound serialize(ICover cover) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(TYPE_KEY,
                ((GregTechCover) cover)
                        .getBehaviour()
                        .getCoverDefinition()
                        .getCoverId()
                        .toString());
        nbt.setTag(DATA_KEY, cover.serializeNBT());
        return nbt;
    }

}
