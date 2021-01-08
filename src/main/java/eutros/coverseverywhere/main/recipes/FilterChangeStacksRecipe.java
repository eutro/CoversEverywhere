package eutros.coverseverywhere.main.recipes;

import eutros.coverseverywhere.main.covers.FilterCover;
import eutros.coverseverywhere.main.covers.StackFilter;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import static eutros.coverseverywhere.common.Constants.prefix;

public class FilterChangeStacksRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    public FilterChangeStacksRecipe() {
        setRegistryName(prefix(FilterCover.NAME.getResourcePath() + "_add"));
    }

    private int findFilter(InventoryCrafting inv) {
        for (int i = 0; i < inv.getSizeInventory() - inv.getWidth(); i++) {
            if (inv.getStackInSlot(i).getItem() == Item.getItemFromBlock(Blocks.HOPPER) &&
                    inv.getStackInSlot(i + inv.getWidth()).getItem() == FilterCover.ITEM) return i + inv.getWidth();
        }
        return -1;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return findFilter(inv) != -1;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        int filterSlot = findFilter(inv);
        ItemStack result = inv.getStackInSlot(filterSlot).copy();
        result.setCount(1);
        StackFilter filter = StackFilter.from(result.getTagCompound());
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (i == filterSlot || i == filterSlot - inv.getWidth()) continue;
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) filter.xorStack(stack.copy());
        }
        result.setTagCompound(filter.serializeNBT());
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        int filterSlot = findFilter(inv);
        NonNullList<ItemStack> ret = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < ret.size(); i++) {
            if (i == filterSlot) continue;
            ret.set(i, inv.getStackInSlot(i).copy());
            inv.setInventorySlotContents(i, ItemStack.EMPTY);
        }
        return ret;
    }

    @Override
    public boolean canFit(int width, int height) {
        return height >= 2 && width * height > 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(FilterCover.ITEM);
    }
}
