package eutros.coverseverywhere.compat.gregtech;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.AbstractCoverType;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverType;
import gregtech.api.cover.CoverDefinition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class GregTechCoverType extends AbstractCoverType {

    public static final ICoverType INSTANCE = new GregTechCoverType();

    public static final String DEFINITION_KEY = CoversEverywhere.MOD_ID + ":gregtech_definition_key";

    protected GregTechCoverType() {
        super(CoversEverywhere.MOD_ID, "gregtech");
    }

    @Override
    public ICover makeCover(TileEntity tile, EnumFacing side, NBTTagCompound nbt) {
        CoverDefinition definition = CoverDefinition.getCoverById(new ResourceLocation(nbt.getString(DEFINITION_KEY)));
        if(definition == null) return null;
        GregTechCover cover = new GregTechCover(definition.createCoverBehavior(new TileWrapper(tile), side), tile, side);
        cover.deserializeNBT(nbt);
        return cover;
    }

}
