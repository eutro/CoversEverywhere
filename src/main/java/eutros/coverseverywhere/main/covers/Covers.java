package eutros.coverseverywhere.main.covers;

import eutros.coverseverywhere.api.ICoverType;
import eutros.coverseverywhere.common.Initialize;
import eutros.coverseverywhere.main.items.ModItems;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class Covers {

    @Initialize
    public static void init() {
        MinecraftForge.EVENT_BUS.register(Covers.class);
    }

    @SubscribeEvent
    public static void registerCovers(RegistryEvent.Register<ICoverType> evt) {
        IForgeRegistry<ICoverType> r = evt.getRegistry();

        r.register(ConveyorCover.TYPE);
        r.register(FilterCover.TYPE);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> evt) {
        IForgeRegistry<Item> r = evt.getRegistry();

        r.register(ConveyorCover.ITEM);
        r.register(FilterCover.ITEM);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent evt) {
        ModItems.registerModel(ConveyorCover.ITEM, ConveyorCover.NAME);
        ModItems.registerModel(FilterCover.ITEM, FilterCover.NAME);
    }

}
