package eutros.coverseverywhere.common.covers;

import eutros.coverseverywhere.CoversEverywhere;
import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.common.covers.types.ConveyorCover;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

public class Covers {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(Covers.class);
    }

    public static IForgeRegistry<ICoverType> REGISTRY;

    @SubscribeEvent
    public static void registerRegistries(RegistryEvent.NewRegistry evt) {
        REGISTRY = new RegistryBuilder<ICoverType>()
                .setName(new ResourceLocation(CoversEverywhere.MOD_ID, "covers"))
                .setType(ICoverType.class)
                .create();
    }

    @SubscribeEvent
    public static void registerCovers(RegistryEvent.Register<ICoverType> evt) {
        IForgeRegistry<ICoverType> r = evt.getRegistry();

        r.register(ConveyorCover.TYPE);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> evt) {
        IForgeRegistry<Item> r = evt.getRegistry();

        r.register(ConveyorCover.ITEM);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent evt) {
        registerModel(ConveyorCover.ITEM, ConveyorCover.NAME);
    }

    private static void registerModel(Item item, ResourceLocation name) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(name, "inventory"));
    }

}
