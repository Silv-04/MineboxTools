package fr.silv.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.silv.model.MineboxStat;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ItemStatUtils {
    private static final Logger ItemStatsRangeLoaderLogger = LogManager.getLogger(ItemStatUtils.class);
    private static final Map<String, Map<String, int[]>> statRanges = new HashMap<>();

    public static void load() {
        try (InputStream input = ItemStatUtils.class.getClassLoader()
                .getResourceAsStream("assets/mineboxtools/mineboxItemsStats.json")) {
            ItemStatsRangeLoaderLogger.info("Loading item stats ranges from JSON file...");
            if (input != null) {
                String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();

                for (Map.Entry<String, JsonElement> itemEntry : root.entrySet()) {
                    String itemId = itemEntry.getKey();
                    JsonObject stats = itemEntry.getValue().getAsJsonObject();
                    Map<String, int[]> itemStats = new HashMap<>();

                    for (Map.Entry<String, JsonElement> stat : stats.entrySet()) {
                        JsonElement value = stat.getValue();

                        if (value.isJsonArray()) {
                            JsonArray arr = value.getAsJsonArray();
                            if (arr.size() == 2) {
                                int[] range = new int[] { arr.get(0).getAsInt(), arr.get(1).getAsInt() };
                                itemStats.put(stat.getKey(), range);
                            }
                        } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                            int val = value.getAsInt();
                            itemStats.put(stat.getKey(), new int[] { val, val });
                        }
                    }
                    statRanges.put(itemId, itemStats);
                }
            }
            ItemStatsRangeLoaderLogger.info("Item stats ranges loaded successfully.");
        } catch (Exception e) {
            ItemStatsRangeLoaderLogger.error("Failed to load item stats ranges from JSON file.", e);
        }
    }

    public static Map<String, int[]> getStatsFor(String itemId) {
        return statRanges.getOrDefault(itemId, Collections.emptyMap());
    }

    public static MineboxStat extractStatsFromLine(Text line, Set<String> validKeys) {
        TranslatableTextContent content = findTranslatable(line, validKeys);
        if (content == null)
            return null;

        String key = content.getKey();
        if (!validKeys.contains(key))
            return null;

        List<Text> flat = flattenText(line);

        for (Text segment : flat) {
            if (segment.getContent() instanceof TranslatableTextContent trContent) {
                if (trContent.getKey().equals("mbx.bonus"))
                    continue;
            }

            String raw = segment.getString();
            Matcher matcher = Pattern.compile("-?\\d+").matcher(raw);
            if (matcher.find()) {
                try {
                    int value = Integer.parseInt(matcher.group());
                    return new MineboxStat(key, value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }

    public static MineboxStat extractStatsFromLineWithBonus(Text line, Set<String> validKeys) {
        TranslatableTextContent content = findTranslatable(line, validKeys);
        if (content == null) return null;

        String key = content.getKey();
        if (!validKeys.contains(key)) return null;

        List<Text> flat = flattenText(line);

        Integer baseValue = null;
        Integer bonusValue = null;
        Integer pendingNumber = null;

        Pattern intPat = Pattern.compile("[+-]?\\d+");

        for (Text segment : flat) {
            String raw = segment.getString();
            Matcher m = intPat.matcher(raw);
            if (m.find()) {
                try {
                    pendingNumber = Integer.parseInt(m.group());
                } catch (NumberFormatException ignored) {}
            }

            if (segment.getContent() instanceof TranslatableTextContent tr) {
                String k = tr.getKey();

                if (k.startsWith("mbx.stats.")) {
                    if (pendingNumber != null && baseValue == null) {
                        baseValue = pendingNumber;
                        pendingNumber = null;
                    }
                } else if (k.equals("mbx.bonus")) {
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

        if (baseValue == null) return null;

        int total = baseValue + (bonusValue != null ? bonusValue : 0);
        return new MineboxStat(key, total);
    }


    private static List<Text> flattenText(Text text) {
        List<Text> result = new java.util.ArrayList<>();
        result.add(text);
        for (Text sibling : text.getSiblings()) {
            result.addAll(flattenText(sibling));
        }
        return result;
    }

    private static TranslatableTextContent findTranslatable(Text text, Set<String> validKeys) {
        if (text.getContent() instanceof TranslatableTextContent content && validKeys.contains(content.getKey())) {
            return content;
        }
        for (Text sibling : text.getSiblings()) {
            TranslatableTextContent result = findTranslatable(sibling, validKeys);
            if (result != null)
                return result;
        }
        return null;
    }

    public static boolean areEquals(List<MineboxStat> listA, List<MineboxStat> listB) {
        if (listA.size() != listB.size()) return false;
        for (MineboxStat statA : listA) {
            boolean found = false;
            for (MineboxStat statB : listB) {
                if (statA.getStat().equals(statB.getStat()) && statA.getValue() == statB.getValue()) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }
}