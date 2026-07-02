package fr.silv.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Loads {@code museum.json} and exposes the flat set of item ids that can be donated to the museum.
 */
public final class MuseumItemUtils {
    private static final Logger LOGGER = ModLog.getLogger(MuseumItemUtils.class);
    private static final Set<String> museumItemIds = new HashSet<>();

    private MuseumItemUtils() {
    }

    /**
     * Loads persisted data into memory.
     */
    public static void load() {
        try (InputStream input = MuseumItemUtils.class.getClassLoader()
                .getResourceAsStream("assets/mineboxtools/museum.json")) {
            if (input == null) {
                LOGGER.error("Cannot find museum.json !");
                return;
            }

            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject categories = JsonParser.parseString(json).getAsJsonObject();
            museumItemIds.clear();

            for (String category : categories.keySet()) {
                for (JsonElement itemId : categories.getAsJsonArray(category)) {
                    museumItemIds.add(itemId.getAsString());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error loading museum file", e);
        }
    }

    /**
     * Indicates whether an item id can be donated to the museum.
     *
     * @param itemId Minebox item identifier
     * @return {@code true} when the item appears in {@code museum.json}
     */
    public static boolean isMuseumItem(String itemId) {
        return itemId != null && museumItemIds.contains(itemId);
    }
}
