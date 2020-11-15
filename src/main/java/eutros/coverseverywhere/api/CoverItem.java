package eutros.coverseverywhere.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * An item that can be used as a cover, showing the grid and any placed covers.
 */
public abstract class CoverItem extends Item implements ICoverRevealer {

    /**
     * Called when a Block is right-clicked with this Item.
     *
     * Places the cover based on the selected {@link GridSection}.
     * The stack then is then partially consumed as by {@link #consumeOne(EntityPlayer, EnumHand)}.
     */
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile == null) return EnumActionResult.PASS;

        ICoverHolder cap = tile.getCapability(CoversEverywhereAPI.getApi().getHolderCapability(), null);
        if(cap == null) return EnumActionResult.PASS;

        EnumFacing side = GridSection.fromXYZ(facing, hitX, hitY, hitZ).offset(facing);
        ICover cover = makeCover(tile, player, hand, side);
        if(cover == null) return EnumActionResult.PASS;

        cap.put(side, cover);
        consumeOne(player, hand);
        return EnumActionResult.SUCCESS;
    }

    /**
     * Called when the cover has been placed to consume one of the cover item,
     * such as by reducing the stack size (the default).
     *
     * @param player The player placing the cover.
     * @param hand The hand the player is placing the cover with.
     */
    protected void consumeOne(EntityPlayer player, EnumHand hand) {
        if(!player.isCreative()) player.getHeldItem(hand).shrink(1);
    }

    /**
     * Create a cover from the given player for the given tile entity.
     *
     * @param tile The tile entity that the cover will be placed on.
     * @param player The player placing the cover.
     * @param hand The hand the player is using to place the cover.
     * @param side The side of the tile entity that the cover is to be placed on.
     * @return The created cover, or null if no cover should be placed.
     */
    @Nullable
    protected abstract ICover makeCover(TileEntity tile, EntityPlayer player, EnumHand hand, EnumFacing side);

}
