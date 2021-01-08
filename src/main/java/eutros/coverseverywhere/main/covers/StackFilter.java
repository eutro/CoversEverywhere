package eutros.coverseverywhere.main.covers;

import eutros.coverseverywhere.common.util.ItemStackHash;
import eutros.coverseverywhere.main.gui.RadialGuiScreen;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class StackFilter implements Predicate<ItemStack>, INBTSerializable<NBTTagCompound> {
    private Set<ItemStack> items = Collections.emptySet();

    private boolean whitelist = true;
    private boolean compareItem = true;
    private boolean compareDamage = true;
    private boolean compareTag = true;

    public void xorStack(ItemStack stack) {
        if (!items.add(stack)) items.remove(stack);
    }

    public void prompt(Consumer<ItemStack> stackConsumer) {
        ArrayList<ItemStack> list = new ArrayList<>(items);
        RadialGuiScreen.prompt(list, i -> stackConsumer.accept(list.get(i)));
    }

    {
        createSet();
    }

    public static StackFilter from(@Nullable NBTTagCompound nbt) {
        StackFilter filter = new StackFilter();
        filter.deserializeNBT(nbt);
        return filter;
    }

    private void createSet() {
        items = new ObjectLinkedOpenCustomHashSet<>(items,
                ItemStackHash.builder()
                        .compareItem(isCompareItem())
                        .compareDamage(isCompareDamage())
                        .compareTag(isCompareTag())
                        .build());
    }

    @Override
    public boolean test(ItemStack stack) {
        return isWhitelist() == items.contains(stack);
    }

    private static final String TAG_WHITELIST = "whitelist";

    private static final String TAG_COMPARE_ITEM = "compareItem";
    private static final String TAG_COMPARE_DAMAGE = "compareDamage";
    private static final String TAG_COMPARE_TAG = "compareTag";

    private static final String TAG_ITEMS = "items";

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(TAG_WHITELIST, isWhitelist());
        tag.setBoolean(TAG_COMPARE_ITEM, isCompareItem());
        tag.setBoolean(TAG_COMPARE_DAMAGE, isCompareDamage());
        tag.setBoolean(TAG_COMPARE_TAG, isCompareTag());
        NBTTagList stacks = new NBTTagList();
        for (ItemStack fStack : items) {
            stacks.appendTag(fStack.serializeNBT());
        }
        tag.setTag(TAG_ITEMS, stacks);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt == null) return;
        setWhitelist(nbt.getBoolean(TAG_WHITELIST));
        setCompareItem(nbt.getBoolean(TAG_COMPARE_ITEM));
        setCompareDamage(nbt.getBoolean(TAG_COMPARE_DAMAGE));
        setCompareTag(nbt.getBoolean(TAG_COMPARE_TAG));
        items.clear();
        createSet();
        NBTTagList stacks = nbt.getTagList(TAG_ITEMS, Constants.NBT.TAG_COMPOUND);
        for (NBTBase stack : stacks) {
            items.add(new ItemStack((NBTTagCompound) stack));
        }
    }

    public void addInformation(List<String> tooltip) {
        tooltip.add((isWhitelist() ? TextFormatting.WHITE : TextFormatting.DARK_GRAY) +
                I18n.format("item.covers_everywhere.filter.tooltip.list." + (isWhitelist() ? "white" : "black")));
        tooltip.add((isCompareItem() ? TextFormatting.GREEN : TextFormatting.RED) +
                I18n.format("item.covers_everywhere.filter.tooltip.compare.item." + isCompareItem()));
        tooltip.add((isCompareDamage() ? TextFormatting.GREEN : TextFormatting.RED) +
                I18n.format("item.covers_everywhere.filter.tooltip.compare.damage." + isCompareDamage()));
        tooltip.add((isCompareTag() ? TextFormatting.GREEN : TextFormatting.RED) +
                I18n.format("item.covers_everywhere.filter.tooltip.compare.tag." + isCompareTag()));

        if (!items.isEmpty()) {
            tooltip.add(I18n.format("item.covers_everywhere.filter.tooltip.stacks"));
            for (ItemStack stack : items) {
                tooltip.add(" - " + stack.getDisplayName());
            }
        }
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
        createSet();
    }

    public boolean isCompareItem() {
        return compareItem;
    }

    public void setCompareItem(boolean compareItem) {
        this.compareItem = compareItem;
        createSet();
    }

    public boolean isCompareDamage() {
        return compareDamage;
    }

    public void setCompareDamage(boolean compareDamage) {
        this.compareDamage = compareDamage;
        createSet();
    }

    public boolean isCompareTag() {
        return compareTag;
    }

    public void setCompareTag(boolean compareTag) {
        this.compareTag = compareTag;
        createSet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackFilter that = (StackFilter) o;
        return whitelist == that.whitelist &&
                compareItem == that.compareItem &&
                compareDamage == that.compareDamage &&
                compareTag == that.compareTag &&
                Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, whitelist, compareItem, compareDamage, compareTag);
    }

    @Override
    public String toString() {
        return "StackFilter{" +
                "items=" + items +
                ", whitelist=" + whitelist +
                ", compareItem=" + compareItem +
                ", compareDamage=" + compareDamage +
                ", compareTag=" + compareTag +
                '}';
    }
}
