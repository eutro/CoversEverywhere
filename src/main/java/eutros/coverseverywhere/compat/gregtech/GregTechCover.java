package eutros.coverseverywhere.compat.gregtech;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Vector3;
import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.client.Textures;
import eutros.coverseverywhere.client.util.RenderHelper;
import gregtech.api.cover.CoverBehavior;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
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
        // TODO figure out how the ccl thingy works
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buff = tes.getBuffer();
        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        RenderHelper.side(buff, Textures.CONVEYOR_SPRITE, tile.getPos(), side);
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

    public CoverBehavior getBehaviour() {
        return behavior;
    }

}
