package fr.silv.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ItemStatsRangeLoader {
    private static final Map<String, Map<String, int[]>> statRanges = new HashMap<>();

    public static void load() {
        try (InputStream input = ItemStatsRangeLoader.class.getClassLoader()
                .getResourceAsStream("mineboxItemsStats.json")) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, int[]> getStatsFor(String itemId) {
        return statRanges.getOrDefault(itemId, Collections.emptyMap());
    }
}