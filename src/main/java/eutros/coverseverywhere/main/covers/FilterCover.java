package eutros.coverseverywhere.main.covers;

import eutros.coverseverywhere.api.*;
import eutros.coverseverywhere.common.Initialize;
import eutros.coverseverywhere.common.networking.Packets;
import eutros.coverseverywhere.common.util.DelegatingItemHandler;
import eutros.coverseverywhere.common.util.RenderHelper;
import eutros.coverseverywhere.main.items.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static eutros.coverseverywhere.common.Constants.prefix;

public class FilterCover implements ICover, INBTSerializable<NBTTagCompound> {
    public static final ResourceLocation NAME = prefix("filter");
    public static final ResourceLocation IN_NAME = prefix("filter_in");
    public static final ResourceLocation OUT_NAME = prefix("filter_out");
    public static final Type TYPE = new Type();
    public static final Item ITEM = new Item();

    private final TileEntity tile;
    private final EnumFacing side;

    private boolean isInsertion = true;
    private StackFilter filter = new StackFilter();

    public FilterCover(TileEntity tile, EnumFacing side) {
        this.tile = tile;
        this.side = side;
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
    public void render(BufferBuilder bufferBuilder) {
        RenderHelper.sideDouble(bufferBuilder, getSprite(), tile.getPos(), side);
    }

    private TextureAtlasSprite getSprite() {
        return isInsertion ? Textures.FILTER_SPRITE_INPUT : Textures.FILTER_SPRITE_OUTPUT;
    }

    @Nullable
    @Override
    public <T> T wrapCapability(@Nullable T toWrap, Capability<T> capability) {
        if (toWrap != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(wrap((IItemHandler) toWrap));
        }
        return toWrap;
    }

    private IItemHandler wrap(IItemHandler toWrap) {
        return new WrappedItemHandler(toWrap);
    }

    @Override
    public ItemStack getRepresentation() {
        ItemStack stack = new ItemStack(ITEM);
        stack.setTagCompound(serializeNBT());
        return stack;
    }

    @Override
    public void configure(EntityPlayer player, EnumHand hand, float hitX, float hitY, float hitZ) {
        isInsertion ^= true;
    }

    private static final String TAG_INSERTION = "insertion";

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = filter.serializeNBT();
        tag.setBoolean(TAG_INSERTION, isInsertion);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        filter = StackFilter.from(nbt);
        isInsertion = nbt.getBoolean(TAG_INSERTION);
    }

    public static class Item extends CoverItem {
        public Item() {
            setRegistryName(NAME);
            setCreativeTab(ModItems.CREATIVE_TAB);
            setUnlocalizedName(NAME.getResourceDomain() + "." + NAME.getResourcePath());
        }

        @Nullable
        @Override
        protected ICover makeCover(TileEntity tile, ICoverHolder holder, EntityPlayer player, EnumHand hand, EnumFacing side) {
            FilterCover cover = new FilterCover(tile, side);
            NBTTagCompound tag = player.getHeldItem(hand).getTagCompound();
            cover.deserializeNBT(tag == null ? new NBTTagCompound() : tag);
            return cover;
        }

        @Override
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
            StackFilter.from(stack.getTagCompound()).addInformation(tooltip);
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
            if (!playerIn.isSneaking()) return super.onItemRightClick(worldIn, playerIn, handIn);
            ItemStack filterStack = playerIn.getHeldItem(handIn);
            if (worldIn.isRemote) {
                StackFilter filter = StackFilter.from(filterStack.getTagCompound());
                filter.prompt(stack -> {
                    if (playerIn.getHeldItem(handIn) != filterStack) return;
                    filter.xorStack(stack);
                    NBTTagCompound tag = filterStack.getTagCompound();
                    if (tag == null) tag = new NBTTagCompound();
                    tag.merge(filter.serializeNBT());
                    filterStack.setTagCompound(tag);
                    Packets.NETWORK.sendToServer(new FilterPopMessage(stack, handIn));
                });
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, filterStack);
        }

        public static class FilterPopMessage implements IMessage, IMessageHandler<FilterPopMessage, IMessage> {
            private ItemStack stack;
            private EnumHand hand;

            public FilterPopMessage() {
            }

            public FilterPopMessage(ItemStack stack, EnumHand hand) {
                this.stack = stack;
                this.hand = hand;
            }

            @Override
            public void fromBytes(ByteBuf buf) {
                PacketBuffer pb = new PacketBuffer(buf);
                try {
                    stack = pb.readItemStack();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                hand = pb.readEnumValue(EnumHand.class);
            }

            @Override
            public void toBytes(ByteBuf buf) {
                PacketBuffer pb = new PacketBuffer(buf);
                pb.writeItemStack(stack);
                pb.writeEnumValue(hand);
            }

            @Nullable
            @Override
            public IMessage onMessage(FilterPopMessage message, MessageContext ctx) {
                NetHandlerPlayServer handler = ctx.getServerHandler();
                EntityPlayerMP player = handler.player;
                MinecraftServer server = player.getServer();
                assert server != null;
                if (!server.isCallingFromMinecraftThread()) {
                    server.addScheduledTask(() -> onMessage(message, ctx));
                    return null;
                }
                ItemStack stack = player.getHeldItem(message.hand);
                NBTTagCompound tag = stack.getTagCompound();
                StackFilter filter = StackFilter.from(tag);
                filter.xorStack(message.stack);
                if (tag == null) tag = new NBTTagCompound();
                tag.merge(filter.serializeNBT());
                stack.setTagCompound(tag);
                return null;
            }
        }

        @Initialize
        public static void init() {
            Packets.NETWORK.registerMessage(new FilterPopMessage(), FilterPopMessage.class, Packets.FILTER_DISCRIMINATOR, Side.SERVER);
        }
    }

    public static class Type extends AbstractCoverType {
        private static final CoverSerializer<?> SERIALIZER = new Serializer();

        protected Type() {
            super(NAME);
        }

        @Override
        public CoverSerializer<?> getSerializer() {
            return SERIALIZER;
        }

        private static class Serializer implements CoverSerializer<FilterCover> {
            @Nullable
            @Override
            public FilterCover makeCover(TileEntity tile, EnumFacing side, NBTTagCompound nbt) {
                FilterCover cover = new FilterCover(tile, side);
                cover.deserializeNBT(nbt);
                return cover;
            }

            @Override
            public NBTTagCompound serialize(FilterCover cover) {
                return cover.serializeNBT();
            }
        }
    }

    private class WrappedItemHandler extends DelegatingItemHandler {
        private final IItemHandler delegate;

        public WrappedItemHandler(IItemHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        protected IItemHandler getDelegate() {
            return delegate;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (isInsertion && !filter.test(stack)) return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (isInsertion) {
                return super.extractItem(slot, amount, simulate);
            } else {
                ItemStack simulated = super.extractItem(slot, amount, true);
                if (!filter.test(simulated)) return ItemStack.EMPTY;
                return simulate ? simulated : super.extractItem(slot, amount, true);
            }
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (isInsertion && !filter.test(stack)) return false;
            return super.isItemValid(slot, stack);
        }
    }
}
