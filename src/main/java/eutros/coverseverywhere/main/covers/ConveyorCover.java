package eutros.coverseverywhere.main.covers;

import eutros.coverseverywhere.api.*;
import eutros.coverseverywhere.common.util.RenderHelper;
import eutros.coverseverywhere.main.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;

import static eutros.coverseverywhere.common.Constants.prefix;

public class ConveyorCover implements ICover, INBTSerializable<NBTTagCompound> {

    public static final ResourceLocation NAME = prefix("conveyor");
    public static final Type TYPE = new Type();
    public static final Item ITEM = new Item();

    private static final String TAG_FREQUENCY = "frequency";
    private static final int DEFAULT_FREQUENCY = 20;
    private static final String TAG_COUNT = "count";
    private static final int DEFAULT_COUNT = 64;

    private final TileEntity tile;
    private final EnumFacing side;

    private int frequency;
    private int count;

    public ConveyorCover(TileEntity tile, EnumFacing side) {
        this.side = side;
        this.tile = tile;
    }

    @Override
    public ICoverType getType() {
        return TYPE;
    }

    @Override
    public void onRemoved() {
        Block.spawnAsEntity(tile.getWorld(), tile.getPos(), getRepresentation());
    }

    @Override
    public void tick() {
        World world = tile.getWorld();
        if (frequency < 0 || world.getTotalWorldTime() % frequency != 0) return;

        TileEntity otherTile = world.getTileEntity(tile.getPos().offset(side));

        IItemHandler tileCap, otherCap;
        if (otherTile == null ||
                (tileCap = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) == null ||
                (otherCap = otherTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite())) == null) {
            return;
        }

        int remainingExtraction = count;
        int slot = 0;
        while (remainingExtraction > 0 && slot < tileCap.getSlots()) {
            ItemStack stack = tileCap.extractItem(slot, remainingExtraction, true);
            if (!stack.isEmpty()) {
                int extractedCount = stack.getCount();
                stack = ItemHandlerHelper.insertItemStacked(otherCap, stack, false);
                extractedCount -= stack.getCount();
                if (tileCap.extractItem(slot, extractedCount, false).getCount() != extractedCount) {
                    throw new IllegalStateException(String.format("Actual extraction from slot %d of item handler (%s) contradicts simulated extraction.",
                            slot,
                            tileCap.getClass().getName()));
                }
                remainingExtraction -= extractedCount;
            }
            slot++;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(BufferBuilder buff) {
        RenderHelper.sideDouble(buff, Textures.CONVEYOR_SPRITE, tile.getPos(), side);
    }

    @Override
    public ItemStack getRepresentation() {
        ItemStack stack = new ItemStack(ITEM);
        stack.setTagCompound(serializeNBT());
        return stack;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(TAG_FREQUENCY, frequency);
        tag.setInteger(TAG_COUNT, count);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        frequency = getIntOr(nbt, TAG_FREQUENCY, DEFAULT_FREQUENCY);
        count = getIntOr(nbt, TAG_COUNT, DEFAULT_COUNT);
    }

    static int getIntOr(@Nullable NBTTagCompound tag, String key, int dflt) {
        if (tag == null) return dflt;
        int value = tag.getInteger(key);
        return value <= 0 ? dflt : value;
    }

    public static class Item extends CoverItem {

        private Item() {
            setRegistryName(NAME);
            setUnlocalizedName(NAME.getResourceDomain() + "." + NAME.getResourcePath());
            setCreativeTab(ModItems.CREATIVE_TAB);
        }

        @Override
        protected ICover makeCover(TileEntity tile, ICoverHolder holder, EntityPlayer player, EnumHand hand, EnumFacing side) {
            for (ICover cover : holder.get(side)) if (cover instanceof ConveyorCover) return null;

            ItemStack stack = player.getHeldItem(hand);
            ConveyorCover cover = new ConveyorCover(tile, side);
            cover.deserializeNBT(stack.getTagCompound());
            return cover;
        }

        @SideOnly(Side.CLIENT)
        @Override
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
            NBTTagCompound nbt = stack.getTagCompound();
            String formatting = TextFormatting.GRAY.toString();
            int count = getIntOr(nbt, TAG_COUNT, DEFAULT_COUNT);
            float freq = getIntOr(nbt, TAG_FREQUENCY, DEFAULT_FREQUENCY) / 20F;
            tooltip.add(formatting +
                    I18n.format("item.covers_everywhere.conveyor.tooltip",

                            "" + TextFormatting.RESET + TextFormatting.GREEN + count + formatting,

                            "" + TextFormatting.RESET + TextFormatting.DARK_AQUA +
                                    (Math.floor(freq) == freq ? Integer.toString((int) Math.floor(freq)) : Float.toString(freq)) + formatting)
            );
        }

        @Override
        public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
            if (isInCreativeTab(tab)) {
                ItemStack stack = new ItemStack(this);
                NBTTagCompound tag = new NBTTagCompound();
                stack.setTagCompound(tag);

                tag.setInteger(TAG_FREQUENCY, DEFAULT_FREQUENCY);
                for (int count : new int[]{ 16, 64, 256 }) {
                    tag.setInteger(TAG_COUNT, count);
                    items.add(stack.copy());
                }
            }
        }
    }

    public static class Type extends AbstractCoverType {

        private static final ICoverType.CoverSerializer<?> SERIALIZER = new Serializer();

        private Type() {
            super(NAME);
        }

        @Override
        public CoverSerializer<?> getSerializer() {
            return SERIALIZER;
        }

        private static class Serializer implements CoverSerializer<ConveyorCover> {
            @Override
            public ConveyorCover makeCover(TileEntity tile, EnumFacing side, NBTTagCompound nbt) {
                ConveyorCover cover = new ConveyorCover(tile, side);
                cover.deserializeNBT(nbt);
                return cover;
            }

            @Override
            public NBTTagCompound serialize(ConveyorCover cover) {
                return cover.serializeNBT();
            }
        }
    }

}
