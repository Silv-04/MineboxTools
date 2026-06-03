package fr.silv.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.silv.model.MineboxStat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Parses and caches per-item statistics from resources.
 */
public final class MineboxItemStatUtils {
    private static final Logger LOGGER = ModLog.getLogger(MineboxItemStatUtils.class);
    private static final Map<String, Map<String, int[]>> STAT_RANGES = new HashMap<>();
    private static final Pattern INTEGER_PATTERN = Pattern.compile("-?\\d+");
    private static final Pattern SIGNED_INTEGER_PATTERN = Pattern.compile("[+-]?\\d+");
    private static final String BONUS_KEY = "mbx.bonus";
    private static final String STAT_KEY_PREFIX = "mbx.stats.";

    private MineboxItemStatUtils() {
    }

    /**
     * Loads stat ranges from the bundled JSON resource into memory.
     * Safe to call multiple times — the cache is cleared and rebuilt on each call.
     */
    public static void load() {
        try (InputStream input = MineboxItemStatUtils.class.getClassLoader()
                .getResourceAsStream("assets/mineboxtools/mineboxItems.json")) {
            LOGGER.info("Loading item stats ranges from JSON file...");
            if (input != null) {
                String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                STAT_RANGES.clear();

                for (Map.Entry<String, JsonElement> itemEntry : root.entrySet()) {
                    String itemId = itemEntry.getKey();
                    JsonObject stats = itemEntry.getValue().getAsJsonObject();
                    Map<String, int[]> itemStats = new HashMap<>();

                    for (Map.Entry<String, JsonElement> stat : stats.entrySet()) {
                        JsonElement value = stat.getValue();

                        if (value.isJsonArray()) {
                            JsonArray arr = value.getAsJsonArray();
                            if (arr.size() == 2) {
                                int[] range = new int[]{arr.get(0).getAsInt(), arr.get(1).getAsInt()};
                                itemStats.put(stat.getKey(), range);
                            }
                        } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                            int val = value.getAsInt();
                            itemStats.put(stat.getKey(), new int[]{val, val});
                        }
                    }
                    STAT_RANGES.put(itemId, itemStats);
                }
            }
            LOGGER.info("Item stats ranges loaded successfully.");
        } catch (Exception e) {
            LOGGER.error("Failed to load item stats ranges from JSON file.", e);
        }
    }

    /**
     * Returns configured stat ranges for a given item id.
     *
     * @param itemId item identifier to query
     * @return map of stat key to {@code [min, max]} range; empty when unknown
     */
    public static Map<String, int[]> getStatsFor(String itemId) {
        return STAT_RANGES.getOrDefault(itemId, Collections.emptyMap());
    }

    /**
     * Extracts a base stat value from a lore text line, ignoring bonus segments.
     *
     * @param line      lore line to parse
     * @param validKeys supported stat translation keys
     * @return parsed stat value, or {@code null} when no valid stat is found
     */
    public static MineboxStat extractStatsFromLine(Component line, Set<String> validKeys) {
        TranslatableContents content = findTranslatable(line, validKeys);
        if (content == null) {
            return null;
        }

        String key = content.getKey();
        if (!validKeys.contains(key)) {
            return null;
        }

        for (Component segment : flattenText(line)) {
            if (segment.getContents() instanceof TranslatableContents trContent
                    && BONUS_KEY.equals(trContent.getKey())) {
                continue;
            }

            Matcher matcher = INTEGER_PATTERN.matcher(segment.getString());
            if (matcher.find()) {
                try {
                    return new MineboxStat(key, Integer.parseInt(matcher.group()));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Extracts a stat value from lore and adds any bonus segment found on the same line.
     *
     * @param line      lore line to parse
     * @param validKeys supported stat translation keys
     * @return combined stat value including bonus, or {@code null} when unavailable
     */
    public static MineboxStat extractStatsFromLineWithBonus(Component line, Set<String> validKeys) {
        TranslatableContents content = findTranslatable(line, validKeys);
        if (content == null) {
            return null;
        }

        String key = content.getKey();
        if (!validKeys.contains(key)) {
            return null;
        }

        Integer baseValue = null;
        Integer bonusValue = null;
        Integer pendingNumber = null;

        for (Component segment : flattenText(line)) {
            Matcher m = SIGNED_INTEGER_PATTERN.matcher(segment.getString());
            if (m.find()) {
                try {
                    pendingNumber = Integer.parseInt(m.group());
                } catch (NumberFormatException ignored) {
                }
            }

            if (segment.getContents() instanceof TranslatableContents tr) {
                String k = tr.getKey();
                if (k.startsWith(STAT_KEY_PREFIX)) {
                    if (pendingNumber != null && baseValue == null) {
                        baseValue = pendingNumber;
                        pendingNumber = null;
                    }
                } else if (BONUS_KEY.equals(k)) {
                    if (pendingNumber != null && bonusValue == null) {
                        bonusValue = pendingNumber;
                        pendingNumber = null;
                    }
                }
            }
        }

        if (baseValue == null && pendingNumber != null) {
            baseValue = pendingNumber;
        }

        if (baseValue == null) {
            return null;
        }

        int total = baseValue + (bonusValue != null ? bonusValue : 0);
        return new MineboxStat(key, total);
    }

    private static List<Component> flattenText(Component text) {
        List<Component> result = new ArrayList<>();
        result.add(text);
        for (Component sibling : text.getSiblings()) {
            result.addAll(flattenText(sibling));
        }
        return result;
    }

    private static TranslatableContents findTranslatable(Component text, Set<String> validKeys) {
        if (text.getContents() instanceof TranslatableContents content && validKeys.contains(content.getKey())) {
            return content;
        }
        for (Component sibling : text.getSiblings()) {
            TranslatableContents result = findTranslatable(sibling, validKeys);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
