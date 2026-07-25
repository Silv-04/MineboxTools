package fr.silv.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.silv.ModConfig;
import fr.silv.items.DurabilityBarHandler;
import fr.silv.model.MineboxStat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Parses and caches per-item statistics, sourced from a JSON file the user fetches
 * from the Minebox API via the mod menu (no data is bundled in the jar).
 */
public final class MineboxItemStatUtils {
    private static final Logger LOGGER = ModLog.getLogger(MineboxItemStatUtils.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path ITEM_STATS_FILE = ModConfig.MOD_DATA_DIR.resolve("mineboxItems.json");
    private static final Map<String, Map<String, int[]>> STAT_RANGES = new HashMap<>();
    private static final Pattern INTEGER_PATTERN = Pattern.compile("-?\\d+");
    private static final Pattern SIGNED_INTEGER_PATTERN = Pattern.compile("[+-]?\\d+");
    private static final String BONUS_KEY = "mbx.bonus";
    private static final String STAT_KEY_PREFIX = "mbx.stats.";
    private static final String ELEMENT_KEY_PREFIX = "mbx.elements.";
    private static final String RESISTANCE_KEY = "mbx.resistance";
    private static final String RESISTANCE_SUFFIX = "_resistance";

    private MineboxItemStatUtils() {
    }

    /**
     * Loads stat ranges from the external JSON file into memory.
     * Safe to call multiple times — the cache is cleared and rebuilt on each call.
     * When the file is missing or corrupt, the cache is left empty and a warning is
     * logged; the user can populate it via the "Update item data" menu button.
     */
    public static void load() {
        STAT_RANGES.clear();

        try {
            if (!Files.exists(ITEM_STATS_FILE)) {
                LOGGER.warn("No item stats file found at {}. Use the mod menu to fetch item data.", ITEM_STATS_FILE);
                return;
            }

            try (Reader reader = Files.newBufferedReader(ITEM_STATS_FILE, StandardCharsets.UTF_8)) {
                LOGGER.info("Loading item stats ranges from {}...", ITEM_STATS_FILE);
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

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
                LOGGER.info("Item stats ranges loaded successfully ({} items).", STAT_RANGES.size());
            } catch (Exception e) {
                STAT_RANGES.clear();
                LOGGER.error("Failed to load item stats ranges from {}. Re-fetch item data from the mod menu.", ITEM_STATS_FILE, e);
            }
        } finally {
            // Reloading may change or clear previously-cached per-item lookups (e.g. after
            // an "Update item data" fetch), so dependent caches must be invalidated too.
            DurabilityBarHandler.clearStatsCache();
        }
    }

    /**
     * Writes freshly-fetched item stat data to the external JSON file and reloads the
     * in-memory cache from it. The write is atomic — a failure never corrupts the
     * previously saved file.
     *
     * @param data item id to stat-map data, in the same shape as the JSON file
     * @return {@code true} when the write and reload succeeded
     */
    public static boolean saveFetchedData(Map<String, Map<String, Object>> data) {
        try {
            Files.createDirectories(ITEM_STATS_FILE.getParent());
            Path tempFile = ITEM_STATS_FILE.resolveSibling(ITEM_STATS_FILE.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
            Files.move(tempFile, ITEM_STATS_FILE, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to write item stats to {}", ITEM_STATS_FILE, e);
            return false;
        }
        load();
        return true;
    }

    /**
     * Returns when the item stats file was last written, for display in the menu.
     *
     * @return last-modified instant, or empty when the file does not exist
     */
    public static Optional<Instant> getLastUpdated() {
        try {
            return Optional.of(Files.getLastModifiedTime(ITEM_STATS_FILE).toInstant());
        } catch (IOException e) {
            return Optional.empty();
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
        String key = resolveStatKey(line, validKeys);
        if (key == null) {
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
        String key = resolveStatKey(line, validKeys);
        if (key == null) {
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

    /**
     * Resolves the stat key represented by a lore line. Most stats carry a single
     * direct {@code mbx.stats.<key>} translatable segment. Elemental resistances
     * instead render as separate {@code mbx.elements.<element>} + {@code mbx.resistance}
     * segments with no unifying key, so those are recognized as a pair and mapped to
     * the synthesized {@code mbx.stats.<element>_resistance} key.
     *
     * @param line      lore line to inspect
     * @param validKeys supported stat translation keys
     * @return resolved stat key, or {@code null} when the line matches none of them
     */
    private static String resolveStatKey(Component line, Set<String> validKeys) {
        String element = null;
        boolean hasResistanceMarker = false;

        for (Component segment : flattenText(line)) {
            if (!(segment.getContents() instanceof TranslatableContents content)) {
                continue;
            }
            String key = content.getKey();
            if (validKeys.contains(key)) {
                return key;
            }
            if (key.startsWith(ELEMENT_KEY_PREFIX)) {
                element = key.substring(ELEMENT_KEY_PREFIX.length());
            } else if (RESISTANCE_KEY.equals(key)) {
                hasResistanceMarker = true;
            }
        }

        if (element != null && hasResistanceMarker) {
            String composite = STAT_KEY_PREFIX + element + RESISTANCE_SUFFIX;
            if (validKeys.contains(composite)) {
                return composite;
            }
        }

        return null;
    }
}
