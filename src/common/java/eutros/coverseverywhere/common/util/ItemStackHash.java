package eutros.coverseverywhere.common.util;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;

public interface ItemStackHash extends Hash.Strategy<ItemStack> {
    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private boolean item, count, damage, tag;

        public Builder compareItem(boolean v) {
            item = v;
            return this;
        }

        public Builder compareCount(boolean v) {
            count = v;
            return this;
        }

        public Builder compareDamage(boolean v) {
            damage = v;
            return this;
        }

        public Builder compareTag(boolean v) {
            tag = v;
            return this;
        }

        public ItemStackHash build() {
            return new ItemStackHash() {
                @Override
                public int hashCode(@Nullable ItemStack o) {
                    return o == null || o.isEmpty() ? 0 : Objects.hash(
                            item ? o.getItem() : null,
                            count ? o.getCount() : null,
                            damage ? o.getItemDamage() : null,
                            tag ? o.getTagCompound() : null
                    );
                }

                @Override
                public boolean equals(@Nullable ItemStack a, @Nullable ItemStack b) {
                    if (a == null || a.isEmpty()) return b == null || b.isEmpty();
                    if (b == null || b.isEmpty()) return false;

                    return (!item || a.getItem() == b.getItem()) &&
                            (!count || a.getCount() == b.getCount()) &&
                            (!damage || a.getItemDamage() == b.getItemDamage()) &&
                            (!tag || Objects.equals(a.getTagCompound(), b.getTagCompound()));
                }
            };
        }
    }
}
