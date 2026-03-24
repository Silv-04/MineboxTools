package fr.silv.hud.widget.config;

import net.minecraft.text.Text;

/**
 * Ã‰numÃ©ration ConfigOption.
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
     * Returns the display name.
     * @return the display name
     */
    public Text getDisplayName() {
        return Text.literal(name);
    }
}
