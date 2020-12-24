package eutros.coverseverywhere.main.integration.jei;

import eutros.coverseverywhere.main.covers.ConveyorCover;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class CoversEverywhereJEIPlugin implements IModPlugin {
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.useNbtForSubtypes(ConveyorCover.ITEM);
    }
}
