package eutros.coverseverywhere.compat.gregtech;

import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverHolder;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.ICoverable;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.Consumer;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

class TileWrapper implements ICoverable {

    public static final Logger LOGGER = LogManager.getLogger();
    final TileEntity tile;

    public TileWrapper(TileEntity tile) {
        this.tile = tile;
    }

    public static void initNetworking(SimpleNetworkWrapper network) {
        network.registerMessage(new Handler(),
                ClientMessage.class,
                GregTechNetworking.discriminator++,
                Side.CLIENT);
    }

    public static class ClientMessage implements IMessage {

        private BlockPos pos;
        private int discriminator;
        private ByteBuf buf;

        public ClientMessage() {
        }

        public ClientMessage(BlockPos pos, int discriminator, ByteBuf buf) {
            this.pos = pos;
            this.discriminator = discriminator;
            this.buf = buf;
        }

        void handle() {
            doHandle();
            buf.release();
        }

        void doHandle() {
            TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(pos);
            if(tile == null) return;
            ICoverable coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
            if(!(coverable instanceof TileWrapper)) return;
            ((TileWrapper) coverable).handle(discriminator, new PacketBuffer(buf));
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            pos = BlockPos.fromLong(buf.readLong());
            discriminator = buf.readInt();
            this.buf = buf.copy();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeLong(pos.toLong());
            buf.writeInt(discriminator);
            this.buf.forEachByte(b -> {
                buf.writeByte(b);
                return true;
            });
            this.buf.release();
        }

    }

    private static class Handler implements IMessageHandler<ClientMessage, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(ClientMessage message, MessageContext ctx) {
            message.handle();
            return null;
        }

    }

    private void sendToClients(int discriminator, Consumer<PacketBuffer> writer) {
        ByteBuf buf = Unpooled.buffer();
        writer.accept(new PacketBuffer(buf));
        BlockPos pos = tile.getPos();
        GregTechNetworking.NETWORK.sendToAllTracking(new ClientMessage(pos, discriminator, buf),
                new NetworkRegistry.TargetPoint(tile.getWorld().provider.getDimension(),
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        -1));
    }

    void handle(int discriminator, PacketBuffer buf) {
        switch(discriminator) {
            case 0: {
                EnumFacing side = EnumFacing.VALUES[buf.readByte()];
                int coverId = buf.readVarInt();
                CoverDefinition definition = CoverDefinition.getCoverByNetworkId(coverId);
                CoverBehavior behaviour = definition.createCoverBehavior(this, side);
                behaviour.readInitialSyncData(buf);
                ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
                if(holder != null) holder.get(side).add(new GregTechCover(behaviour, tile, side));
                break;
            }
            case 1: {
                EnumFacing side = EnumFacing.VALUES[buf.readByte()];
                CoverBehavior behavior = getCoverAtSide(side);
                int internalId = buf.readVarInt();
                if(behavior != null) behavior.readUpdateData(internalId, buf);
                break;
            }
            case 2: {
                EnumFacing side = EnumFacing.VALUES[buf.readByte()];
                ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
                if(holder != null) {
                    Iterator<ICover> it = holder.get(side).iterator();
                    while(it.hasNext()) {
                        ICover cover = it.next();
                        if(cover instanceof GregTechCover) {
                            it.remove();
                            break;
                        }
                    }
                }
            }
            default: {
                LOGGER.warn("Unrecognised discriminator: {}", discriminator);
            }
        }
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
        // this might need to be reconsidered
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
        sendToClients(0, buf -> {
            buf.writeByte(side.getIndex());
            buf.writeVarInt(CoverDefinition.getNetworkIdForCover(coverDefinition));
            coverBehavior.writeInitialSyncData(buf);
        });
        holder.get(side).add(new GregTechCover(coverBehavior, tile, side));
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
                sendToClients(2, buf -> buf.writeByte(side.getIndex()));
                cover.onRemoved();
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
    public void writeCoverData(CoverBehavior cover, int internalId, Consumer<PacketBuffer> dataWriter) {
        sendToClients(1, buf -> {
            buf.writeByte(cover.attachedSide.getIndex());
            buf.writeVarInt(internalId);
            dataWriter.accept(buf);
        });
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
        return tile.getWorld().getBlockState(tile.getPos()).isFullCube()
                ? 0 : (1.0 / 16.0);
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
