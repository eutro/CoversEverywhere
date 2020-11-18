package eutros.coverseverywhere.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Interface for items that should reveal the selection grid or covers.
 *
 * If the {@link ItemStack#getItem()} implements this interface, then the item will be used,
 * otherwise it may be obtained from {@link ItemStack#getCapability(Capability, EnumFacing)}.
 */
public interface ICoverRevealer {

    default boolean shouldShowGrid() {
        return true;
    }

}
