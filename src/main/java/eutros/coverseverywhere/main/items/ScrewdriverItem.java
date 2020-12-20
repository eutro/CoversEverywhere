package eutros.coverseverywhere.main.items;

import eutros.coverseverywhere.api.GridSection;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverHolder;
import eutros.coverseverywhere.api.ICoverRevealer;
import eutros.coverseverywhere.common.Constants;
import eutros.coverseverywhere.common.Initialize;
import eutros.coverseverywhere.common.networking.Packets;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class ScrewdriverItem extends Item implements ICoverRevealer {

    public static final ResourceLocation NAME = new ResourceLocation(Constants.MOD_ID, "screwdriver");

    public ScrewdriverItem() {
        setRegistryName(NAME);
        setUnlocalizedName(NAME.getResourceDomain() + "." + NAME.getResourcePath());
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile == null) return EnumActionResult.PASS;

        ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
        if (holder == null) return EnumActionResult.PASS;

        EnumFacing side = GridSection.fromXYZ(facing, hitX, hitY, hitZ).offset(facing);
        List<ICover> covers = holder.get(side);
        if (covers.isEmpty()) return EnumActionResult.FAIL;
        if (worldIn.isRemote) {
            NonNullList<ItemStack> choices = covers.stream()
                    .map(ICover::getRepresentation)
                    .collect(Collectors.toCollection(NonNullList::create));
            RadialGuiScreen.prompt(choices, i -> {
                covers.get(i).configure(player, hand, hitX, hitY, hitZ);
                Packets.NETWORK.sendToServer(new ScrewdriverMessage(i, pos, side, hand, hitX, hitY, hitZ));
            });
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

    private static final int SCREWDRIVER_DISCRIMINATOR = 101;

    @Initialize
    public static void init() {
        RadialPacket.register(SCREWDRIVER_DISCRIMINATOR, ScrewdriverMessage.class);
    }
}
