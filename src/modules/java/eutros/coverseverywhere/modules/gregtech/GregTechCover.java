package eutros.coverseverywhere.modules.gregtech;

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
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.ICoverable;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

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

    /**
     * @see MetaTileEntity#renderCovers(CCRenderState, Matrix4, BlockRenderLayer)
     */
    @Override
    public void render(BufferBuilder buff) {
        ICoverable coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
        if (coverable == null) return;
        CCRenderState renderState = CCRenderState.instance();
        renderState.reset();
        renderState.bind(buff);
        BlockPos pos = tile.getPos();
        Matrix4 coverTranslation = new Matrix4().translate(pos.getX(), pos.getY(), pos.getZ());
        renderState.lightMatrix.locate(coverable.getWorld(), coverable.getPos());
        double coverPlateThickness = coverable.getCoverPlateThickness();
        IVertexOperation[] coverPipeline = { renderState.lightMatrix };
        Cuboid6 plateBox = ICoverable.getCoverPlateBox(side, coverPlateThickness)
                .expand(side.getAxis() == EnumFacing.Axis.X ? 0.01 : 0,
                        side.getAxis() == EnumFacing.Axis.Y ? 0.01 : 0,
                        side.getAxis() == EnumFacing.Axis.Z ? 0.01 : 0);

        behavior.renderCover(renderState, coverTranslation.copy(), coverPipeline, plateBox, BlockRenderLayer.CUTOUT);
    }

    private static final String TYPE_KEY = "type";
    private static final String DATA_KEY = "data";

    public NBTTagCompound serialize() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(TYPE_KEY, behavior
                .getCoverDefinition()
                .getCoverId()
                .toString());
        NBTTagCompound data = new NBTTagCompound();
        behavior.writeToNBT(data);
        nbt.setTag(DATA_KEY, data);
        return nbt;
    }

    @Nullable
    static GregTechCover deserialize(TileEntity tile, EnumFacing side, NBTTagCompound nbt) {
        CoverDefinition definition = CoverDefinition.getCoverById(new ResourceLocation(nbt.getString(TYPE_KEY)));
        if (definition == null) return null;
        CoverBehavior behavior = definition.createCoverBehavior(new TileWrapper(tile), side);
        behavior.readFromNBT(nbt.getCompoundTag(DATA_KEY));
        return new GregTechCover(behavior, tile, side);
    }

    @Override
    public void tick() {
        if (behavior instanceof ITickable) ((ITickable) behavior).update();
    }

    @Override
    public void configure(EntityPlayer player, EnumHand hand, float hitX, float hitY, float hitZ) {
        Vec3d tileVec = new Vec3d(tile.getPos());
        CuboidRayTraceResult crt = new CuboidRayTraceResult(player,
                new Vector3(tileVec),
                new IndexedCuboid6(null, new AxisAlignedBB(tile.getPos())),
                player.getPositionVector().distanceTo(new Vec3d(hitX, hitY, hitZ).add(tileVec)));
        behavior.onScrewdriverClick(player, hand, crt);
    }

    @Override
    public void onRemoved() {
        behavior.onRemoved();
        for (ItemStack stack : behavior.getDrops()) {
            Block.spawnAsEntity(tile.getWorld(), tile.getPos(), stack);
        }
    }

    @Nullable
    @Override
    public <T> T wrapCapability(@Nullable T toWrap, Capability<T> capability) {
        return behavior.getCapability(capability, toWrap);
    }

    @Override
    public <T> boolean wrapHasCapability(boolean hadBefore, Capability<T> capability) {
        if (hadBefore) {
            // I sure do hope this works
            return wrapCapability(capability.getDefaultInstance(), capability) != null;
        }
        return false;
    }

    @Override
    public ItemStack getRepresentation() {
        return behavior.getPickItem();
    }

    public CoverBehavior getBehaviour() {
        return behavior;
    }

}
