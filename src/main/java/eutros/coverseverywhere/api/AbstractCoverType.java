package eutros.coverseverywhere.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class AbstractCoverType<T extends ICover>
        extends IForgeRegistryEntry.Impl<ICoverType>
        implements ICoverType {

    protected AbstractCoverType(ResourceLocation id) {
        setRegistryName(id);
    }

    protected AbstractCoverType(String modid, String name) {
        setRegistryName(modid, name);
    }

}
