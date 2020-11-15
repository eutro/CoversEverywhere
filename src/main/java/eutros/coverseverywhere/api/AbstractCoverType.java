package eutros.coverseverywhere.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * An abstract class implementing {@link ICoverType} by extending {@link IForgeRegistryEntry.Impl} for
 * its implementation of {@link IForgeRegistryEntry}.
 */
public abstract class AbstractCoverType
        extends IForgeRegistryEntry.Impl<ICoverType>
        implements ICoverType {

    /**
     * Convenience constructor that sets the registry name when constructed.
     *
     * @param id The registry name of the cover type.
     */
    protected AbstractCoverType(ResourceLocation id) {
        setRegistryName(id);
    }

    /**
     * Convenience constructor that sets the registry name when constructed.
     *
     * @param namespace The namespace for the registry name of the cover type.
     * @param path The path for the registry name of the cover type.
     */
    protected AbstractCoverType(String namespace, String path) {
        setRegistryName(namespace, path);
    }

    /**
     * A constructor that does not set the registry name when constructed.
     */
    protected AbstractCoverType() {
    }

}
