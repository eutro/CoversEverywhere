package eutros.coverseverywhere.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nullable;

public abstract class CoverItem extends Item implements ICoverRevealer {

    @CapabilityInject(ICoverHolder.class)
    private static Capability<ICoverHolder> CAPABILITY = null;

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile == null) return EnumActionResult.PASS;

        ICoverHolder cap = tile.getCapability(CAPABILITY, null);
        if(cap == null) return EnumActionResult.PASS;

        EnumFacing side = GridSection.fromXYZ(facing, hitX, hitY, hitZ).offset(facing);
        ICover cover = makeCover(player, worldIn, pos, hand, side);
        if(cover == null) return EnumActionResult.PASS;

        cap.put(side, cover);
        if(!player.isCreative()) player.getHeldItem(hand).shrink(1);
        return EnumActionResult.SUCCESS;
    }

    @Nullable
    protected abstract ICover makeCover(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing);

}
