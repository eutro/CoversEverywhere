package eutros.coverseverywhere.common.covers;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.AbstractCoverType;
import eutros.coverseverywhere.api.CoverItem;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.common.util.RenderHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConveyorCover implements ICover {

    public static final ResourceLocation NAME = new ResourceLocation(CoversEverywhere.MOD_ID, "conveyor");
    public static Type TYPE = new Type();
    public static Item ITEM = new Item();

    private final TileEntity tile;
    private EnumFacing side;

    public ConveyorCover(TileEntity tile, EnumFacing side) {
        this.side = side;
        this.tile = tile;
    }

    ConveyorCover(TileEntity tile) {
        this.tile = tile;
    }

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
    public void tick() {
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

    @SideOnly(Side.CLIENT)
    @Override
    public void render(BufferBuilder buff) {
        RenderHelper.side(buff, RenderHelper.COVER_SPRITE, tile.getPos(), side);
    }

    @Override
    public List<ItemStack> getDrops() {
        return Collections.singletonList(new ItemStack(ITEM));
    }

    public static class Item extends CoverItem {

        private Item() {
            setRegistryName(NAME);
            setUnlocalizedName(NAME.getResourceDomain() + "." + NAME.getResourcePath());
        }

        @Override
        protected ICover makeCover(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing) {
            return new ConveyorCover(Objects.requireNonNull(worldIn.getTileEntity(pos)), facing);
        }

    }

    public static class Type extends AbstractCoverType<ConveyorCover> {

        private Type() {
            super(NAME);
        }

        @Override
        public ConveyorCover makeCover(TileEntity tile, NBTTagCompound nbt) {
            ConveyorCover cover = new ConveyorCover(tile);
            cover.deserializeNBT(nbt);
            return cover;
        }

    }

}
