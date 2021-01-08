package eutros.coverseverywhere.main.gui;

import java.util.List;

public interface Option {
    List<String> getTooltip();

    void renderAt(int x, int y);
}
