package eutros.coverseverywhere.compat.gregtech;

import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverHolder;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.ICoverable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.Consumer;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

// TODO ensure everything is properly implemented
class TileWrapper implements ICoverable {

    private final TileEntity tile;

    public TileWrapper(TileEntity tile) {
        this.tile = tile;
    }

    @Override
    public World getWorld() {
        return tile.getWorld();
    }

    @Override
    public BlockPos getPos() {
        return tile.getPos();
    }

    @Override
    public long getTimer() {
        return tile.getWorld().getTotalWorldTime();
    }

    @Override
    public void markDirty() {
        tile.markDirty();
    }

    @Override
    public boolean isValid() {
        return !tile.isInvalid();
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing enumFacing) {
        return tile.getCapability(capability, enumFacing);
    }

    @Override
    public boolean placeCoverOnSide(EnumFacing side, ItemStack itemStack, CoverDefinition coverDefinition) {
        ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
        if(holder == null) return false;

        CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, side);
        coverBehavior.onAttached(itemStack);
        holder.put(side, new GregTechCover(coverBehavior, tile, side));
        tile.markDirty();
        return true;
    }

    @Override
    public boolean removeCover(EnumFacing side) {
        ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
        if(holder == null) return false;

        Iterator<ICover> it = holder.get(side).iterator();
        while(it.hasNext()) {
            ICover cover = it.next();
            if(cover instanceof GregTechCover) {
                it.remove();
                tile.markDirty();
                holder.drop(side, cover);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPlaceCoverOnSide(EnumFacing side) {
        return tile.hasCapability(getApi().getHolderCapability(), null);
    }

    @Nullable
    @Override
    public CoverBehavior getCoverAtSide(EnumFacing side) {
        ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
        if(holder == null) return null;

        for(ICover cover : holder.get(side)) {
            if(cover instanceof GregTechCover) return ((GregTechCover) cover).getBehaviour();
        }
        return null;
    }

    @Override
    public void writeCoverData(CoverBehavior coverBehavior, int i, Consumer<PacketBuffer> consumer) {
        // FIXME
    }

    @Override
    public int getInputRedstoneSignal(EnumFacing enumFacing, boolean ignoreCover) {
        // what does ignoreCover even do
        return getWorld().getRedstonePower(getPos(), enumFacing);
    }

    @Override
    public ItemStack getStackForm() {
        IBlockState state = getWorld().getBlockState(getPos());
        return new ItemStack(state.getBlock());
    }

    @Override
    public double getCoverPlateThickness() {
        return 0;
    }

    @Override
    public int getPaintingColor() {
        return 0;
    }

    @Override
    public boolean shouldRenderBackSide() {
        return false;
    }

    @Override
    public void notifyBlockUpdate() {
        IBlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 3);
    }

    @Override
    public void scheduleRenderUpdate() {
        getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
    }

}
