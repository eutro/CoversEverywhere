package eutros.coverseverywhere.compat.gregtech;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverType;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GregTechCover implements ICover {

    private final CoverBehavior behavior;
    private final TileEntity tile;
    private final EnumFacing side;

    public GregTechCover(CoverBehavior behavior, TileEntity tile, EnumFacing side) {
        this.behavior = behavior;
        this.tile = tile;
        this.side = side;
    }

    @Override
    public ICoverType getType() {
        return GregTechCoverType.INSTANCE;
    }

    @Override
    public void render() {
        ICoverable coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
        if(coverable == null) return;
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buff = tes.getBuffer();
        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buff);
        BlockPos pos = tile.getPos();
        Matrix4 coverTranslation = new Matrix4().translate(pos.getX(), pos.getY(), pos.getZ());
        renderState.lightMatrix.locate(coverable.getWorld(), coverable.getPos());
        double coverPlateThickness = coverable.getCoverPlateThickness();
        IVertexOperation[] coverPipeline = new IVertexOperation[] {renderState.lightMatrix};
        Cuboid6 plateBox = ICoverable.getCoverPlateBox(side, coverPlateThickness);
        for(BlockRenderLayer renderLayer : BlockRenderLayer.values()) {
            switch(renderLayer) {
                case SOLID:
                    GlStateManager.disableAlpha();
                    break;
                case CUTOUT_MIPPED:
                    GlStateManager.enableAlpha();
                    break;
                case CUTOUT:
                    break;
                case TRANSLUCENT:
                    GlStateManager.enableBlend();
                    break;
            }
            if(behavior.canRenderInLayer(renderLayer)) {
                behavior.renderCover(renderState, coverTranslation.copy(), coverPipeline, plateBox, renderLayer);
            }
        }
        GlStateManager.disableBlend();
        tes.draw();
    }

    @Override
    public List<ItemStack> getDrops() {
        return behavior.getDrops();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        behavior.writeToNBT(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        behavior.readFromNBT(nbt);
    }

    @Override
    public void tick() {
        if(behavior instanceof ITickable) ((ITickable) behavior).update();
    }

    @Override
    public boolean configure(EntityPlayer player, EnumHand hand, float hitX, float hitY, float hitZ) {
        Vec3d tileVec = new Vec3d(tile.getPos());
        CuboidRayTraceResult crt = new CuboidRayTraceResult(player,
                new Vector3(tileVec),
                new IndexedCuboid6(null, new AxisAlignedBB(tile.getPos())),
                player.getPositionVector().distanceTo(new Vec3d(hitX, hitY, hitZ).add(tileVec)));
        return behavior.onScrewdriverClick(player, hand, crt) == EnumActionResult.SUCCESS;
    }

    @Override
    public void onRemoved() {
        behavior.onRemoved();
    }

    public CoverBehavior getBehaviour() {
        return behavior;
    }

}
