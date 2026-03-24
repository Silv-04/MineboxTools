package fr.silv;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.silv.utils.ModLog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads and resolves localized text entries for the mod.
 */
public final class Lang {
    private static final Logger LOGGER = ModLog.getLogger(Lang.class);
    private static final Gson GSON = new Gson();
    private static final String DEFAULT_LANGUAGE = "en_us";
    private static final TypeToken<Map<String, String>> TRANSLATION_TYPE = new TypeToken<>() {};

    private static final Map<String, Map<String, String>> CACHE = new HashMap<>();

    private static Map<String, String> translations = Collections.emptyMap();
    private static Map<String, String> fallbackTranslations = Collections.emptyMap();
    private static String loadedLanguage = "";

    private Lang() {
    }

    /**
     * Loads persisted data into memory.
     * @param lang value for lang
     */
    public static void load(String lang) {
        String requestedLanguage = normalizeLanguage(lang);
        fallbackTranslations = loadTranslations(DEFAULT_LANGUAGE);

        if (requestedLanguage.equals(loadedLanguage)) {
            return;
        }

        Map<String, String> requestedTranslations = loadTranslations(requestedLanguage);
        if (requestedTranslations.isEmpty() && !DEFAULT_LANGUAGE.equals(requestedLanguage)) {
            ModLog.warnThrottled(LOGGER, "lang-load-fallback:" + requestedLanguage, 10_000,
                    "Failed to load translations for '{}', falling back to '{}'", requestedLanguage, DEFAULT_LANGUAGE);
        }

        translations = requestedTranslations;
        loadedLanguage = requestedLanguage;
    }

    /**
     * Executes the get operation.
     * @param key value for key
     * @return the computed get value
     */
    public static String get(String key) {
        String value = translations.get(key);
        if (value != null) {
            return value;
        }

        value = fallbackTranslations.get(key);
        return value != null ? value : key;
    }

    private static String normalizeLanguage(String lang) {
        return lang == null || lang.isBlank() ? DEFAULT_LANGUAGE : lang;
    }

    private static Map<String, String> loadTranslations(String lang) {
        return CACHE.computeIfAbsent(lang, Lang::readTranslations);
    }

    private static Map<String, String> readTranslations(String lang) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return Collections.emptyMap();
        }

        Identifier id = Identifier.of("mineboxtools", "lang/" + lang + ".json");
        Optional<Resource> resource = client.getResourceManager().getResource(id);
        if (resource.isEmpty()) {
            return Collections.emptyMap();
        }

        try (InputStreamReader reader = new InputStreamReader(resource.get().getInputStream(), StandardCharsets.UTF_8)) {
            Map<String, String> loadedTranslations = GSON.fromJson(reader, TRANSLATION_TYPE.getType());
            return loadedTranslations != null ? Map.copyOf(loadedTranslations) : Collections.emptyMap();
        } catch (Exception e) {
            ModLog.warnThrottled(LOGGER, "lang-read-failure:" + lang, 10_000,
                    "Failed to read translations for '{}'", lang, e);
            return Collections.emptyMap();
        }
    }
}
