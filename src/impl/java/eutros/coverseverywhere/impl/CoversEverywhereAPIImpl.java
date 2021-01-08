package eutros.coverseverywhere.impl;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
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
import java.util.*;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;
import static eutros.coverseverywhere.common.Constants.prefix;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class CoversEverywhereAPIImpl implements CoversEverywhereAPI {

    // soft dependencies shouldn't use this, they can @CapabilityInject themselves.
    @CapabilityInject(ICoverHolder.class)
    public static final Capability<ICoverHolder> COVER_HOLDER_CAPABILITY = null;
    @CapabilityInject(ICoverRevealer.class)
    public static final Capability<ICoverRevealer> COVER_REVEALER_CAPABILITY = null;

    private static IForgeRegistry<ICoverType> REGISTRY;

    @Initialize
    public static void init() {
        Packets.NETWORK.registerMessage(new SynchronizeMessageHandler(), SynchronizeMessage.class, Packets.SYNCHRONIZE_DISCRIMINATOR, Side.CLIENT);
    }

    // fires before pre-init *grumble grumble*
    @SubscribeEvent
    public static void registerRegistriesEvent(RegistryEvent.NewRegistry evt) {
        REGISTRY = new RegistryBuilder<ICoverType>()
                .setName(prefix("covers"))
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
    public void synchronize(WorldServer world, BlockPos pos, EnumFacing... sides) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile == null) return;
        ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
        if (holder == null) return;
        Packets.NETWORK.sendToAllTracking(new SynchronizeMessage(pos, holder, sides),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(),
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        -1));
    }

    public static class SynchronizeMessage implements IMessage {
        public Multimap<EnumFacing, Pair<ICoverType, NBTTagCompound>> covers = Multimaps.newMultimap(new EnumMap<>(EnumFacing.class), LinkedList::new);
        private BlockPos pos;
        private EnumFacing[] sides;

        public SynchronizeMessage() {
        }

        public SynchronizeMessage(BlockPos pos, ICoverHolder holder, EnumFacing[] sides) {
            this.pos = pos;
            this.sides = sides;
            for (EnumFacing side : sides) {
                for (ICover cover : holder.get(side)) {
                    ICoverType type = cover.getType();
                    this.covers.put(side, Pair.of(type, type.getSerializer().uncheckedSerialize(cover)));
                }
            }
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            try {
                PacketBuffer pb = new PacketBuffer(buf);
                pos = pb.readBlockPos();
                sides = new EnumFacing[pb.readVarInt()];
                for (int i = 0; i < sides.length; i++) {
                    sides[i] = pb.readEnumValue(EnumFacing.class);
                }
                for (EnumFacing side : sides) {
                    int size = pb.readVarInt();
                    for (int i = 0; i < size; i++) {
                        covers.put(side,
                                Pair.of(
                                        getApi().getRegistry().getValue(pb.readResourceLocation()),
                                        pb.readCompoundTag()
                                ));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e); // why
            }
        }

        @Override
        public void toBytes(ByteBuf buf) {
            PacketBuffer pb = new PacketBuffer(buf);
            pb.writeBlockPos(pos);
            pb.writeVarInt(sides.length);
            for (EnumFacing side : sides) {
                pb.writeEnumValue(side);
            }
            for (EnumFacing side : sides) {
                Collection<Pair<ICoverType, NBTTagCompound>> covers = this.covers.get(side);
                pb.writeVarInt(covers.size());
                for (Pair<ICoverType, NBTTagCompound> pair : covers) {
                    pb.writeResourceLocation(Objects.requireNonNull(pair.getLeft().getRegistryName()));
                    pb.writeCompoundTag(pair.getRight());
                }
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
            for (EnumFacing side : message.sides) {
                Collection<ICover> covers = holder.get(side);
                covers.clear();
                for (Pair<ICoverType, NBTTagCompound> pair : message.covers.get(side)) {
                    covers.add(pair.getLeft().getSerializer().makeCover(tile, side, pair.getRight()));
                }
            }
            return null;
        }
    }
}
