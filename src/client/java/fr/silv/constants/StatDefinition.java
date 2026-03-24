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
 * Ãƒâ€°numÃƒÂ©ration StatDefinition.
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
     * Executes the key operation.
     * @return the computed key value
     */
    public String key() {
        return key;
    }

    /**
     * Executes the weight operation.
     * @return the computed weight value
     */
    public int weight() {
        return weight;
    }

    /**
     * Executes the simple label operation.
     * @return the computed simple label value
     */
    public String simpleLabel() {
        return symbol;
    }

    /**
     * Executes the advanced label operation.
     * @return the computed advanced label value
     */
    public String advancedLabel() {
        return symbol + " " + Lang.get(translationKey);
    }

    /**
     * Executes the style text operation.
     * @param content value for content
     * @return the computed style text value
     */
    public Text styleText(String content) {
        MutableText text = Text.literal(content);
        return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
    }

    /**
     * Executes the from key operation.
     * @param key value for key
     * @return an optional statdefinition value when present
     */
    public static Optional<StatDefinition> fromKey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_KEY.get(key.toLowerCase()));
    }

    /**
     * Executes the keys operation.
     * @return the computed keys value
     */
    public static Set<String> keys() {
        return Arrays.stream(values())
                .map(StatDefinition::key)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Executes the default style operation.
     * @param content value for content
     * @return the computed default style value
     */
    public static Text defaultStyle(String content) {
        return Text.literal(content).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
    }
}
