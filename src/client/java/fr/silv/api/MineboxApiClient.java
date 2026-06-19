package fr.silv.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.silv.utils.ModLog;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * Async HTTP client for the Minebox REST API.
 * All requests are non-blocking; results are delivered via {@link CompletableFuture}.
 */
public final class MineboxApiClient {
    private static final Logger LOGGER = ModLog.getLogger(MineboxApiClient.class);

    /** Base URL of the Minebox API. */
    public static final String BASE_URL = "https://api.minebox.co/";

    /** Maximum accepted response body size; larger responses are rejected to avoid excessive memory use. */
    private static final int MAX_RESPONSE_BYTES = 256 * 1024;

    /** Valid Minecraft username pattern: 1–16 letters, digits or underscores. */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{1,16}$");

    /** Valid guild name pattern: 1–32 letters, digits, spaces or underscores. */
    private static final Pattern GUILD_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_ ]{1,32}$");

    public static final long COOLDOWN_MS = 3000L;
    private static volatile long lastRequestMillis = 0L;

    /** Mod version resolved from Fabric metadata, used in the User-Agent header. */
    private static final String VERSION = FabricLoader.getInstance()
            .getModContainer("mineboxtools")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");

    /** User-Agent identifying this mod to the Minebox API. */
    private static final String USER_AGENT = "MBT/" + VERSION;

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private MineboxApiClient() {
    }

    /**
     * Acquires the shared request cooldown. Returns {@code true} and records the current
     * timestamp when enough time has elapsed since the last request; returns {@code false}
     * immediately otherwise.
     */
    public static boolean tryAcquireCooldown() {
        long now = System.currentTimeMillis();
        if (now - lastRequestMillis < COOLDOWN_MS) return false;
        lastRequestMillis = now;
        return true;
    }

    /**
     * Validates that a username matches the Minecraft username format.
     *
     * @param username candidate username
     * @return {@code true} when the username is safe to embed in a request URL
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validates that a guild name matches the expected format.
     *
     * @param guildName candidate guild name
     * @return {@code true} when the name is safe to embed in a request URL
     */
    public static boolean isValidGuildName(String guildName) {
        return guildName != null && GUILD_NAME_PATTERN.matcher(guildName).matches();
    }

    /**
     * Categorises the possible failure modes of a player lookup.
     */
    public enum LookupError {
        /** HTTP 404 — the username does not exist. */
        NOT_FOUND,
        /** HTTP 403 — the player has disabled profile sharing. */
        PROFILE_PRIVATE,
        /** HTTP 429 — the API rate limit has been exceeded. */
        RATE_LIMITED,
        /** HTTP 5xx — the API is temporarily unavailable. */
        SERVER_ERROR,
        /** Network-level failure (timeout, DNS, connection refused…). */
        NETWORK_ERROR,
        /** The username does not match the expected Minecraft username format. */
        INVALID_USERNAME
    }

    /**
     * Fetches a player profile asynchronously.
     * The future always completes normally; errors are wrapped in {@link ApiResult#err}.
     *
     * @param username in-game username to look up
     * @return future resolving to a profile on success or a {@link LookupError} on failure
     */
    public static CompletableFuture<ApiResult<PlayerProfile, LookupError>> fetchPlayerProfile(String username) {
        if (!isValidUsername(username)) {
            return CompletableFuture.completedFuture(ApiResult.err(LookupError.INVALID_USERNAME));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "data/" + username))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .handle((response, ex) -> {
                    if (ex != null) {
                        LOGGER.error("Network error while fetching profile for '{}'", username, ex);
                        return ApiResult.<PlayerProfile, LookupError>err(LookupError.NETWORK_ERROR);
                    }
                    try (InputStream body = response.body()) {
                        return switch (response.statusCode()) {
                            case 200 -> readProfile(body, username);
                            case 404 -> ApiResult.<PlayerProfile, LookupError>err(LookupError.NOT_FOUND);
                            case 403 -> ApiResult.<PlayerProfile, LookupError>err(LookupError.PROFILE_PRIVATE);
                            case 429 -> ApiResult.<PlayerProfile, LookupError>err(LookupError.RATE_LIMITED);
                            default -> {
                                LOGGER.warn("Minebox API returned HTTP {} for '{}'", response.statusCode(), username);
                                yield ApiResult.<PlayerProfile, LookupError>err(LookupError.SERVER_ERROR);
                            }
                        };
                    } catch (IOException ioException) {
                        LOGGER.error("Error reading API response for '{}'", username, ioException);
                        return ApiResult.<PlayerProfile, LookupError>err(LookupError.NETWORK_ERROR);
                    }
                });
    }

    /**
     * Fetches a guild profile asynchronously.
     *
     * @param guildName guild name to look up
     * @return future resolving to a guild profile or a {@link LookupError}
     */
    public static CompletableFuture<ApiResult<GuildProfile, LookupError>> fetchGuildProfile(String guildName) {
        if (!isValidGuildName(guildName)) {
            return CompletableFuture.completedFuture(ApiResult.err(LookupError.INVALID_USERNAME));
        }

        String encodedName = URLEncoder.encode(guildName, StandardCharsets.UTF_8).replace("+", "%20");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.minebox.co/guild/" + encodedName))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .handle((response, ex) -> {
                    if (ex != null) {
                        LOGGER.error("Network error while fetching guild '{}'", guildName, ex);
                        return ApiResult.<GuildProfile, LookupError>err(LookupError.NETWORK_ERROR);
                    }
                    try (InputStream body = response.body()) {
                        return switch (response.statusCode()) {
                            case 200 -> readGuildProfile(body, guildName);
                            case 404 -> ApiResult.<GuildProfile, LookupError>err(LookupError.NOT_FOUND);
                            case 403 -> ApiResult.<GuildProfile, LookupError>err(LookupError.PROFILE_PRIVATE);
                            case 429 -> ApiResult.<GuildProfile, LookupError>err(LookupError.RATE_LIMITED);
                            default -> {
                                LOGGER.warn("Minebox API returned HTTP {} for guild '{}'", response.statusCode(), guildName);
                                yield ApiResult.<GuildProfile, LookupError>err(LookupError.SERVER_ERROR);
                            }
                        };
                    } catch (IOException ioException) {
                        LOGGER.error("Error reading API response for guild '{}'", guildName, ioException);
                        return ApiResult.<GuildProfile, LookupError>err(LookupError.NETWORK_ERROR);
                    }
                });
    }

    private static ApiResult<GuildProfile, LookupError> readGuildProfile(InputStream body, String guildName) throws IOException {
        byte[] bytes = body.readNBytes(MAX_RESPONSE_BYTES + 1);
        if (bytes.length > MAX_RESPONSE_BYTES) {
            LOGGER.warn("Minebox API response for guild '{}' exceeded {} bytes; rejected", guildName, MAX_RESPONSE_BYTES);
            return ApiResult.err(LookupError.SERVER_ERROR);
        }
        try {
            GuildProfile guild = GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), GuildProfile.class);
            if (guild == null) {
                return ApiResult.err(LookupError.SERVER_ERROR);
            }
            return ApiResult.ok(guild);
        } catch (com.google.gson.JsonSyntaxException jsonException) {
            LOGGER.warn("Failed to parse Minebox API response for guild '{}'", guildName, jsonException);
            return ApiResult.err(LookupError.SERVER_ERROR);
        }
    }

    /**
     * Reads the response body up to {@link #MAX_RESPONSE_BYTES} and parses it into a profile.
     * Oversized or malformed responses are reported as {@link LookupError#SERVER_ERROR}.
     *
     * @param body     response body stream
     * @param username username being looked up, used for logging
     * @return parsed profile result or a server-error result
     * @throws IOException when the stream cannot be read
     */
    private static ApiResult<PlayerProfile, LookupError> readProfile(InputStream body, String username) throws IOException {
        byte[] bytes = body.readNBytes(MAX_RESPONSE_BYTES + 1);
        if (bytes.length > MAX_RESPONSE_BYTES) {
            LOGGER.warn("Minebox API response for '{}' exceeded {} bytes; rejected", username, MAX_RESPONSE_BYTES);
            return ApiResult.err(LookupError.SERVER_ERROR);
        }
        try {
            PlayerProfile profile = GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), PlayerProfile.class);
            if (profile == null) {
                return ApiResult.err(LookupError.SERVER_ERROR);
            }
            return ApiResult.ok(profile);
        } catch (JsonSyntaxException jsonException) {
            LOGGER.warn("Failed to parse Minebox API response for '{}'", username, jsonException);
            return ApiResult.err(LookupError.SERVER_ERROR);
        }
    }
}
