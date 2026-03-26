package fr.silv.availability;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.time.LocalTime;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Represents one availability rule entry displayed in the HUD.
 * @param langKey translation key used for the label
 * @param icon icon identifier displayed next to the entry
 * @param section section where this entry is grouped
 * @param enabled supplier returning whether the entry is enabled
 * @param setter consumer used to update the enabled state
 * @param visibilityRule predicate that checks if the entry should be visible
 * @param displayOrder per-slot ordering values used for sorting
 */
public record AvailabilityEntry(
        String langKey,
        Identifier icon,
        AvailabilitySection section,
        BooleanSupplier enabled,
        Consumer<Boolean> setter,
        BiPredicate<World, LocalTime> visibilityRule,
        Map<AvailabilitySlot, Integer> displayOrder
) {
    /**
     * Returns the current enabled state for this entry.
     * The value is read from the user configuration backing this entry.
     *
     * @return {@code true} when this entry is enabled in configuration
     */
    public boolean isEnabled() {
        return enabled.getAsBoolean();
    }

    /**
     * Updates the enabled state for this entry.
     * This is typically called from menu/HUD configuration controls.
     *
     * @param value new enabled state to persist
     */
    public void setEnabled(boolean value) {
        setter.accept(value);
    }

    /**
     * Determines whether this entry should currently be visible.
     * Visibility requires both user activation and a satisfied runtime visibility
     * rule (time slot, weather, moon cycle, etc.).
     *
     * @param world current world state used by visibility predicates
     * @param now current time used by slot-based visibility predicates
     * @return {@code true} when the entry is enabled and currently visible
     */
    public boolean isVisible(World world, LocalTime now) {
        return isEnabled() && visibilityRule.test(world, now);
    }

    /**
     * Checks whether this entry belongs to the requested section.
     *
     * @param expectedSection section to compare against
     * @return {@code true} when this entry is part of the given section
     */
    public boolean isInSection(AvailabilitySection expectedSection) {
        return section == expectedSection;
    }

    /**
     * Returns the display priority for a given availability slot.
     *
     * @param slot slot to retrieve ordering for
     * @return ordering value, or {@code null} if this entry is not present in the slot
     */
    public Integer orderFor(AvailabilitySlot slot) {
        return displayOrder.get(slot);
    }
}
