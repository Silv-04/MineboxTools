package fr.silv.effects;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.silv.utils.ModLog;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Loads the Minebox effects catalog and answers lookups against it.
 *
 * <p>The catalog is fetched from the API exactly once and cached to a file; on every later start the
 * cached file is read and the network is never touched again (delete the file to force a refresh).
 * Entries are indexed under every id a menu or consumed item might reference - the effect {@code id},
 * its {@code group_id}, and each {@code durations[].item} - so both the effects menu's
 * {@code mythicmobs:type} and a consumed item's {@code mbitems:id} resolve to the same entry.
 */
public final class EffectCatalogService {
    private static final Logger LOGGER = ModLog.getLogger(EffectCatalogService.class);

    private static final String EFFECTS_URL = "https://api.minebox.co/effects";
    private static final int MAX_RESPONSE_BYTES = 2 * 1024 * 1024;
    private static final Path CACHE_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("mineboxtools.effects.json");

    private static final String USER_AGENT = "MBT/" + FabricLoader.getInstance()
            .getModContainer("mineboxtools")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static volatile Map<String, CatalogEffect> index = Map.of();

    private EffectCatalogService() {
    }

    /**
     * Loads the catalog: from the cached file when present, otherwise fetched once from the API and
     * cached. Safe to call on the client thread - any network work runs asynchronously.
     */
    public static void init() {
        if (Files.exists(CACHE_FILE)) {
            loadFromCache();
        } else {
            fetchAndCache();
        }
    }

    /** Whether the catalog is loaded and non-empty. */
    public static boolean isLoaded() {
        return !index.isEmpty();
    }

    /** The catalog entry registered under {@code key} (effect id, group id or source item id), if any. */
    public static Optional<CatalogEffect> getByKey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(index.get(key.toLowerCase(Locale.ROOT)));
    }

    private static void loadFromCache() {
        try {
            String json = Files.readString(CACHE_FILE, StandardCharsets.UTF_8);
            if (buildIndex(json)) {
                LOGGER.info("Loaded effects catalog from cache ({} entries)", index.size());
            } else {
                LOGGER.warn("Cached effects catalog at {} was empty or invalid", CACHE_FILE);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read cached effects catalog from {}", CACHE_FILE, e);
        }
    }

    private static void fetchAndCache() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(EFFECTS_URL))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();

        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        LOGGER.warn("Effects catalog fetch returned HTTP {}", response.statusCode());
                        return;
                    }
                    byte[] bytes = response.body();
                    if (bytes.length > MAX_RESPONSE_BYTES) {
                        LOGGER.warn("Effects catalog response exceeded {} bytes; rejected", MAX_RESPONSE_BYTES);
                        return;
                    }
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    if (!buildIndex(json)) {
                        LOGGER.warn("Effects catalog response was empty or invalid; not caching");
                        return;
                    }
                    writeCache(json);
                    LOGGER.info("Fetched effects catalog from API ({} entries) and cached to {}", index.size(), CACHE_FILE);
                })
                .exceptionally(throwable -> {
                    LOGGER.error("Network error while fetching effects catalog", throwable);
                    return null;
                });
    }

    private static boolean buildIndex(String json) {
        EffectCatalog catalog;
        try {
            catalog = GSON.fromJson(json, EffectCatalog.class);
        } catch (JsonSyntaxException e) {
            LOGGER.warn("Failed to parse effects catalog JSON", e);
            return false;
        }
        if (catalog == null || catalog.effects == null || catalog.effects.isEmpty()) {
            return false;
        }

        Map<String, CatalogEffect> built = new HashMap<>();
        for (CatalogEffect effect : catalog.effects) {
            register(built, effect.id, effect);
            register(built, effect.groupId, effect);
            if (effect.durations != null) {
                for (CatalogEffect.DurationEntry entry : effect.durations) {
                    register(built, entry == null ? null : entry.item, effect);
                }
            }
        }
        index = Map.copyOf(built);
        return true;
    }

    private static void register(Map<String, CatalogEffect> target, String key, CatalogEffect effect) {
        if (key != null && !key.isBlank()) {
            // First writer wins: an effect's own id is registered before its group/item aliases.
            target.putIfAbsent(key.toLowerCase(Locale.ROOT), effect);
        }
    }

    private static void writeCache(String json) {
        try {
            Files.writeString(CACHE_FILE, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Failed to write effects catalog cache to {}", CACHE_FILE, e);
        }
    }

    /** Every effect id currently indexed (for debugging/introspection). */
    public static List<String> indexedKeys() {
        return index.keySet().stream().sorted().toList();
    }
}
