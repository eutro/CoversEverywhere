package eutros.coverseverywhere.impl;

import eutros.coverseverywhere.api.ICoverRevealer;
import eutros.coverseverywhere.common.Initialize;
import eutros.coverseverywhere.common.util.CapHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class GridRenderer {

    private static final AxisAlignedBB X_PLANE = new AxisAlignedBB(0.25, 0, 0, 0.75, 1, 1);
    private static final AxisAlignedBB Y_PLANE = new AxisAlignedBB(0, 0.25, 0, 1, 0.75, 1);
    private static final AxisAlignedBB Z_PLANE = new AxisAlignedBB(0, 0, 0.25, 1, 1, 0.75);

    @Initialize(sides = { Side.CLIENT })
    public static void init() {
        MinecraftForge.EVENT_BUS.register(GridRenderer.class);
    }

    /**
     * @see RenderGlobal#drawSelectionBox(EntityPlayer, RayTraceResult, int, float)
     */
    @SubscribeEvent
    public static void renderGrid(DrawBlockHighlightEvent evt) {
        EntityPlayer player = evt.getPlayer();

        ICoverRevealer revealer = CapHelper.getRevealer(player);
        if (revealer == null || !revealer.shouldShowGrid()) return;

        RayTraceResult movingObjectPositionIn = evt.getTarget();
        float partialTicks = evt.getPartialTicks();
        World world = evt.getPlayer().getEntityWorld();
        if (movingObjectPositionIn.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = movingObjectPositionIn.getBlockPos();
            TileEntity tile = world.getTileEntity(pos);
            if (tile == null || !tile.hasCapability(getApi().getHolderCapability(), null)) return;

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            IBlockState state = world.getBlockState(pos);

            if (state.getMaterial() != Material.AIR && world.getWorldBorder().contains(pos)) {
                double tx = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
                double ty = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
                double tz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
                AxisAlignedBB selected = state.getSelectedBoundingBox(world, pos);
                RenderGlobal.drawSelectionBoundingBox(selected.intersect(X_PLANE.offset(pos)).grow(0.002).offset(-tx, -ty, -tz), 0, 0, 0, 0.4F);
                RenderGlobal.drawSelectionBoundingBox(selected.intersect(Y_PLANE.offset(pos)).grow(0.002).offset(-tx, -ty, -tz), 0, 0, 0, 0.4F);
                RenderGlobal.drawSelectionBoundingBox(selected.intersect(Z_PLANE.offset(pos)).grow(0.002).offset(-tx, -ty, -tz), 0, 0, 0, 0.4F);
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }

}
