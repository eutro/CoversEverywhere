package eutros.coverseverywhere.common.covers;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverHolder;
import eutros.coverseverywhere.api.ICoverRevealer;
import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.common.util.CapHelper;
import eutros.coverseverywhere.common.util.NbtSerializableStorage;
import eutros.coverseverywhere.common.util.NoOpStorage;
import eutros.coverseverywhere.common.util.SingletonCapProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class CoversCapabilityProvider extends SingletonCapProvider<ICoverHolder> implements ICapabilityProvider, ICoverHolder {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation NAME = new ResourceLocation(CoversEverywhere.MOD_ID, "covers");

    private final Multimap<EnumFacing, ICover> covers = LinkedHashMultimap.create();
    @Nullable // null means this has been invalidated
    private TileEntity tile;

    public CoversCapabilityProvider(@Nullable TileEntity tile) {
        super(getApi().getHolderCapability(), ICoverHolder.class);
        this.tile = tile;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(CoversCapabilityProvider.class);
        CapabilityManager.INSTANCE.register(ICoverHolder.class,
                new NbtSerializableStorage<>(),
                () -> new CoversCapabilityProvider(null));
        CapabilityManager.INSTANCE.register(ICoverRevealer.class,
                new NoOpStorage<>(),
                () -> new ICoverRevealer() {
                });
    }

    @SubscribeEvent
    public static void onTile(AttachCapabilitiesEvent<TileEntity> event) {
        event.addCapability(NAME, new CoversCapabilityProvider(event.getObject()));
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return tile != null && super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return tile == null ? null : super.getCapability(capability, facing);
    }

    private static final String TYPE_TAG = "type";
    private static final String DATA_TAG = "data";

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        if(tile == null) {
            destroy();
            return nbt;
        }
        for(EnumFacing side : EnumFacing.values()) {
            NBTTagList sideNbt = new NBTTagList();
            for(ICover cover : covers.get(side)) {
                ICoverType type = cover.getType();
                NBTTagCompound coverNbt = new NBTTagCompound();
                coverNbt.setString(TYPE_TAG, Preconditions.checkNotNull(type.getRegistryName(),
                        "Attempted to serialize unregistered cover type: %s", type)
                        .toString());
                coverNbt.setTag(DATA_TAG, type.serialize(cover));
                sideNbt.appendTag(coverNbt);
            }
            nbt.setTag(side.getName(), sideNbt);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if(tile == null) {
            destroy();
            return;
        }
        covers.clear();
        for(String key : nbt.getKeySet()) {
            EnumFacing side = EnumFacing.byName(key);
            if(side == null) continue;
            NBTTagList tagList = nbt.getTagList(key, Constants.NBT.TAG_COMPOUND);
            for(NBTBase tag : tagList) {
                NBTTagCompound coverNbt = (NBTTagCompound) tag;
                ResourceLocation typeLoc = new ResourceLocation(coverNbt.getString(TYPE_TAG));
                ICoverType type = getApi().getRegistry().getValue(typeLoc);
                if(type == null) {
                    LOGGER.warn("Unknown cover type: {}.", typeLoc);
                    continue;
                }
                ICover cover = type.makeCover(tile, side, coverNbt.getCompoundTag(DATA_TAG));
                if(cover != null) covers.put(side, cover);
            }
        }
    }

    private void destroy() {
        MinecraftForge.EVENT_BUS.unregister(this);
        for(EnumFacing side : covers.keySet()) {
            for(ICover cover : covers.get(side)) {
                drop(side, cover);
            }
        }
        covers.clear();
        tile = null;
    }

    // ICoverHolder implementation

    @Override
    public void put(@Nonnull EnumFacing side, @Nonnull ICover cover) {
        covers.put(side, cover);
    }

    @Override
    public Collection<ICover> get(EnumFacing side) {
        return covers.get(side);
    }

    @Override
    public void drop(EnumFacing side, ICover cover) {
        Preconditions.checkNotNull(tile, "Cannot drop items with null tile!");
        Vec3d pos = new Vec3d(tile.getPos())
                .addVector(0.5, 0.5, 0.5)
                .addVector(
                        side.getFrontOffsetX() * 0.5,
                        side.getFrontOffsetY() * 0.5,
                        side.getFrontOffsetZ() * 0.5
                );
        World world = tile.getWorld();

        if(world.isRemote) return;
        for(ItemStack stack : cover.getDrops()) {
            world.spawnEntity(new EntityItem(world,
                    pos.x + world.rand.nextGaussian() * 0.1,
                    pos.y + world.rand.nextGaussian() * 0.1,
                    pos.z + world.rand.nextGaussian() * 0.1,
                    stack));
        }
    }

    // event handlers to call ICover methods

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            if(tile == null || tile.isInvalid()) {
                destroy();
                return;
            }
            for(ICover cover : covers.values()) {
                cover.tick();
            }
        }
    }

    private boolean noRender(ICoverRevealer revealer) {
        for(ICover cover : covers.values()) {
            if(revealer.shouldShowCover(cover)) return false;
        }
        return true;
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        if(tile == null) {
            destroy();
            return;
        }

        if(covers.values().isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player == null) return;

        ICoverRevealer revealer = CapHelper.getRevealer(mc.player);
        if(revealer == null || noRender(revealer)) return;

        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Entity entity = mc.getRenderViewEntity();
        if(entity == null) entity = mc.player;

        double tx = entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * event.getPartialTicks());
        double ty = entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * event.getPartialTicks());
        double tz = entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * event.getPartialTicks());

        GlStateManager.pushMatrix();
        GlStateManager.translate(-tx, -ty, -tz);
        GlStateManager.disableDepth();
        GlStateManager.enableLighting();
        GlStateManager.color(1, 1, 1, 1);
        for(ICover cover : covers.values()) {
            if(revealer.shouldShowCover(cover)) cover.render();
        }
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

}
