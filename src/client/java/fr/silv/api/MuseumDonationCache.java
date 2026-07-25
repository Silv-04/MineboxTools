package fr.silv.api;

import fr.silv.utils.ModLog;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Caches the local player's donated museum item ids, fetched from their own Minebox profile.
 * Never polls in the background: refreshed only via {@link #refresh()} (world join, and a
 * delayed follow-up after the museum donation screen closes) or {@link #refreshManual()}
 * (the menu button, throttled so spam-clicking can't hammer the API).
 */
public final class MuseumDonationCache {
    private static final Logger LOGGER = ModLog.getLogger(MuseumDonationCache.class);
    private static final long MANUAL_REFRESH_COOLDOWN_MS = 60 * 1000L;

    private static volatile Set<String> donatedItemIds = Set.of();
    private static volatile boolean loaded = false;
    private static volatile long lastManualRefreshMillis = 0L;
    private static final AtomicBoolean fetchInFlight = new AtomicBoolean(false);

    private MuseumDonationCache() {
    }

    /**
     * Indicates whether donation data has been successfully fetched at least once.
     * Highlighting must stay off until this is {@code true} to avoid flagging already-donated
     * items as missing.
     *
     * @return {@code true} once at least one successful fetch has completed
     */
    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * Indicates whether an item id has already been donated to the museum.
     *
     * @param itemId Minebox item identifier
     * @return {@code true} when present in the cached donation list
     */
    public static boolean isDonated(String itemId) {
        return itemId != null && donatedItemIds.contains(itemId);
    }

    /**
     * Returns how many milliseconds remain before {@link #refreshManual()} can fire again.
     *
     * @return remaining cooldown in milliseconds, or {@code 0} when it can fire immediately
     */
    public static long remainingManualCooldownMs() {
        long elapsed = System.currentTimeMillis() - lastManualRefreshMillis;
        return Math.max(0L, MANUAL_REFRESH_COOLDOWN_MS - elapsed);
    }

    /**
     * Outcome of a {@link #refreshManual()} call, used to give the player feedback.
     */
    public enum RefreshOutcome {
        /** A new fetch was dispatched. */
        TRIGGERED,
        /** Skipped because the one-minute manual cooldown hasn't elapsed yet. */
        ON_COOLDOWN,
        /** Skipped because the local player isn't available (not in a world). */
        NO_PLAYER
    }

    /**
     * Requests a refresh with no cooldown, only deduplicated against an in-flight request.
     * Intended for infrequent, event-driven triggers (world join, museum donation screen
     * closing) where every call represents a real reason to have fresh data.
     */
    public static void refresh() {
        fetch();
    }

    /**
     * Requests a refresh from the manual menu button, gated by a one-minute cooldown so
     * spam-clicking can't hammer the API.
     *
     * @return the outcome of this call, so the caller can surface feedback
     */
    public static RefreshOutcome refreshManual() {
        long now = System.currentTimeMillis();
        if (now - lastManualRefreshMillis < MANUAL_REFRESH_COOLDOWN_MS) {
            return RefreshOutcome.ON_COOLDOWN;
        }
        if (Minecraft.getInstance().player == null) {
            return RefreshOutcome.NO_PLAYER;
        }

        lastManualRefreshMillis = now;
        fetch();
        return RefreshOutcome.TRIGGERED;
    }

    private static void fetch() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            LOGGER.info("Museum refresh skipped: no local player");
            return;
        }
        if (!fetchInFlight.compareAndSet(false, true)) {
            LOGGER.info("Museum refresh skipped: a fetch is already in flight");
            return;
        }

        String username = player.getGameProfile().name();
        LOGGER.info("Museum refresh dispatched for '{}'", username);

        MineboxApiClient.fetchPlayerProfile(username)
                .thenAccept(result -> {
                    if (result.isOk()) {
                        PlayerProfile profile = result.getValue();
                        if (profile.data != null && profile.data.objectives != null
                                && profile.data.objectives.museum != null) {
                            donatedItemIds = Set.copyOf(profile.data.objectives.museum);
                            loaded = true;
                            LOGGER.info("Museum refresh succeeded: {} items donated", donatedItemIds.size());
                        } else {
                            LOGGER.warn("Museum refresh succeeded but response had no objectives.museum field");
                        }
                    } else {
                        LOGGER.warn("Museum refresh failed: {}", result.getError());
                    }
                    fetchInFlight.set(false);
                });
    }
}
