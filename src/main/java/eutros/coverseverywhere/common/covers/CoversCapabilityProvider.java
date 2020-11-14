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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
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
import java.util.Objects;

public class CoversCapabilityProvider implements ICapabilityProvider, ICoverHolder {

    // soft dependencies shouldn't use this, they can @CapabilityInject themselves.
    @CapabilityInject(ICoverHolder.class)
    public static Capability<ICoverHolder> COVER_HOLDER_CAPABILITY = null;

    private static final ResourceLocation NAME = new ResourceLocation(CoversEverywhere.MOD_ID, "covers");
    // soft dependencies shouldn't use this, they can @CapabilityInject themselves.
    @CapabilityInject(ICover.class)
    public static Capability<ICover> COVERS_CAPABILITY = null;

    private final EnumMap<EnumFacing, ICover> covers = new EnumMap<>(EnumFacing.class);
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
        return (capability == COVER_HOLDER_CAPABILITY)
                || (capability == COVERS_CAPABILITY && covers.containsKey(facing));
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == COVER_HOLDER_CAPABILITY ? COVER_HOLDER_CAPABILITY.cast(this) :
               capability == COVERS_CAPABILITY ? COVERS_CAPABILITY.cast(covers.get(facing)) :
               null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        covers.forEach((enumFacing, cover) -> {
            NBTTagCompound coverNbt = new NBTTagCompound();
            coverNbt.setString("id", Objects.requireNonNull(cover.getType().getRegistryName()).toString());
            coverNbt.setTag("data", cover.serializeNBT());
            nbt.setTag(enumFacing.getName(), coverNbt);
        });
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        covers.clear();
        for(String key : nbt.getKeySet()) {
            NBTTagCompound coverNbt = nbt.getCompoundTag(key);
            ICoverType type = CoversEverywhereAPI.getInstance()
                    .getRegistry()
                    .getValue(new ResourceLocation(coverNbt.getString("id")));
            if(type == null) continue;
            covers.put(EnumFacing.byName(key), type.makeCover(coverNbt.getCompoundTag("data")));
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent event) {
        if (tile == null || tile.isInvalid()) {
            unregister();
            return;
        }
        if(event.phase == TickEvent.Phase.END) {
            covers.values().forEach(c -> c.tick(tile));
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
        covers.values().forEach(c -> c.render(buff, tile.getPos()));
        tes.draw();

        GlStateManager.popMatrix();
    }

    private void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
        covers.clear();
    }

    @Override
    public void put(@Nonnull EnumFacing side, @Nonnull ICover cover) {
        covers.put(side, cover);
    }

}
