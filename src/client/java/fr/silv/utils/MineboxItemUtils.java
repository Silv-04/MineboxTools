package fr.silv.utils;

import com.google.gson.*;
import fr.silv.model.MineboxItem;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for loading and querying minebox item metadata.
 */
public class MineboxItemUtils {
    private static final Logger LOGGER = ModLog.getLogger(MineboxItemUtils.class);
    private static final Map<String, MineboxItem> mineboxItems = new HashMap<>();

    /**
     * Loads persisted data into memory.
     */
    public static void load() {
        try (InputStream input = MineboxItemUtils.class.getClassLoader()
                .getResourceAsStream("assets/mineboxtools/mineboxHarvestables.json")) {
            if (input == null) {
                LOGGER.error("Cannot find mineboxHarvestables.json !");
                return;
            }

            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            JsonArray itemsArray = JsonParser.parseString(json).getAsJsonArray();
            mineboxItems.clear();

            for (JsonElement element : itemsArray) {
                JsonObject obj = element.getAsJsonObject();
                String id = obj.get("id").getAsString();

                JsonArray locArray = obj.getAsJsonArray("location");
                List<String> location = new ArrayList<>();
                for (JsonElement locElem : locArray) {
                    location.add(locElem.getAsString());
                }

                List<String> conditions = parseStringOrArray(obj.get("condition"));
                String boost = obj.get("boost").getAsString();

                MineboxItem item = new MineboxItem(id, location, conditions, boost);
                mineboxItems.put(id, item);
            }

        } catch (Exception e) {
            LOGGER.error("Error loading harvestable file", e);
        }
    }

    /**
     * Returns cached metadata for a Minebox item id.
     *
     * @param itemId item identifier from game data
     * @return matching item metadata, or {@code null} when absent
     */
    public static MineboxItem get(String itemId) {
        return mineboxItems.get(itemId);
    }

    private static List<String> parseStringOrArray(JsonElement element) {
        if (element == null || element.isJsonNull()) return List.of();
        if (element.isJsonArray()) {
            List<String> result = new ArrayList<>();
            for (JsonElement e : element.getAsJsonArray()) {
                String s = e.getAsString();
                if (!s.isEmpty()) result.add(s);
            }
            return result;
        }
        String s = element.getAsString();
        return s.isEmpty() ? List.of() : List.of(s);
    }

}
