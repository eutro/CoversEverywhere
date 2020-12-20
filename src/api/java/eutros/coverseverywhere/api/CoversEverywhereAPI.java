package eutros.coverseverywhere.api;

import com.google.common.base.Suppliers;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Supplier;

/**
 * An entry point for the API of the Covers Everywhere mod.
 *
 * The instance can be obtained with {@link #getApi()}.
 */
public interface CoversEverywhereAPI {

    Supplier<CoversEverywhereAPI> INSTANCE = Suppliers.memoize(() -> {
        try {
            return (CoversEverywhereAPI) Class.forName("eutros.coverseverywhere.impl.CoversEverywhereAPIImpl").newInstance();
        } catch(ReflectiveOperationException e) {
            // fail fast
            throw new RuntimeException("Couldn't find CoversEverywhereAPIImpl.", e);
        }
    });

    /**
     * @return The implementation of the {@link CoversEverywhereAPI}.
     */
    static CoversEverywhereAPI getApi() {
        return INSTANCE.get();
    }

    /**
     * @return The forge registry of cover types. This is what all {@link ICoverType}s should be registered in.
     */
    IForgeRegistry<ICoverType> getRegistry();

    /**
     * Get the {@link Capability} for {@link ICoverHolder}s.
     *
     * This is for convenience for hard dependencies, soft dependencies can use
     * {@link CapabilityInject} to obtain this instance.
     *
     * @return The {@link Capability} for {@link ICoverHolder}s.
     */
    Capability<ICoverHolder> getHolderCapability();

    /**
     * Get the {@link Capability} for {@link ICoverRevealer}s.
     *
     * This is for convenience for hard dependencies, soft dependencies can use
     * {@link CapabilityInject} to obtain this instance.
     *
     * Note that not all {@link ItemStack}s representing {@link ICoverRevealer}s will
     * have this capability. Any stacks whose {@link ItemStack#getItem()} implements
     * {@link ICoverRevealer} also count.
     *
     * @return The {@link Capability} for {@link ICoverRevealer}s.
     */
    Capability<ICoverRevealer> getRevealerCapability();

    /**
     * Synchronize the side of an {@link ICoverHolder} to nearby players.
     *
     * @param world The server world to synchronize from.
     * @param pos The position of the tile entity.
     * @param side The side of the tile entity to send.
     */
    void synchronize(WorldServer world, BlockPos pos, EnumFacing side);
}
