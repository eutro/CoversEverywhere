package eutros.coverseverywhere.common.util;

import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverRevealer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static eutros.coverseverywhere.api.CoversEverywhereAPI.getApi;

public class CapHelper {

    @Nullable
    public static ICoverRevealer getRevealer(ItemStack stack) {
        return stack.getItem() instanceof ICoverRevealer ?
               (ICoverRevealer) stack.getItem() :
               stack.getCapability(getApi().getRevealerCapability(), null);
    }

    @Nullable
    public static ICoverRevealer getRevealer(EntityPlayer player) {
        return combineOr(getRevealer(player.getHeldItem(EnumHand.MAIN_HAND)),
                getRevealer(player.getHeldItem(EnumHand.OFF_HAND)));
    }

    @Nullable
    public static ICoverRevealer combineOr(@Nullable ICoverRevealer a, @Nullable ICoverRevealer b) {
        if (a == null) return b;
        if (b == null) return a;
        return new ICoverRevealer() {
            @Override
            public boolean shouldShowCover(@Nonnull ICover cover) {
                return a.shouldShowCover(cover) || b.shouldShowCover(cover);
            }

            @Override
            public boolean shouldShowGrid() {
                return a.shouldShowGrid() || b.shouldShowGrid();
            }
        };
    }

}
