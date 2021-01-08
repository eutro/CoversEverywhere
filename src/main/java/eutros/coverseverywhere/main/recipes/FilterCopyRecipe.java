package eutros.coverseverywhere.main.recipes;

import eutros.coverseverywhere.main.covers.FilterCover;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

import static eutros.coverseverywhere.common.Constants.prefix;

public class FilterCopyRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    public FilterCopyRecipe() {
        setRegistryName(prefix(FilterCover.NAME.getResourcePath() + "_copy"));
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World worldIn) {
        return countFilters(inv) > 1;
    }

    private int countFilters(InventoryCrafting inv) {
        int count = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (FilterCover.ITEM != stack.getItem()) return -1;
            count++;
        }
        return count;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        ItemStack stack = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) break;
        }
        stack = stack.copy();
        stack.setCount(countFilters(inv));
        return stack;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height > 1;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(FilterCover.ITEM, 2);
    }
}
