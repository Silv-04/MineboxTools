package fr.silv.utils;

import com.google.gson.*;
import fr.silv.model.MineboxItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MineboxItemUtils {
    private static final Logger LOGGER = LogManager.getLogger(MineboxItemUtils.class);
    private static Map<String, MineboxItem> mineboxItems = new HashMap<>();

    public static void load() {
        try (InputStream input = MineboxItemUtils.class.getClassLoader()
                .getResourceAsStream("assets/mineboxtools/mineboxItems.json")) {
            if (input == null) {
                LOGGER.error("Cannot find mineboxItems.json !");
                return;
            }

            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            JsonArray itemsArray = JsonParser.parseString(json).getAsJsonArray();

            for (JsonElement element : itemsArray) {
                JsonObject obj = element.getAsJsonObject();
                String id = obj.get("id").getAsString();

                JsonArray locArray = obj.getAsJsonArray("location");
                List<String> location = new ArrayList<>();
                for (JsonElement locElem : locArray) {
                    location.add(locElem.getAsString());
                }

                String condition = obj.get("condition").getAsString();

                MineboxItem item = new MineboxItem(id, location, condition);
                mineboxItems.put(id, item);
            }

        } catch (Exception e) {
            LOGGER.error("Error loading harvestable file", e);
        }
    }

    public static MineboxItem get(String itemId) {
        return mineboxItems.get(itemId);
    }

}
