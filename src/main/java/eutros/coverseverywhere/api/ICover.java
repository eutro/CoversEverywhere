package eutros.coverseverywhere.api;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A cover instance that is to be on a tile entity.
 * <p>
 * Typically, one may wish to hold a reference to the side and tile entity that the cover is on.
 * <p>
 * {@link INBTSerializable} is implemented for convenience, but the associated {@link ICoverType}'s
 * {@link ICoverType#serialize(ICover)} has to call it.
 * <p>
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
     * Called when the cover is removed.
     *
     * Covers are responsible for their own drops.
     */
    void onRemoved();

    /**
     * Called to render the cover.
     * You MUST use the provided vertex builder.
     * THE BUFFER IS ALREADY DRAWING!
     * The format is {@link DefaultVertexFormats#BLOCK}.
     *
     * @param bufferBuilder The builder to put vertices into.
     */
    @SideOnly(Side.CLIENT)
    void render(BufferBuilder bufferBuilder);

    /**
     * Configure this cover, called when the cover is activated with a screwdriver.
     * <p>
     * This might open a GUI, or just change the state immediately.
     *
     * @return Whether configuration had any effect.
     */
    default boolean configure(EntityPlayer player, EnumHand hand, float hitX, float hitY, float hitZ) {
        return false;
    }

}
