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
        * Rebuilds the active HUD widget list in display order.
     */
    public static void init() {
        WIDGETS.clear();
        WIDGETS.add(new DurabilityWidget());
        WIDGETS.add(new IconWidget());
        WIDGETS.add(new StatWidget());
    }

    /**
     * Returns an immutable view of currently registered HUD widgets.
     *
     * @return active widget list
     */
    public static List<HudWidget> getWidgets() {
        return Collections.unmodifiableList(WIDGETS);
    }
}
