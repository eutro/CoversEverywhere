package eutros.coverseverywhere.main.recipes;

import eutros.coverseverywhere.common.Initialize;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModRecipes {
    @Initialize
    public static void init() {
        MinecraftForge.EVENT_BUS.register(ModRecipes.class);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> evt) {
        IForgeRegistry<IRecipe> r = evt.getRegistry();
        r.register(new FilterCopyRecipe());
        int idx = 0;
        r.register(new FilterConfigureRecipe(idx++, filter -> filter.setWhitelist(!filter.isWhitelist())));
        r.register(new FilterConfigureRecipe(idx++, filter -> filter.setCompareItem(!filter.isCompareItem())));
        r.register(new FilterConfigureRecipe(idx++, filter -> filter.setCompareDamage(!filter.isCompareDamage())));
        r.register(new FilterConfigureRecipe(idx, filter -> filter.setCompareTag(!filter.isCompareTag())));
        r.register(new FilterChangeStacksRecipe());
    }
}
