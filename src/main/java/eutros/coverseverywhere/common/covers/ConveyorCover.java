package eutros.coverseverywhere.common.covers;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.AbstractCoverType;
import eutros.coverseverywhere.api.CoverItem;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class ConveyorCover implements ICover {

    public static final ResourceLocation NAME = new ResourceLocation(CoversEverywhere.MOD_ID, "conveyor");
    public static Type TYPE = new Type();
    public static Item ITEM = new Item();

    private EnumFacing side;

    public ConveyorCover(@Nonnull EnumFacing side) {
        this.side = side;
    }

    ConveyorCover() {
    }

    @Nonnull
    @Override
    public ICoverType getType() {
        return TYPE;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("side", side.getName());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        side = EnumFacing.byName(nbt.getString("side"));
    }

    @Override
    public void tick(@Nonnull TileEntity tile) {
        TileEntity otherTile = tile.getWorld().getTileEntity(tile.getPos().offset(side));
        if(otherTile == null ||
                !tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) ||
                !otherTile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()))
            return;

        IItemHandler tileCap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
        assert tileCap != null;
        ItemStack toExtract = ItemStack.EMPTY;
        int slot;
        for(slot = 0; slot < tileCap.getSlots(); slot++) {
            toExtract = tileCap.extractItem(slot, Integer.MAX_VALUE, true);
            if(!toExtract.isEmpty()) break;
        }
        if(toExtract.isEmpty()) return;

        IItemHandler otherCap = otherTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
        ItemStack uninserted = ItemHandlerHelper.insertItemStacked(otherCap, toExtract, false);

        int extractedCount = toExtract.getCount() - uninserted.getCount();
        if(extractedCount > 0) tileCap.extractItem(slot, extractedCount, false);
    }

    public static class Item extends CoverItem {

        private Item() {
            setRegistryName(NAME);
        }

        @Nonnull
        @ParametersAreNonnullByDefault
        @Override
        protected ICover makeCover(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing) {
            return new ConveyorCover(facing);
        }

    }

    public static class Type extends AbstractCoverType<ConveyorCover> {

        private Type() {
            super(NAME);
        }

        @Nonnull
        @Override
        public ConveyorCover makeCover(@Nonnull NBTTagCompound nbt) {
            ConveyorCover cover = new ConveyorCover();
            cover.deserializeNBT(nbt);
            return cover;
        }

    }

}
