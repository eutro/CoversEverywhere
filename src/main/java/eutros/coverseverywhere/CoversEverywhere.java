package eutros.coverseverywhere;

import eutros.coverseverywhere.common.CoversEverywhereAPIImpl;
import eutros.coverseverywhere.common.covers.Covers;
import eutros.coverseverywhere.common.covers.CoversCapabilityProvider;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = CoversEverywhere.MOD_ID,
     name = CoversEverywhere.MOD_NAME,
     version = CoversEverywhere.VERSION)
public class CoversEverywhere {

    public static final String MOD_ID = "covers_everywhere";
    public static final String MOD_NAME = "Covers Everywhere";
    public static final String VERSION = "GRADLE:VERSION";

    public CoversEverywhere() {
        CoversEverywhereAPIImpl.init();
        Covers.init();
    }

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        CoversCapabilityProvider.init();
    }

}
