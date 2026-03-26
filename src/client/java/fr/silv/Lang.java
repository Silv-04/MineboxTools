package fr.silv;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.silv.utils.ModLog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
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
        * Loads translation entries for the requested language into memory.
        *
        * @param lang language code to load (falls back to default when invalid)
     */
    public static void load(String lang) {
        String requestedLanguage = normalizeLanguage(lang);
        Map<String, String> requestedFallbackTranslations = loadTranslations(DEFAULT_LANGUAGE);
        fallbackTranslations = requestedFallbackTranslations;

        boolean hasUsableTranslations = !translations.isEmpty() || !fallbackTranslations.isEmpty();

        if (requestedLanguage.equals(loadedLanguage) && hasUsableTranslations) {
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
     * Resolves a localized message value for the provided translation key.
     * The lookup first uses the currently selected language, then falls back to
     * default-language entries, and finally returns the key itself.
     *
     * @param key translation key to resolve
     * @return localized string or the original key when missing
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

        ResourceManager resourceManager = client.getResourceManager();
        if (resourceManager == null) {
            ModLog.warnThrottled(LOGGER, "lang-resource-manager-unavailable", 10_000,
                    "Skipping translation load for '{}' because resource manager is not initialized yet", lang);
            return Collections.emptyMap();
        }

        Identifier id = Identifier.of("mineboxtools", "lang/" + lang + ".json");
        Optional<Resource> resource = resourceManager.getResource(id);
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
