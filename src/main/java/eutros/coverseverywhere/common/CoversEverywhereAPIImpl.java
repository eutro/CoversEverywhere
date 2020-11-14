package eutros.coverseverywhere.common;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.CoversEverywhereAPI;
import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.common.covers.Covers;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CoversEverywhereAPIImpl implements CoversEverywhereAPI {

    public static void init() {
    }

    @Override
    public IForgeRegistry<ICoverType> getRegistry() {
        return Covers.REGISTRY;
    }

}
