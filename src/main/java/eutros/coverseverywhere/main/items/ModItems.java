package eutros.coverseverywhere.main.items;

import eutros.coverseverywhere.common.Constants;
import eutros.coverseverywhere.common.Initialize;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems {

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(Constants.MOD_ID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(CROWBAR);
        }
    };

    public static CrowbarItem CROWBAR;
    public static ScrewdriverItem SCREWDRIVER;

    @Initialize
    public static void init() {
        MinecraftForge.EVENT_BUS.register(ModItems.class);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> evt) {
        IForgeRegistry<Item> r = evt.getRegistry();

        r.register(CROWBAR = new CrowbarItem());
        r.register(SCREWDRIVER = new ScrewdriverItem());
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent evt) {
        registerModel(CROWBAR, CrowbarItem.NAME);
        registerModel(SCREWDRIVER, ScrewdriverItem.NAME);
    }

    public static void registerModel(Item item, ResourceLocation name) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(name, "inventory"));
    }

}
