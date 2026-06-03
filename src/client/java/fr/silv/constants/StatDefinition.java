package fr.silv.constants;

import fr.silv.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

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
    AGILITY("mbx.stats.agility", "mineboxtools.stat.agility", 1, 0x6BC047, "☄"),
    DEFENSE("mbx.stats.defense", "mineboxtools.stat.defense", 1, 0x9E9E9E, "🛡"),
    DEXTERITY("mbx.stats.dexterity", "mineboxtools.stat.dexterity", 1, 0xBDBDBD, "✗"),
    ENDURANCE("mbx.stats.endurance", "mineboxtools.stat.endurance", 1, 0x4CAF50, "💪"),
    ENERGY("mbx.stats.energy", "mineboxtools.stat.energy", 1, 0xFFD700, "⚡"),
    FORTUNE("mbx.stats.fortune", "mineboxtools.stat.fortune", 1, 0xEC8C2E, "🔱"),
    MINING_FORTUNE("mbx.stats.mining_fortune", "mineboxtools.stat.mining_fortune", 1, 0x9E9E9E, "⛏"),
    FISHING_FORTUNE("mbx.stats.fishing_fortune", "mineboxtools.stat.fishing_fortune", 1, 0x29B6F6, "🎣"),
    WOODCUTTING_FORTUNE("mbx.stats.woodcutting_fortune", "mineboxtools.stat.woodcutting_fortune", 1, 0x8D6E63, "🪓"),
    FARMING_FORTUNE("mbx.stats.farming_fortune", "mineboxtools.stat.farming_fortune", 1, 0x8BC34A, "🌾"),
    GATHERING_FORTUNE("mbx.stats.gathering_fortune", "mineboxtools.stat.gathering_fortune", 1, 0x66BB6A, "🌿"),
    LOOTING_FORTUNE("mbx.stats.looting_fortune", "mineboxtools.stat.looting_fortune", 1, 0xBA68C8, "💎"),
    HEALTH("mbx.stats.health", "mineboxtools.stat.health", 1, 0xE24A63, "❤"),
    INTELLIGENCE("mbx.stats.intelligence", "mineboxtools.stat.intelligence", 1, 0xE24A2E, "🔥"),
    LUCK("mbx.stats.luck", "mineboxtools.stat.luck", 1, 0x3D84A8, "🌊"),
    MOVEMENT_SPEED("mbx.stats.movement_speed", "mineboxtools.stat.movement_speed", 1, 0x26C6DA, "«"),
    STRENGTH("mbx.stats.strength", "mineboxtools.stat.strength", 1, 0xA55F26, "₪"),
    WISDOM("mbx.stats.wisdom", "mineboxtools.stat.wisdom", 1, 0x9457D3, "☽"),
    ATTACK_SPEED("mbx.stats.attack_speed", "mineboxtools.stat.attack_speed", 1, 0xFFEB3B, "⚔"),
    CHARISMA("mbx.stats.charisma", "mineboxtools.stat.charisma", 1, 0xF06292, "⚓");

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
    public Component styleText(String content) {
        MutableComponent text = Component.literal(content);
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
    public static Component defaultStyle(String content) {
        return Component.literal(content).setStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
    }
}
