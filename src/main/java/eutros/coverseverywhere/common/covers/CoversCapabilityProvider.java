package eutros.coverseverywhere.common.covers;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.*;
import eutros.coverseverywhere.common.util.NbtSerializableStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class CoversCapabilityProvider implements ICapabilityProvider, ICoverHolder {

    // soft dependencies shouldn't use this, they can @CapabilityInject themselves.
    @CapabilityInject(ICoverHolder.class)
    public static Capability<ICoverHolder> COVER_HOLDER_CAPABILITY = null;

    private static final ResourceLocation NAME = new ResourceLocation(CoversEverywhere.MOD_ID, "covers");

    private final Map<EnumFacing, Map<ICoverType, ICover>> covers = new EnumMap<>(EnumFacing.class);
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
        CapabilityManager.INSTANCE.register(ICover.class,
                new NbtSerializableStorage<>(),
                () -> null);
    }

    @SubscribeEvent
    public static void onTile(AttachCapabilitiesEvent<TileEntity> event) {
        event.addCapability(NAME, new CoversCapabilityProvider(event.getObject()));
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return (capability == COVER_HOLDER_CAPABILITY);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == COVER_HOLDER_CAPABILITY ? COVER_HOLDER_CAPABILITY.cast(this) :
               null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
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
        covers.clear();
        for(String key : nbt.getKeySet()) {
            IdentityHashMap<ICoverType, ICover> side = new IdentityHashMap<>();
            covers.put(EnumFacing.byName(key), side);
            NBTTagCompound sideNbt = nbt.getCompoundTag(key);
            for(String id : sideNbt.getKeySet()) {
                ICoverType type = CoversEverywhereAPI.getInstance()
                        .getRegistry()
                        .getValue(new ResourceLocation(sideNbt.getString(id)));
                if(type == null) continue;
                side.put(type, type.makeCover(tile, sideNbt.getCompoundTag(id)));
            }
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            if(tile == null || tile.isInvalid()) {
                onDestroy();
                return;
            }
            for(Map<ICoverType, ICover> m : covers.values()) {
                for(ICover c : m.values()) {
                    c.tick();
                }
            }
        }
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        if(covers.values().isEmpty()) return;
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.player == null) return;
        if(!(mc.player.getHeldItemMainhand().getItem() instanceof ICoverRevealer)) return;

        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        Entity entity = mc.getRenderViewEntity();
        if(entity == null) entity = mc.player;

        double tx = entity.lastTickPosX + ((entity.posX - entity.lastTickPosX) * event.getPartialTicks());
        double ty = entity.lastTickPosY + ((entity.posY - entity.lastTickPosY) * event.getPartialTicks());
        double tz = entity.lastTickPosZ + ((entity.posZ - entity.lastTickPosZ) * event.getPartialTicks());

        GlStateManager.pushMatrix();
        GlStateManager.translate(-tx, -ty, -tz);
        GlStateManager.disableDepth();

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buff = tes.getBuffer();
        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for(Map<ICoverType, ICover> m : covers.values()) {
            for(ICover c : m.values()) {
                c.render(buff);
            }
        }
        tes.draw();

        GlStateManager.popMatrix();
    }

    private void onDestroy() {
        MinecraftForge.EVENT_BUS.unregister(this);
        for(Map<ICoverType, ICover> map : covers.values()) {
            for(ICover cover : map.values()) {
                dropItems(cover);
            }
        }
        covers.clear();
    }

    @Override
    public void put(@Nonnull EnumFacing side, @Nonnull ICover cover) {
        Map<ICoverType, ICover> map;
        if(!covers.containsKey(side)) covers.put(side, map = new IdentityHashMap<>());
        else map = covers.get(side);

        ICoverType type = cover.getType();
        if(map.containsKey(type)) {
            dropItems(map.get(type));
            map.remove(type);
        }
        map.put(type, cover);
    }

    private void dropItems(ICover cover) {
        BlockPos pos = tile.getPos();
        World world = tile.getWorld();
        if(world.isRemote) return;

        for(ItemStack stack : cover.getDrops()) {
            world.spawnEntity(new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack));
        }
    }

}
