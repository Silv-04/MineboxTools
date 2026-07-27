package fr.silv.effects;

import fr.silv.utils.MineboxItemDataUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * Turns "the player finished eating/drinking a Minebox consumable" into a scan request.
 *
 * <p>Called from the item-consumption mixin, which fires at human pace, so the filter is kept to two
 * cheap checks: the item must be consumable, and it must be a Minebox item (it carries an
 * {@code mbitems:id}). Plain vanilla food and drink carry no such id and are ignored, so ordinary
 * eating never triggers a scan. The consumed id itself isn't needed - the scan reads the authoritative
 * effect list - it only gates whether a scan is worth doing.
 */
public final class ConsumableConsumeWatcher {
    private ConsumableConsumeWatcher() {
    }

    public static void onConsume(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.has(DataComponents.CONSUMABLE)) {
            return;
        }
        if (MineboxItemDataUtils.getItemId(stack).isEmpty()) {
            return; // not a Minebox item - plain food/drink
        }
        EffectScanController.requestScan();
    }
}
