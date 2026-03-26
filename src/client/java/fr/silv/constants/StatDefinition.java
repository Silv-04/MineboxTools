package fr.silv.constants;

import fr.silv.Lang;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Defines supported stat keys, labels, colors, and symbol metadata.
 */
public enum StatDefinition {
    FORTUNE("mbx.stats.fortune", "mineboxtools.stat.fortune", 1, 0xEC8C2E, "\uD83D\uDD31"),
    LUCK("mbx.stats.luck", "mineboxtools.stat.luck", 1, 0x3D84A8, "\uD83C\uDF0A"),
    INTELLIGENCE("mbx.stats.intelligence", "mineboxtools.stat.intelligence", 1, 0xE24A2E, "\uD83D\uDD25"),
    STRENGTH("mbx.stats.strength", "mineboxtools.stat.strength", 1, 0xA55F26, "\u20AA"),
    HEALTH("mbx.stats.health", "mineboxtools.stat.health", 1, 0xE24A63, "\u2764"),
    AGILITY("mbx.stats.agility", "mineboxtools.stat.agility", 1, 0x6BC047, "\u2604"),
    WISDOM("mbx.stats.wisdom", "mineboxtools.stat.wisdom", 1, 0x9457D3, "\u263D"),
    DEFENSE("mbx.stats.defense", "mineboxtools.stat.defense", 1, 0x1F8ECD, "\uD83D\uDEE1");

    private static final Map<String, StatDefinition> BY_KEY = Arrays.stream(values())
            .collect(Collectors.toMap(StatDefinition::key, Function.identity()));

    private final String key;
    private final String translationKey;
    private final int weight;
    private final int color;
    private final String symbol;

    StatDefinition(String key, String translationKey, int weight, int color, String symbol) {
        this.key = key;
        this.translationKey = translationKey;
        this.weight = weight;
        this.color = color;
        this.symbol = symbol;
    }

    /**
     * Returns the canonical stat key used in item metadata.
     *
     * @return lowercase stat key
     */
    public String key() {
        return key;
    }

    /**
     * Returns the weighting factor used in stat calculations.
     *
     * @return stat weight value
     */
    public int weight() {
        return weight;
    }

    /**
     * Returns the compact symbol-only representation of the stat.
     *
     * @return short symbol label
     */
    public String simpleLabel() {
        return symbol;
    }

    /**
     * Returns the full display label combining symbol and translated name.
     *
     * @return user-facing translated stat label
     */
    public String advancedLabel() {
        return symbol + " " + Lang.get(translationKey);
    }

    /**
     * Applies this stat's configured color style to text content.
     *
     * @param content text to style
     * @return colored text instance
     */
    public Text styleText(String content) {
        MutableText text = Text.literal(content);
        return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
    }

    /**
     * Resolves a stat definition from a metadata key.
     *
     * @param key key to resolve (case-insensitive)
     * @return matching definition when available
     */
    public static Optional<StatDefinition> fromKey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_KEY.get(key.toLowerCase()));
    }

    /**
        * Returns all stat keys in declaration order.
        *
        * @return ordered set of supported stat keys
     */
    public static Set<String> keys() {
        return Arrays.stream(values())
                .map(StatDefinition::key)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Applies the default white style to plain text content.
     *
     * @param content text to style
     * @return white-styled text instance
     */
    public static Text defaultStyle(String content) {
        return Text.literal(content).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
    }
}
