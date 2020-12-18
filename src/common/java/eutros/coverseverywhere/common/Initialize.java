package eutros.coverseverywhere.common;

import net.minecraftforge.fml.relauncher.Side;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Initialize {
    String[] requiredMods() default {};

    Side[] sides() default { Side.CLIENT, Side.SERVER };
}
