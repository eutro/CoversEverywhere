package eutros.coverseverywhere.common.util;

import java.util.function.Consumer;

public class Expressions {
    @SafeVarargs
    public static <T> T doto(T obj, Consumer<T>... actions) {
        for (Consumer<T> action : actions) {
            action.accept(obj);
        }
        return obj;
    }
}
