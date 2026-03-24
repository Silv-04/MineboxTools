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
     * Checks whether enabled.
     * @return true if the condition is met; otherwise false
     */
    public boolean isEnabled() {
        return enabled.getAsBoolean();
    }

    /**
     * Updates the enabled.
     * @param value value for value
     */
    public void setEnabled(boolean value) {
        setter.accept(value);
    }

    /**
     * Checks whether visible.
     * @param world value for world
     * @param now value for now
     * @return true if the condition is met; otherwise false
     */
    public boolean isVisible(World world, LocalTime now) {
        return isEnabled() && visibilityRule.test(world, now);
    }

    /**
     * Checks whether in section.
     * @param expectedSection value for expectedSection
     * @return true if the condition is met; otherwise false
     */
    public boolean isInSection(AvailabilitySection expectedSection) {
        return section == expectedSection;
    }

    /**
     * Executes the order for operation.
     * @param slot value for slot
     * @return the computed order for value
     */
    public Integer orderFor(AvailabilitySlot slot) {
        return displayOrder.get(slot);
    }
}
