package fr.silv.hud.widget.config;

import fr.silv.Lang;
import net.minecraft.network.chat.Component;

/**
 * Enumerates display modes used by configurable HUD/stat rendering.
 */
public enum ConfigOption {
    SIMPLE("mineboxtools.menu.stats.simple"),
    ADVANCED("mineboxtools.menu.stats.advanced"),
    OFF("mineboxtools.menu.stats.off");

    private final String translationKey;

    ConfigOption(String translationKey) {
        this.translationKey = translationKey;
    }

    /**
     * Returns the localized label text shown in cycling buttons.
     *
     * @return text instance for this option label
     */
    public Component getDisplayName() {
        return Component.literal(Lang.get(translationKey));
    }
}
