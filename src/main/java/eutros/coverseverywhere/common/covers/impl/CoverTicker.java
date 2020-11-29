package eutros.coverseverywhere.common.covers.impl;

import eutros.coverseverywhere.api.ICover;
import eutros.coverseverywhere.api.ICoverHolder;
import net.minecraft.util.EnumFacing;
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
            for(ICoverHolder provider : CoverManager.get(event.world).getHolders()) {
                for(EnumFacing side : EnumFacing.values()) {
                    for(ICover cover : provider.get(side)) {
                        cover.tick();
                    }
                }
            }
        }
    }

}
