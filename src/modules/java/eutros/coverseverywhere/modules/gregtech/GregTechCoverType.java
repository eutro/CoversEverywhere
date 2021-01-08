package eutros.coverseverywhere.modules.gregtech;

import eutros.coverseverywhere.api.AbstractCoverType;
import eutros.coverseverywhere.api.ICoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import static eutros.coverseverywhere.common.Constants.prefix;

public class GregTechCoverType extends AbstractCoverType {

    public static final ICoverType INSTANCE = new GregTechCoverType();
    private static final CoverSerializer<?> SERIALIZER = new GregtechCoverSerializer();

    protected GregTechCoverType() {
        super(prefix("gregtech"));
    }

    @Override
    public CoverSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static class GregtechCoverSerializer implements ICoverType.CoverSerializer<GregTechCover> {

        @Override
        public GregTechCover makeCover(TileEntity tile, EnumFacing side, NBTTagCompound nbt) {
            return GregTechCover.deserialize(tile, side, nbt);
        }

        @Override
        public NBTTagCompound serialize(GregTechCover cover) {
            return cover.serialize();
        }

    }

}
