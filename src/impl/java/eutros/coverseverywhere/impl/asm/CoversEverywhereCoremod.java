package eutros.coverseverywhere.impl.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("eutros.coverseverywhere.impl.asm")
@IFMLLoadingPlugin.Name("Covers Everywhere Coremod")
public class CoversEverywhereCoremod implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{ CoversEverywhereClassTransformer.class.getName() };
    }

    @Nullable
    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Nullable
    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
