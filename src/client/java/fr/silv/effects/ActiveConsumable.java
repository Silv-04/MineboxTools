package fr.silv.effects;

import java.util.Optional;

/**
 * One effect currently active on the player, as read from the in-game effects menu.
 *
 * <p>Only the volatile, per-instance data is stored here: which effect it is and when it ends. The
 * permanent description (stats, icon, base duration) is <em>not</em> duplicated - it is looked up on
 * demand from {@link EffectCatalogService} using {@link #sourceType()}, so the two sources of truth
 * stay separate.
 *
 * @param sourceType          the menu item's {@code mythicmobs:type} (matches a {@link CatalogEffect}
 *                            id), or {@code null} when the menu item carries none (e.g. beacon buffs)
 * @param displayName         the menu item's display name, kept as a language-correct fallback label
 * @param expiresAtEpochMillis wall-clock instant the effect ends, computed from the menu's remaining
 *                            time at read time so the countdown can run locally without re-scanning
 */
public record ActiveConsumable(String sourceType, String displayName, long expiresAtEpochMillis) {

    /** Milliseconds left until this effect ends, clamped at zero. */
    public long remainingMillis() {
        return Math.max(0L, expiresAtEpochMillis - System.currentTimeMillis());
    }

    /** Whole seconds left until this effect ends, rounded up so it reads "1s" until it truly hits zero. */
    public long remainingSeconds() {
        return (remainingMillis() + 999L) / 1000L;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAtEpochMillis;
    }

    /** The permanent catalog entry for this effect, when its source id is known and in the catalog. */
    public Optional<CatalogEffect> catalogEntry() {
        return sourceType == null ? Optional.empty() : EffectCatalogService.getByKey(sourceType);
    }
}
