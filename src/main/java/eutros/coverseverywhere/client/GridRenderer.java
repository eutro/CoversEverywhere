package eutros.coverseverywhere.client;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.ICoverRevealer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

@Mod.EventBusSubscriber(modid = CoversEverywhere.MOD_ID, value = Side.CLIENT)
public class GridRenderer {

    private static AxisAlignedBB X_PLANE = new AxisAlignedBB(0.25, 0, 0, 0.75, 1, 1);
    private static AxisAlignedBB Y_PLANE = new AxisAlignedBB(0, 0.25, 0, 1, 0.75, 1);
    private static AxisAlignedBB Z_PLANE = new AxisAlignedBB(0, 0, 0.25, 1, 1, 0.75);

    private static boolean noReveal(ItemStack stack) {
        ICoverRevealer revealer;
        if(stack.getItem() instanceof ICoverRevealer) revealer = (ICoverRevealer) stack.getItem();
        else revealer = stack.getCapability(getApi().getRevealerCapability(), null);
        if(revealer == null) return true;
        return !revealer.shouldShowGrid();
    }

    /**
     * @see RenderGlobal#drawSelectionBox(EntityPlayer, RayTraceResult, int, float)
     */
    @SubscribeEvent
    public static void renderGrid(DrawBlockHighlightEvent evt) {
        EntityPlayer player = evt.getPlayer();

        if(noReveal(player.getHeldItem(EnumHand.MAIN_HAND)) &&
                noReveal(player.getHeldItem(EnumHand.OFF_HAND))) return;

        RayTraceResult movingObjectPositionIn = evt.getTarget();
        float partialTicks = evt.getPartialTicks();
        World world = evt.getPlayer().getEntityWorld();
        if(movingObjectPositionIn.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = movingObjectPositionIn.getBlockPos();
            TileEntity tile = world.getTileEntity(pos);
            if(tile == null || !tile.hasCapability(getApi().getHolderCapability(), null)) return;

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            IBlockState state = world.getBlockState(pos);

            if(state.getMaterial() != Material.AIR && world.getWorldBorder().contains(pos)) {
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
