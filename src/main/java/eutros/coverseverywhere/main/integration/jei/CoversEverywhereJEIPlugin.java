package eutros.coverseverywhere.main.integration.jei;

import com.google.common.collect.ImmutableList;
import eutros.coverseverywhere.main.covers.ConveyorCover;
import eutros.coverseverywhere.main.covers.FilterCover;
import eutros.coverseverywhere.main.covers.StackFilter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import mezz.jei.api.recipe.wrapper.IShapedCraftingRecipeWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@JEIPlugin
public class CoversEverywhereJEIPlugin implements IModPlugin {
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.useNbtForSubtypes(ConveyorCover.ITEM);
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipes(ImmutableList.<ICraftingRecipeWrapper>of(
                ingredients -> {
                    ItemStack filterStack = new ItemStack(FilterCover.ITEM);
                    StackFilter filter = new StackFilter();
                    filter.setCompareTag(false);
                    filter.xorStack(new ItemStack(Items.DIAMOND));
                    filterStack.setTagCompound(filter.serializeNBT());
                    ingredients.setInputs(VanillaTypes.ITEM, ImmutableList.of(filterStack, new ItemStack(FilterCover.ITEM)));
                    ItemStack outStack = filterStack.copy();
                    outStack.setCount(2);
                    ingredients.setOutput(VanillaTypes.ITEM, outStack);
                },
                new IShapedCraftingRecipeWrapper() {
                    @Override
                    public int getWidth() {
                        return 2;
                    }

                    @Override
                    public int getHeight() {
                        return 2;
                    }

                    @Override
                    public void getIngredients(IIngredients ingredients) {
                        ingredients.setInputs(VanillaTypes.ITEM, ImmutableList.of(
                                new ItemStack(Blocks.HOPPER),
                                ItemStack.EMPTY,
                                new ItemStack(FilterCover.ITEM),
                                new ItemStack(Items.DIAMOND)
                        ));
                        ItemStack outStack = new ItemStack(FilterCover.ITEM);
                        StackFilter filter = new StackFilter();
                        filter.xorStack(new ItemStack(Items.DIAMOND));
                        outStack.setTagCompound(filter.serializeNBT());
                        ingredients.setOutput(VanillaTypes.ITEM, outStack);
                    }
                }),
                VanillaRecipeCategoryUid.CRAFTING);

        List<Consumer<StackFilter>> consumers = ImmutableList.of(
                filter -> filter.setWhitelist(!filter.isWhitelist()),
                filter -> filter.setCompareItem(!filter.isCompareItem()),
                filter -> filter.setCompareDamage(!filter.isCompareDamage()),
                filter -> filter.setCompareTag(!filter.isCompareTag()));
        registry.addRecipes(IntStream.range(0, consumers.size())
                        .<ICraftingRecipeWrapper>mapToObj(idx -> {
                            Consumer<StackFilter> consumer = consumers.get(idx);
                            return ingredients -> {
                                NonNullList<ItemStack> inputs = NonNullList.withSize(9, ItemStack.EMPTY);
                                inputs.set(idx, new ItemStack(FilterCover.ITEM));
                                ingredients.setInputs(VanillaTypes.ITEM, inputs);
                                ItemStack output = new ItemStack(FilterCover.ITEM);
                                StackFilter filter = new StackFilter();
                                consumer.accept(filter);
                                output.setTagCompound(filter.serializeNBT());
                                ingredients.setOutput(VanillaTypes.ITEM, output);
                            };
                        })
                        .collect(Collectors.toList()),
                VanillaRecipeCategoryUid.CRAFTING);
    }
}
