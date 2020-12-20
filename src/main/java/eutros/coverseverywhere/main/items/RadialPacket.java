package eutros.coverseverywhere.main.items;

import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverHolder;
import eutros.coverseverywhere.common.networking.Packets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public abstract class RadialPacket implements IMessage {
    public static IMessageHandler<RadialPacket, IMessage> HANDLER = new Handler();

    public static void register(int discriminator, Class<? extends RadialPacket> clazz) {
        Packets.NETWORK.registerMessage(HANDLER, clazz, discriminator, Side.SERVER);
    }

    protected int index;
    protected BlockPos pos;
    protected EnumFacing side;

    public RadialPacket() {
    }

    protected RadialPacket(int index, BlockPos pos, EnumFacing side) {
        this.index = index;
        this.pos = pos;
        this.side = side;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        index = pb.readVarInt();
        pos = pb.readBlockPos();
        side = pb.readEnumValue(EnumFacing.class);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        pb.writeVarInt(index);
        pb.writeBlockPos(pos);
        pb.writeEnumValue(side);
    }

    public abstract void handle(MessageContext ctx);

    protected Optional<List<ICover>> coversFor(MessageContext ctx) {
        WorldServer world = ctx.getServerHandler().player.getServerWorld();
        TileEntity tile = world.getTileEntity(pos);
        if (tile == null) return Optional.empty();
        ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
        if (holder == null) return Optional.empty();
        List<ICover> covers = holder.get(side);
        if (index < 0 || covers.size() <= index) return Optional.empty();
        return Optional.of(covers);
    }

    public static class Handler implements IMessageHandler<RadialPacket, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(RadialPacket message, MessageContext ctx) {
            message.handle(ctx);
            return null;
        }
    }
}
