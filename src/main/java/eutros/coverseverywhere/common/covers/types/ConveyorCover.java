package eutros.coverseverywhere.common.covers.types;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.AbstractCoverType;
import eutros.coverseverywhere.api.CoverItem;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.client.Textures;
import eutros.coverseverywhere.client.util.RenderHelper;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Collections;
import java.util.List;

public class ConveyorCover implements ICover {

    public static final ResourceLocation NAME = new ResourceLocation(CoversEverywhere.MOD_ID, "conveyor");
    public static final Type TYPE = new Type();
    public static final Item ITEM = new Item();

    private final TileEntity tile;
    private final EnumFacing side;

    public ConveyorCover(TileEntity tile, EnumFacing side) {
        this.side = side;
        this.tile = tile;
    }

    @Override
    public ICoverType getType() {
        return TYPE;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
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
        if(MinecraftForgeClient.getRenderLayer() == BlockRenderLayer.CUTOUT) {
            RenderHelper.side(buff, Textures.CONVEYOR_SPRITE, tile.getPos(), side);
        }
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
        protected ICover makeCover(TileEntity tile, EntityPlayer player, EnumHand hand, EnumFacing side) {
            return new ConveyorCover(tile, side);
        }

    }

    public static class Type extends AbstractCoverType {

        private Type() {
            super(NAME);
        }

        @Override
        public ConveyorCover makeCover(TileEntity tile, EnumFacing side, NBTTagCompound nbt) {
            ConveyorCover cover = new ConveyorCover(tile, side);
            cover.deserializeNBT(nbt);
            return cover;
        }

        @Override
        public NBTTagCompound serialize(ICover cover) {
            return cover.serializeNBT();
        }

    }

}
