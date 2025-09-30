package fr.silv.hud.widget;

import java.util.ArrayList;
import java.util.List;

public class HudWidgetManager {
    private static final List<HudWidget> WIDGETS = new ArrayList<>();

    public static void init() {
        WIDGETS.add(new DurabilityWidget());
        WIDGETS.add(new IconWidget());
        WIDGETS.add(new StatWidget());
    }

    public static List<HudWidget> getWidgets() {
        return WIDGETS;
    }
}
