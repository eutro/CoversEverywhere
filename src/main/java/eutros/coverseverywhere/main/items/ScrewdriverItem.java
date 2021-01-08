package eutros.coverseverywhere.main.items;

import eutros.coverseverywhere.api.GridSection;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverHolder;
import eutros.coverseverywhere.api.ICoverRevealer;
import eutros.coverseverywhere.common.Initialize;
import eutros.coverseverywhere.common.networking.Packets;
import eutros.coverseverywhere.main.gui.RadialGuiScreen;
import eutros.coverseverywhere.main.gui.RadialPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;
import static eutros.coverseverywhere.common.Constants.prefix;

public class ScrewdriverItem extends Item implements ICoverRevealer {

    public static final ResourceLocation NAME = prefix("screwdriver");

    public ScrewdriverItem() {
        setRegistryName(NAME);
        setUnlocalizedName(NAME.getResourceDomain() + "." + NAME.getResourcePath());
        setMaxStackSize(1);
        setCreativeTab(ModItems.CREATIVE_TAB);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GRAY + I18n.format("item.covers_everywhere.screwdriver.tooltip"));
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, EnumHand hand) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile == null) return EnumActionResult.PASS;

        ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
        if (holder == null) return EnumActionResult.PASS;

        EnumFacing side = GridSection.fromXYZ(facing, hitX, hitY, hitZ).offset(facing);
        List<ICover> covers = holder.get(side);
        if (covers.isEmpty()) return EnumActionResult.FAIL;
        if (world.isRemote) {
            NonNullList<ItemStack> choices = covers.stream()
                    .map(ICover::getRepresentation)
                    .collect(Collectors.toCollection(NonNullList::create));
            RadialGuiScreen.prompt(choices, i -> {
                covers.get(i).configure(player, hand, hitX, hitY, hitZ);
                Packets.NETWORK.sendToServer(new ScrewdriverMessage(i, pos, side, hand, hitX, hitY, hitZ));
            });
        } else {
            getApi().synchronize((WorldServer) world, pos, side);
        }
        return EnumActionResult.SUCCESS;
    }

    public static class ScrewdriverMessage extends RadialPacket {
        private EnumHand hand;
        private float hitX;
        private float hitY;
        private float hitZ;

        public ScrewdriverMessage() {
        }

        public ScrewdriverMessage(int index, BlockPos pos, EnumFacing side,
                                  EnumHand hand, float hitX, float hitY, float hitZ) {
            super(index, pos, side);
            this.hand = hand;
            this.hitX = hitX;
            this.hitY = hitY;
            this.hitZ = hitZ;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            super.fromBytes(buf);
            hand = buf.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
            hitX = buf.readFloat();
            hitY = buf.readFloat();
            hitZ = buf.readFloat();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            super.toBytes(buf);
            buf.writeBoolean(hand == EnumHand.MAIN_HAND);
            buf.writeFloat(hitX);
            buf.writeFloat(hitY);
            buf.writeFloat(hitZ);
        }

        @Override
        public void handle(MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            coversFor(ctx).ifPresent(covers ->
                    Objects.requireNonNull(player.getServer()).addScheduledTask(() ->
                            covers.get(index).configure(player, hand, hitX, hitY, hitZ)));
        }
    }

    @Initialize
    public static void init() {
        RadialPacket.register(Packets.SCREWDRIVER_DISCRIMINATOR, ScrewdriverMessage.class);
    }
}
