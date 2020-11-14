package eutros.coverseverywhere.api;

import com.google.common.base.Suppliers;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Supplier;

public interface CoversEverywhereAPI {

    Supplier<CoversEverywhereAPI> INSTANCE = Suppliers.memoize(() -> {
        try {
            return (CoversEverywhereAPI) Class.forName("eutros.coverseverywhere.common.CoversEverywhereAPIImpl").newInstance();
        } catch(ReflectiveOperationException e) {
            // fail fast
            throw new RuntimeException("Couldn't find CoversEverywhereAPIImpl.", e);
        }
    });

    static CoversEverywhereAPI getInstance() {
        return INSTANCE.get();
    }

    IForgeRegistry<ICoverType> getRegistry();

}
