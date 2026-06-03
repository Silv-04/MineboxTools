package fr.silv.utils;

import fr.silv.constants.StatDefinition;
import net.minecraft.network.chat.Component;

/**
 * Formats and styles statistic text fragments.
 */
public final class StatTextUtils {
    private StatTextUtils() {
    }

    /**
     * Colors a stat string according to the configured definition style.
     *
     * @param content text content to style
     * @param statKey stat key used to resolve the color definition
     * @return styled text using stat color or default style when unknown
     */
    public static Component statColor(String content, String statKey) {
        return StatDefinition.fromKey(statKey)
                .map(definition -> definition.styleText(content))
                .orElseGet(() -> StatDefinition.defaultStyle(content));
    }

    /**
     * Returns the advanced display label for a stat key.
     *
     * @param statKey stat key to format
     * @return translated symbol + name label, or raw key when unknown
     */
    public static String formatStatAdvanced(String statKey) {
        return StatDefinition.fromKey(statKey)
                .map(StatDefinition::advancedLabel)
                .orElse(statKey);
    }

    /**
     * Returns the compact display label for a stat key.
     *
     * @param statKey stat key to format
     * @return symbol-only label when known, otherwise raw key
     */
    public static String formatStatSimple(String statKey) {
        return StatDefinition.fromKey(statKey)
                .map(StatDefinition::simpleLabel)
                .orElse(statKey);
    }
}
