package eutros.coverseverywhere.main;

import eutros.coverseverywhere.common.Constants;
import eutros.coverseverywhere.common.Initialize;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collection;
import java.util.Map;

@Mod(modid = Constants.MOD_ID,
        name = Constants.MOD_NAME,
        version = Constants.VERSION)
public class CoversEverywhere {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Side side = FMLCommonHandler.instance().getSide();
        try {
            for (ASMDataTable.ASMData data : event.getAsmData().getAll(Initialize.class.getName())) {
                Map<String, Object> info = data.getAnnotationInfo();
                Collection<?> requiredMods = (Collection<?>) info.get("requiredMods");
                Collection<?> sides = (Collection<?>) info.get("sides");
                if ((sides == null || sides.stream()
                        .map(ModAnnotation.EnumHolder.class::cast)
                        .map(ModAnnotation.EnumHolder::getValue)
                        .map(Side::valueOf)
                        .anyMatch(s -> s == side))
                        && (requiredMods == null || requiredMods.stream()
                        .map(String.class::cast)
                        .allMatch(Loader::isModLoaded))) {
                    String methodNameAndDesc = data.getObjectName();
                    String methodName = methodNameAndDesc.substring(0, methodNameAndDesc.indexOf('('));
                    Class.forName(data.getClassName()).getMethod(methodName).invoke(null);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
