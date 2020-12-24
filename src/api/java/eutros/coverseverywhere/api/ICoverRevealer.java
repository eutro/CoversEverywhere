package eutros.coverseverywhere.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Interface for items that should reveal the selection grid.
 * <p>
 * If the {@link ItemStack#getItem()} implements this interface, then the item will be used,
 * otherwise it may be obtained from {@link ItemStack#getCapability(Capability, EnumFacing)}.
 *
 * @see GridSection
 */
public interface ICoverRevealer {

    /**
     * If the player is holding this item in either their offhand or their main hand, and this is true,
     * the selection grid will be shown.
     *
     * @return Whether the selection grid should be displayed on the faces of the highlighted block.
     */
    default boolean shouldShowGrid() {
        return true;
    }

}
