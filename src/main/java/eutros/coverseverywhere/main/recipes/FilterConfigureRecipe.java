package eutros.coverseverywhere.main.recipes;

import eutros.coverseverywhere.main.covers.FilterCover;
import eutros.coverseverywhere.main.covers.StackFilter;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.function.Consumer;

import static eutros.coverseverywhere.common.Constants.prefix;

public class FilterConfigureRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final int idx;
    private final Consumer<StackFilter> flip;

    public FilterConfigureRecipe(int idx, Consumer<StackFilter> flip) {
        this.idx = idx;
        this.flip = flip;
        setRegistryName(prefix(FilterCover.NAME.getResourcePath() + "_configure_" + idx));
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (i == idx) {
                if (stack.getItem() != FilterCover.ITEM) return false;
            } else {
                if (!stack.isEmpty()) return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack stack = inv.getStackInSlot(idx).copy();
        NBTTagCompound tag = stack.getTagCompound();
        StackFilter filter = StackFilter.from(tag);
        if (tag == null) stack.setTagCompound(tag = new NBTTagCompound());
        flip.accept(filter);
        tag.merge(filter.serializeNBT());
        return stack;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= idx;
    }

    @Override
    public ItemStack getRecipeOutput() {
        ItemStack stack = new ItemStack(FilterCover.ITEM);
        StackFilter filter = new StackFilter();
        flip.accept(filter);
        stack.setTagCompound(filter.serializeNBT());
        return stack;
    }
}
