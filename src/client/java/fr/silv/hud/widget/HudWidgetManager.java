package fr.silv.hud.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates and exposes the active HUD widget set.
 */
public class HudWidgetManager {
    private static final List<HudWidget> WIDGETS = new ArrayList<>();

    /**
     * Executes the init operation.
     */
    public static void init() {
        WIDGETS.clear();
        WIDGETS.add(new DurabilityWidget());
        WIDGETS.add(new IconWidget());
        WIDGETS.add(new StatWidget());
    }

    /**
     * Returns the widgets.
     * @return the widgets
     */
    public static List<HudWidget> getWidgets() {
        return Collections.unmodifiableList(WIDGETS);
    }
}
