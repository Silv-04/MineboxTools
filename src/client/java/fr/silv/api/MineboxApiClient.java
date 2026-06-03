package fr.silv.api;

import com.google.gson.Gson;
import fr.silv.utils.ModLog;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Async HTTP client for the Minebox REST API.
 * All requests are non-blocking; results are delivered via {@link CompletableFuture}.
 */
public final class MineboxApiClient {
    private static final Logger LOGGER = ModLog.getLogger(MineboxApiClient.class);

    /** Base URL of the Minebox API. */
    public static final String BASE_URL = "https://api.minebox.co/";

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private MineboxApiClient() {
    }

    private static PlayerProfile parseProfile(String body) {
        return GSON.fromJson(body, PlayerProfile.class);
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
        NETWORK_ERROR
    }

    /**
     * Fetches a player profile asynchronously.
     * The future always completes normally; errors are wrapped in {@link ApiResult#err}.
     *
     * @param username in-game username to look up
     * @return future resolving to a profile on success or a {@link LookupError} on failure
     */
    public static CompletableFuture<ApiResult<PlayerProfile, LookupError>> fetchPlayerProfile(String username) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "data/" + username))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((response, ex) -> {
                    if (ex != null) {
                        LOGGER.error("Network error while fetching profile for '{}'", username, ex);
                        return ApiResult.<PlayerProfile, LookupError>err(LookupError.NETWORK_ERROR);
                    }
                    return switch (response.statusCode()) {
                        case 200 -> ApiResult.ok(parseProfile(response.body()));
                        case 404 -> ApiResult.<PlayerProfile, LookupError>err(LookupError.NOT_FOUND);
                        case 403 -> ApiResult.<PlayerProfile, LookupError>err(LookupError.PROFILE_PRIVATE);
                        case 429 -> ApiResult.<PlayerProfile, LookupError>err(LookupError.RATE_LIMITED);
                        default -> {
                            LOGGER.warn("Minebox API returned HTTP {} for '{}'", response.statusCode(), username);
                            yield ApiResult.<PlayerProfile, LookupError>err(LookupError.SERVER_ERROR);
                        }
                    };
                });
    }
}
