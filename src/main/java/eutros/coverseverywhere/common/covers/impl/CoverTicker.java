package eutros.coverseverywhere.common.covers.impl;

import eutros.coverseverywhere.api.ICover;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class CoverTicker {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(CoverTicker.class);
    }

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        if(event.phase == TickEvent.Phase.END) {
            for(ICover cover : CoverManager.get(event.world).getCovers()) cover.tick();
        }
    }

}
