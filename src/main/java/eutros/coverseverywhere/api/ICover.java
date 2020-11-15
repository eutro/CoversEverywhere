package eutros.coverseverywhere.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * A cover instance that is to be on a tile entity.
 *
 * Typically, one may wish to hold a reference to the side and tile entity that the cover is on.
 * Any extra data should be serialized by implementing {@link INBTSerializable}.
 *
 * Can be attached to and obtained from tiles by {@link ICoverHolder}.
 */
public interface ICover extends INBTSerializable<NBTTagCompound> {

    /**
     * Get the {@link ICoverType} of this cover. This will be used for serialization,
     * and thus it should be registered in the cover type registry {@link CoversEverywhereAPI#getRegistry()}
     * to work properly.
     *
     * @return The {@link ICoverType} for this cover.
     */
    ICoverType getType();

    /**
     * Called once at the end of each tick of the tile entity.
     */
    default void tick() {
    }

    /**
     * Called each frame to render the cover at its position.
     */
    @SideOnly(Side.CLIENT)
    void render();

    /**
     * @return The list of stacks to drop when the cover is removed.
     */
    List<ItemStack> getDrops();

    /**
     * Configure this cover, called when the cover is activated with a screwdriver.
     *
     * This might open a GUI, or just change the state immediately.
     *
     * @return Whether configuration had any effect.
     */
    default boolean configure(EntityPlayer player, EnumHand hand, float hitX, float hitY, float hitZ) {
        return false;
    }

}
