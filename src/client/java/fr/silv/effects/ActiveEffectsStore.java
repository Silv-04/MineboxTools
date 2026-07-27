package fr.silv.effects;

import java.util.List;

/**
 * Holds the set of effects last read from the effects menu.
 *
 * <p>Written from the client thread by {@link EffectScanController} after each silent scan and read
 * from anywhere (commands, future HUD). The reference is swapped wholesale, so readers always see a
 * complete, immutable snapshot; {@link #getActive()} additionally drops entries whose local
 * countdown has already elapsed, so callers never see an expired effect even between scans.
 */
public final class ActiveEffectsStore {
    private static volatile List<ActiveConsumable> current = List.of();
    private static volatile long lastScanEpochMillis = 0L;

    private ActiveEffectsStore() {
    }

    /** Replaces the tracked set with a fresh scan result. */
    public static void update(List<ActiveConsumable> effects) {
        current = List.copyOf(effects);
        lastScanEpochMillis = System.currentTimeMillis();
    }

    /** The still-running effects, expired ones filtered out. */
    public static List<ActiveConsumable> getActive() {
        return current.stream().filter(effect -> !effect.isExpired()).toList();
    }

    /** When the last scan completed, or {@code 0} if none has run this session. */
    public static long lastScanEpochMillis() {
        return lastScanEpochMillis;
    }
}
