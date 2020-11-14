package eutros.coverseverywhere.api;

public interface ICoverRevealer {

    default boolean shouldShowCover(ICover cover) {
        return true;
    }

    default boolean shouldShowGrid() {
        return true;
    }

}
