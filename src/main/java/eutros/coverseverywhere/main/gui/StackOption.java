package eutros.coverseverywhere.main.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.List;

import static net.minecraft.client.util.ITooltipFlag.TooltipFlags.ADVANCED;
import static net.minecraft.client.util.ITooltipFlag.TooltipFlags.NORMAL;

public class StackOption implements Option {

    private static final int ITEM_SIZE = 16;

    private final ItemStack stack;
    private final Minecraft mc = Minecraft.getMinecraft();

    public StackOption(ItemStack stack) {
        this.stack = stack;
    }

    protected StackOption() {
        this(ItemStack.EMPTY);
    }

    protected ItemStack getStack() {
        return stack;
    }

    @Override
    public List<String> getTooltip() {
        return stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ADVANCED : NORMAL);
    }

    @Override
    public void renderAt(int x, int y) {
        mc.getRenderItem().renderItemIntoGUI(getStack(),
                x - ITEM_SIZE / 2,
                y - ITEM_SIZE / 2);
    }
}
