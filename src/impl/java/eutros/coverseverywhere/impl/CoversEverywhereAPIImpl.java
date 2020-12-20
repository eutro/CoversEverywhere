package eutros.coverseverywhere.impl;

import eutros.coverseverywhere.api.*;
import eutros.coverseverywhere.common.Constants;
import eutros.coverseverywhere.common.Initialize;
import eutros.coverseverywhere.common.networking.Packets;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class CoversEverywhereAPIImpl implements CoversEverywhereAPI {

    // soft dependencies shouldn't use this, they can @CapabilityInject themselves.
    @CapabilityInject(ICoverHolder.class)
    public static final Capability<ICoverHolder> COVER_HOLDER_CAPABILITY = null;
    @CapabilityInject(ICoverRevealer.class)
    public static final Capability<ICoverRevealer> COVER_REVEALER_CAPABILITY = null;

    private static IForgeRegistry<ICoverType> REGISTRY;

    private static final int SYNCHRONIZE_DISCRIMINATOR = 0;

    @Initialize
    public static void init() {
        Packets.NETWORK.registerMessage(new SynchronizeMessageHandler(),
                SynchronizeMessage.class,
                SYNCHRONIZE_DISCRIMINATOR,
                Side.CLIENT);
    }

    // fires before pre-init *grumble grumble*
    @SubscribeEvent
    public static void registerRegistriesEvent(RegistryEvent.NewRegistry evt) {
        REGISTRY = new RegistryBuilder<ICoverType>()
                .setName(new ResourceLocation(Constants.MOD_ID, "covers"))
                .setType(ICoverType.class)
                .create();
    }

    @Override
    public IForgeRegistry<ICoverType> getRegistry() {
        return REGISTRY;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Capability<ICoverHolder> getHolderCapability() {
        return COVER_HOLDER_CAPABILITY;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Capability<ICoverRevealer> getRevealerCapability() {
        return COVER_REVEALER_CAPABILITY;
    }

    @Override
    public void synchronize(WorldServer world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile == null) return;
        ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
        if (holder == null) return;
        Packets.NETWORK.sendToAllTracking(new SynchronizeMessage(pos, side, holder.get(side)),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(),
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        -1));
    }

    public static class SynchronizeMessage implements IMessage {
        public List<Pair<ICoverType, NBTTagCompound>> covers;
        private BlockPos pos;
        private EnumFacing side;

        public SynchronizeMessage() {
        }

        public SynchronizeMessage(BlockPos pos, EnumFacing side, Collection<ICover> covers) {
            this.pos = pos;
            this.side = side;
            this.covers = new ArrayList<>(covers.size());
            for (ICover cover : covers) {
                ICoverType type = cover.getType();
                this.covers.add(Pair.of(type, type.serialize(cover)));
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            try {
                PacketBuffer pb = new PacketBuffer(buf);
                pos = pb.readBlockPos();
                side = pb.readEnumValue(EnumFacing.class);
                int size = pb.readVarInt();
                covers = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    covers.add(Pair.of(
                            getApi().getRegistry().getValue(pb.readResourceLocation()),
                            pb.readCompoundTag()
                    ));
                }
            } catch (IOException e) {
                throw new RuntimeException(e); // why
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            PacketBuffer pb = new PacketBuffer(buf);
            pb.writeBlockPos(pos);
            pb.writeEnumValue(side);
            pb.writeVarInt(covers.size());
            for (Pair<ICoverType, NBTTagCompound> pair : covers) {
                pb.writeResourceLocation(Objects.requireNonNull(pair.getLeft().getRegistryName()));
                pb.writeCompoundTag(pair.getRight());
            }
        }
    }

    public static class SynchronizeMessageHandler implements IMessageHandler<SynchronizeMessage, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(SynchronizeMessage message, MessageContext ctx) {
            World world = Minecraft.getMinecraft().player.world;
            TileEntity tile = world.getTileEntity(message.pos);
            if (tile == null) return null;
            ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
            if (holder == null) return null;
            Collection<ICover> covers = holder.get(message.side);
            covers.clear();
            for (Pair<ICoverType, NBTTagCompound> pair : message.covers) {
                covers.add(pair.getLeft().makeCover(tile, message.side, pair.getRight()));
            }
            return null;
        }
    }
}
