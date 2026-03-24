package fr.silv.utils;

import fr.silv.constants.StatDefinition;
import net.minecraft.text.Text;

/**
 * Formats and styles statistic text fragments.
 */
public final class StatTextUtils {
    private StatTextUtils() {
    }

    /**
     * Executes the stat color operation.
     * @param content value for content
     * @param statKey value for statKey
     * @return the computed stat color value
     */
    public static Text statColor(String content, String statKey) {
        return StatDefinition.fromKey(statKey)
                .map(definition -> definition.styleText(content))
                .orElseGet(() -> StatDefinition.defaultStyle(content));
    }

    /**
     * Formats the stat advanced.
     * @param statKey value for statKey
     * @return the computed format stat advanced value
     */
    public static String formatStatAdvanced(String statKey) {
        return StatDefinition.fromKey(statKey)
                .map(StatDefinition::advancedLabel)
                .orElse(statKey);
    }

    /**
     * Formats the stat simple.
     * @param statKey value for statKey
     * @return the computed format stat simple value
     */
    public static String formatStatSimple(String statKey) {
        return StatDefinition.fromKey(statKey)
                .map(StatDefinition::simpleLabel)
                .orElse(statKey);
    }
}
