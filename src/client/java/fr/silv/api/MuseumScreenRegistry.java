package fr.silv.api;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which currently-open container ids were fingerprinted as the museum donation screen.
 *
 * <p>Populated from {@code ClientboundContainerSetContentPacket} (network layer, where the
 * screen's real slot contents are available) and consumed from {@code ScreenEvents.remove}
 * (client Screen layer, where a player-initiated close reliably fires) since neither layer
 * alone can both identify and detect the close of that specific screen.
 */
public final class MuseumScreenRegistry {
    private static final ConcurrentHashMap<Integer, Boolean> OPEN_MUSEUM_SCREENS = new ConcurrentHashMap<>();

    private MuseumScreenRegistry() {
    }

    /**
     * Records whether a container id was fingerprinted as the museum donation screen.
     *
     * @param containerId          network container id from the content packet
     * @param isMuseumDonationScreen {@code true} when the slot fingerprint matched
     */
    public static void markScreen(int containerId, boolean isMuseumDonationScreen) {
        OPEN_MUSEUM_SCREENS.put(containerId, isMuseumDonationScreen);
    }

    /**
     * Removes and returns whether a container id was fingerprinted as the museum donation
     * screen, intended to be called once when that screen closes client-side.
     *
     * @param containerId container id to look up
     * @return {@code true} when this container id was the museum donation screen
     */
    public static boolean consumeAndCheck(int containerId) {
        return Boolean.TRUE.equals(OPEN_MUSEUM_SCREENS.remove(containerId));
    }
}
