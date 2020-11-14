package eutros.coverseverywhere.common.covers;

import com.google.common.base.Preconditions;
import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.*;
import eutros.coverseverywhere.common.util.NbtSerializableStorage;
import eutros.coverseverywhere.common.util.NoOpStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class CoversCapabilityProvider implements ICapabilityProvider, ICoverHolder {

    private static final ResourceLocation NAME = new ResourceLocation(CoversEverywhere.MOD_ID, "covers");

    private final Map<EnumFacing, Map<ICoverType, ICover>> covers = new EnumMap<>(EnumFacing.class);
    @Nullable
    private TileEntity tile;

    public CoversCapabilityProvider() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public CoversCapabilityProvider(TileEntity tile) {
        this();
        this.tile = tile;
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.register(CoversCapabilityProvider.class);
        CapabilityManager.INSTANCE.register(ICoverHolder.class,
                new NbtSerializableStorage<>(),
                CoversCapabilityProvider::new);
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
        return capability == getApi().getHolderCapability();
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == getApi().getHolderCapability() ? getApi().getHolderCapability().cast(this) :
               null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        if(tile == null) return nbt; // shouldn't serialize with a null tile
        for(Map.Entry<EnumFacing, Map<ICoverType, ICover>> e1 : covers.entrySet()) {
            NBTTagCompound sideNbt = new NBTTagCompound();
            for(Map.Entry<ICoverType, ICover> e2 : e1.getValue().entrySet()) {
                sideNbt.setTag(Objects.requireNonNull(e2.getKey().getRegistryName()).toString(),
                        e2.getValue().serializeNBT());
            }
            nbt.setTag(e1.getKey().getName(), sideNbt);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if(tile == null) {
            // cannot deserialize with null tile
            destroy();
            return;
        }
        covers.clear();
        for(String key : nbt.getKeySet()) {
            IdentityHashMap<ICoverType, ICover> side = new IdentityHashMap<>();
            covers.put(EnumFacing.byName(key), side);
            NBTTagCompound sideNbt = nbt.getCompoundTag(key);
            for(String id : sideNbt.getKeySet()) {
                ICoverType type = getApi()
                        .getRegistry()
                        .getValue(new ResourceLocation(sideNbt.getString(id)));
                if(type == null) continue;
                side.put(type, type.makeCover(tile, sideNbt.getCompoundTag(id)));
            }
        }
    }

    private void destroy() {
        MinecraftForge.EVENT_BUS.unregister(this);
        for(Map.Entry<EnumFacing, Map<ICoverType, ICover>> entry : covers.entrySet()) {
            for(ICover cover : entry.getValue().values()) {
                dropItems(entry.getKey(), cover);
            }
        }
        tile = null;
        covers.clear();
    }

    // ICoverHolder implementation

    @Override
    public void put(@Nonnull EnumFacing side, @Nonnull ICover cover) {
        Map<ICoverType, ICover> map;
        if(!covers.containsKey(side)) covers.put(side, map = new IdentityHashMap<>());
        else map = covers.get(side);

        ICoverType type = cover.getType();
        remove(side, type, true);
        map.put(type, cover);
    }

    @Nullable
    @Override
    public ICover get(EnumFacing side, ICoverType type) {
        return covers.containsKey(side) ? covers.get(side).get(type) : null;
    }

    @Nullable
    @Override
    public ICover remove(EnumFacing side, @Nullable ICoverType type, boolean drop) {
        ICover cover = null;
        if(covers.containsKey(side)) {
            if(type == null) {
                for(Map.Entry<ICoverType, ICover> entry : covers.get(side).entrySet()) {
                    cover = entry.getValue();
                    break;
                }
                if(cover != null) covers.get(side).remove(cover.getType());
            } else {
                cover = covers.get(side).remove(type);
            }
        }
        if(drop && cover != null) dropItems(side, cover);
        return cover;
    }

    private void dropItems(EnumFacing side, ICover cover) {
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
            for(Map<ICoverType, ICover> m : covers.values()) {
                for(ICover c : m.values()) {
                    c.tick();
                }
            }
        }
    }

    private boolean noRender(@Nullable ICoverRevealer revealer) {
        if(revealer == null) return true;
        for(Map<ICoverType, ICover> map : covers.values()) {
            for(ICover cover : map.values()) {
                if(revealer.shouldShowCover(cover)) return false;
            }
        }
        return true;
    }

    @Nullable
    private ICoverRevealer getRevealer(ItemStack stack) {
        if(stack.getItem() instanceof ICoverRevealer) return (ICoverRevealer) stack.getItem();
        else return stack.getCapability(getApi().getRevealerCapability(), null);
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        if(covers.values().isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player == null) return;

        ICoverRevealer mRevealer = getRevealer(mc.player.getHeldItem(EnumHand.MAIN_HAND));
        ICoverRevealer oRevealer = getRevealer(mc.player.getHeldItem(EnumHand.OFF_HAND));
        if(noRender(mRevealer) && noRender(oRevealer)) return;

        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Entity entity = mc.getRenderViewEntity();
        if(entity == null) entity = mc.player;

        double tx = entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * event.getPartialTicks());
        double ty = entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * event.getPartialTicks());
        double tz = entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * event.getPartialTicks());

        GlStateManager.pushMatrix();
        GlStateManager.translate(-tx, -ty, -tz);
        GlStateManager.disableDepth();
        for(Map<ICoverType, ICover> map : covers.values()) {
            for(ICover cover : map.values()) {
                if((mRevealer != null && mRevealer.shouldShowCover(cover)) ||
                        (oRevealer != null && oRevealer.shouldShowCover(cover))) cover.render();
            }
        }
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

}
