package eutros.coverseverywhere.api;

public interface ICoverRevealer {

    default boolean showCovers() {
        return true;
    }

    default boolean showGrid() {
        return true;
    }

}
