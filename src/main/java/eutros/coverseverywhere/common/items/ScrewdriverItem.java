package eutros.coverseverywhere.common.items;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class ScrewdriverItem extends Item implements ICoverRevealer {

    public static final ResourceLocation NAME = new ResourceLocation(CoversEverywhere.MOD_ID, "screwdriver");

    public ScrewdriverItem() {
        setRegistryName(NAME);
        setUnlocalizedName(NAME.getResourceDomain() + "." + NAME.getResourcePath());
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile == null) return EnumActionResult.PASS;

        ICoverHolder holder = tile.getCapability(getApi().getHolderCapability(), null);
        if(holder == null) return EnumActionResult.PASS;

        EnumFacing side = GridSection.fromXYZ(facing, hitX, hitY, hitZ).offset(facing);
        for(ICover cover : holder.get(side)) {
            if(cover.configure(player, hand, hitX, hitY, hitZ)) return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

}
