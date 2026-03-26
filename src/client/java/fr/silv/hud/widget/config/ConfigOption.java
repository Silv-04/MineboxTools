package fr.silv.hud.widget.config;

import net.minecraft.text.Text;

/**
 * Enumerates display modes used by configurable HUD/stat rendering.
 */
public enum ConfigOption {
    SIMPLE("Simple"),
    ADVANCED("Advanced"),
    OFF("Off");

    private final String name;

    ConfigOption(String name) {
        this.name = name;
    }

    /**
     * Returns the localized label text shown in cycling buttons.
     *
     * @return text instance for this option label
     */
    public Text getDisplayName() {
        return Text.literal(name);
    }
}
