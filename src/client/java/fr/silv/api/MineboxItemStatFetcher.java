package fr.silv.api;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import fr.silv.utils.MineboxItemStatUtils;
import fr.silv.utils.ModLog;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Fetches the full Minebox item stat catalogue from the API and hands it to
 * {@link MineboxItemStatUtils} for persistence. Triggered manually from the mod menu —
 * never runs automatically. Paginated requests are spaced out to respect the API's
 * ~10 requests/10s rate limit.
 */
public final class MineboxItemStatFetcher {
    private static final Logger LOGGER = ModLog.getLogger(MineboxItemStatFetcher.class);

    private static final String ITEMS_URL = MineboxApiClient.BASE_URL + "items";
    private static final int MAX_RESPONSE_BYTES = 2 * 1024 * 1024;
    private static final long PAGE_DELAY_MS = 1100L;

    /** Minimum time between fetch attempts, to prevent spamming the API from the menu. */
    public static final long COOLDOWN_MS = 3_600_000L;

    private static final String VERSION = FabricLoader.getInstance()
            .getModContainer("mineboxtools")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    private static final String USER_AGENT = "MBT/" + VERSION;

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    /** Lifecycle state of the most recent (or in-progress) fetch. */
    public enum State {
        IDLE, FETCHING, SUCCESS, ERROR
    }

    /** Failure reasons surfaced to the menu UI. */
    public enum FetchError {
        ALREADY_RUNNING, ON_COOLDOWN, NETWORK_ERROR, RATE_LIMITED, SERVER_ERROR, PARSE_ERROR, WRITE_ERROR
    }

    private static volatile State state = State.IDLE;
    private static volatile int currentPage = 0;
    private static volatile int totalPages = 0;
    private static volatile FetchError lastError = null;
    private static volatile long lastAttemptMillis = 0L;

    private MineboxItemStatFetcher() {
    }

    public static State getState() {
        return state;
    }

    public static int getCurrentPage() {
        return currentPage;
    }

    public static int getTotalPages() {
        return totalPages;
    }

    public static FetchError getLastError() {
        return lastError;
    }

    /**
     * Estimated remaining time for the in-progress fetch, based on the fixed delay
     * between page requests. Zero when idle or the page count isn't known yet.
     *
     * @return estimated milliseconds remaining
     */
    public static long estimatedRemainingMs() {
        if (state != State.FETCHING || totalPages <= 0 || currentPage >= totalPages) {
            return 0L;
        }
        return (long) (totalPages - currentPage) * PAGE_DELAY_MS;
    }

    /**
     * Time remaining before another fetch attempt is allowed.
     *
     * @return milliseconds remaining, or 0 when a new attempt is allowed
     */
    public static long remainingCooldownMs() {
        return Math.max(0L, COOLDOWN_MS - (System.currentTimeMillis() - lastAttemptMillis));
    }

    /**
     * Starts a full catalogue fetch, unless one is already running or the cooldown
     * hasn't elapsed since the last attempt.
     *
     * @return future resolving to {@code true} on success; {@code false} on any failure
     *         (including an already-running fetch or an active cooldown, exposed via
     *         {@link #getLastError()})
     */
    public static synchronized CompletableFuture<Boolean> fetchAndApply() {
        if (state == State.FETCHING) {
            return CompletableFuture.completedFuture(false);
        }
        if (remainingCooldownMs() > 0) {
            lastError = FetchError.ON_COOLDOWN;
            state = State.ERROR;
            return CompletableFuture.completedFuture(false);
        }

        lastAttemptMillis = System.currentTimeMillis();
        state = State.FETCHING;
        currentPage = 0;
        totalPages = 0;
        lastError = null;

        Map<String, Map<String, Object>> output = new LinkedHashMap<>();
        return fetchPage(1, output)
                .thenApply(success -> {
                    if (!success) {
                        state = State.ERROR;
                        return false;
                    }
                    boolean written = MineboxItemStatUtils.saveFetchedData(output);
                    if (!written) {
                        lastError = FetchError.WRITE_ERROR;
                        state = State.ERROR;
                        return false;
                    }
                    state = State.SUCCESS;
                    return true;
                })
                .exceptionally(ex -> {
                    LOGGER.error("Unexpected failure while fetching item stats", ex);
                    lastError = FetchError.NETWORK_ERROR;
                    state = State.ERROR;
                    return false;
                });
    }

    private static CompletableFuture<Boolean> fetchPage(int page, Map<String, Map<String, Object>> output) {
        CompletableFuture<HttpResponse<InputStream>> responseFuture = page == 1
                ? sendRequest(page)
                : CompletableFuture
                        .supplyAsync(() -> null, CompletableFuture.delayedExecutor(PAGE_DELAY_MS, TimeUnit.MILLISECONDS))
                        .thenCompose(unused -> sendRequest(page));

        return responseFuture
                .handle((response, ex) -> {
                    if (ex != null) {
                        LOGGER.error("Network error while fetching item stats page {}", page, ex);
                        lastError = FetchError.NETWORK_ERROR;
                        return false;
                    }
                    return handlePageResponse(page, response, output);
                })
                .thenCompose(success -> {
                    if (!Boolean.TRUE.equals(success)) {
                        return CompletableFuture.completedFuture(false);
                    }
                    if (currentPage < totalPages) {
                        return fetchPage(currentPage + 1, output);
                    }
                    return CompletableFuture.completedFuture(true);
                });
    }

    private static CompletableFuture<HttpResponse<InputStream>> sendRequest(int page) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ITEMS_URL + "?page=" + page))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();
        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
    }

    private static boolean handlePageResponse(int page, HttpResponse<InputStream> response,
                                               Map<String, Map<String, Object>> output) {
        try (InputStream body = response.body()) {
            if (response.statusCode() == 429) {
                lastError = FetchError.RATE_LIMITED;
                return false;
            }
            if (response.statusCode() != 200) {
                LOGGER.warn("Minebox API returned HTTP {} for items page {}", response.statusCode(), page);
                lastError = FetchError.SERVER_ERROR;
                return false;
            }

            byte[] bytes = body.readNBytes(MAX_RESPONSE_BYTES + 1);
            if (bytes.length > MAX_RESPONSE_BYTES) {
                LOGGER.warn("Minebox API response for items page {} exceeded {} bytes; rejected", page, MAX_RESPONSE_BYTES);
                lastError = FetchError.SERVER_ERROR;
                return false;
            }

            ItemsPageResponse parsed = GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), ItemsPageResponse.class);
            if (parsed == null || parsed.items == null) {
                lastError = FetchError.PARSE_ERROR;
                return false;
            }

            if (totalPages == 0) {
                int pageSize = Math.max(1, parsed.pageSize);
                totalPages = (int) Math.ceil((double) parsed.total / pageSize);
            }
            currentPage = parsed.page;

            for (ApiItem item : parsed.items) {
                if (item.id == null) {
                    continue;
                }
                Map<String, Object> data = new LinkedHashMap<>();
                if (item.stats != null) {
                    for (Map.Entry<String, List<Integer>> entry : item.stats.entrySet()) {
                        data.put("mbx.stats." + entry.getKey().toLowerCase(), entry.getValue());
                    }
                }
                data.put("mbx.durability", item.durability);
                output.put(item.id, data);
            }
            return true;
        } catch (IOException e) {
            LOGGER.error("Error reading item stats page {}", page, e);
            lastError = FetchError.NETWORK_ERROR;
            return false;
        } catch (com.google.gson.JsonSyntaxException e) {
            LOGGER.warn("Failed to parse item stats page {}", page, e);
            lastError = FetchError.PARSE_ERROR;
            return false;
        }
    }

    /** Gson DTO for a single page of the {@code /items} endpoint. */
    private static final class ItemsPageResponse {
        List<ApiItem> items;
        int page;
        @SerializedName("pageSize")
        int pageSize;
        int total;
    }

    /** Gson DTO for one item entry within a page. */
    private static final class ApiItem {
        String id;
        String name;
        int durability;
        Map<String, List<Integer>> stats;
    }
}
