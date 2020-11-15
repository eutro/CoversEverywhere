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

import java.util.Set;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class ScrewdriverItem extends Item implements ICoverRevealer {

    public static ResourceLocation NAME = new ResourceLocation(CoversEverywhere.MOD_ID, "screwdriver");

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
        Set<ICoverType> types = holder.getTypes(side);
        if(types.isEmpty()) return EnumActionResult.PASS;

        // FIXME this should be deterministic between runs and, you know, less bad
        ICoverType type = types.iterator().next();
        ICover cover = holder.get(side, type);
        if(cover == null) return EnumActionResult.PASS;

        return cover.configure(player) ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
    }

}
