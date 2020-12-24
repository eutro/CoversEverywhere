package eutros.coverseverywhere.api;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * A cover instance that is to be on a tile entity.
 * <p>
 * Typically, one may wish to hold a reference to the side and tile entity that the cover is on.
 * <p>
 * Has to be serialized by its {@link #getType() type}'s {@link ICoverType#getSerializer() serializer}.
 * <p>
 * Can be attached to and obtained from tiles by {@link ICoverHolder}.
 */
public interface ICover {

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
     * <p>
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
     */
    default void configure(EntityPlayer player, EnumHand hand, float hitX, float hitY, float hitZ) {
    }

    /**
     * Override a capability that the tile entity may have.
     * <p>
     * If you are modifying availability (e.g. returning null when toWrap is non-null) in any way you MUST override
     * hasCapability too.
     *
     * @param toWrap     The capability instance to wrap. Null if the tile entity does not have the given capability.
     * @param capability The capability object that is being looked for.
     * @param <T>        The type of the capability.
     * @return The wrapped capability, or {@param toWrap} if no wrapping was done.
     */
    @Nullable
    default <T> T wrapCapability(@Nullable T toWrap, Capability<T> capability) {
        return toWrap;
    }

    /**
     * Override the presence of a capability that the tile entity may have.
     * <p>
     * This is necessary to keep with the contract of {@link ICapabilityProvider}.
     *
     * @param hadBefore  Whether the capability is already present.
     * @param capability The capability object.
     * @return Whether the capability object will be present after {@link #wrapCapability(Object, Capability)} is called.
     */
    default <T> boolean wrapHasCapability(boolean hadBefore, Capability<T> capability) {
        return hadBefore;
    }

    /**
     * @return The ItemStack representation of this cover, used in GUIs and such.
     */
    ItemStack getRepresentation();

}
