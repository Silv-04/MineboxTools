package fr.silv;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class Lang {
    private static Map<String, String> translations = Collections.emptyMap();
    private static Logger LangLogger = LogManager.getLogger(Lang.class);

    public static void load(String lang) {
        try {
            Identifier id = Identifier.of("mineboxtools", "lang/" + lang + ".json");
            Resource res = MinecraftClient.getInstance().getResourceManager().getResource(id).get();

            try (InputStreamReader reader = new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8)) {
                translations = new Gson().fromJson(reader, new TypeToken<Map<String, String>>(){}.getType());
            }
        } catch (Exception e) {
            LangLogger.error(e.getMessage());
            translations = Collections.emptyMap();
        }
    }

    public static String get(String key) {
        return translations.getOrDefault(key, key);
    }
}
