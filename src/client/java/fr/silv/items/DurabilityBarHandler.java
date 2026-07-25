package fr.silv.items;

import fr.silv.ModConfig;
import fr.silv.utils.MineboxItemDataUtils;
import fr.silv.utils.MineboxItemStatUtils;
import fr.silv.utils.ModLog;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Renders custom durability bars for supported items.
 */
public final class DurabilityBarHandler {
    private static final Logger LOGGER = ModLog.getLogger(DurabilityBarHandler.class);
    private static final long DURABILITY_LOG_THROTTLE_MS = 10_000;
    private static final String DURABILITY_STAT_KEY = "mbx.durability";

    private static final Map<ItemStack, Integer> durabilityCache = new WeakHashMap<>();
    private static final Map<ItemStack, Integer> nbtHashCache = new WeakHashMap<>();
    private static final Map<String, Map<String, int[]>> statsCache = new HashMap<>();

    /**
     * Clears the per-item stat lookup cache. Must be called whenever
     * {@link MineboxItemStatUtils} reloads its data (e.g. after an "Update item data"
     * fetch), otherwise items looked up before the reload keep serving stale
     * (possibly empty) cached results indefinitely.
     */
    public static void clearStatsCache() {
        statsCache.clear();
    }

    private static final List<String> SUPPORTED_PREFIXES = List.of(
            "hammer_", "vein_", "watering_can_", "sponge_", "bucket_",
            "laborer_", "basket_seeds_", "block_stick_", "leaf_blower", "silk_touch_");

    private DurabilityBarHandler() {
    }

    /**
     * Registers the per-tick durability scanning hook.
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ModConfig.isEnabled(ModConfig.FeatureFlag.DURABILITY)) return;
            LocalPlayer player = client.player;
            if (player != null) {
                Set<ItemStack> scannedItems = Collections.newSetFromMap(new IdentityHashMap<>());
                scanInventory(player.getInventory().getNonEquipmentItems(), scannedItems);
                scanItem(player.getOffhandItem(), scannedItems);
                if (player.containerMenu != null) {
                    for (Slot slot : player.containerMenu.slots) {
                        scanItem(slot.getItem(), scannedItems);
                    }
                }
            }
        });
    }

    private static void scanInventory(List<ItemStack> inventory, Set<ItemStack> scannedItems) {
        for (ItemStack item : inventory) {
            scanItem(item, scannedItems);
        }
    }

    private static void scanItem(ItemStack item, Set<ItemStack> scannedItems) {
        if (item == null || item.isEmpty()) return;
        if (!scannedItems.add(item)) return;

        CustomData customData = item.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;

        int nbtHash = customData.hashCode();
        Integer previousHash = nbtHashCache.get(item);
        if (previousHash != null && previousHash == nbtHash) return;
        nbtHashCache.put(item, nbtHash);

        if (MineboxItemDataUtils.isDisplayOnlyItem(item)) return;

        Optional<MineboxItemDataUtils.PersistentItemData> itemDataOptional = MineboxItemDataUtils.getPersistentItemData(item);
        if (itemDataOptional.isEmpty()) return;
        MineboxItemDataUtils.PersistentItemData itemData = itemDataOptional.get();
        String id = itemData.itemId();
        CompoundTag persistent = itemData.persistentData();

        if (id.contains("haversack") || id.contains("block_infinite_chest")) {
            String[] amountInside = getHaverackAmountInside(item);
            if (amountInside != null) {
                try {
                    int current = Integer.parseInt(amountInside[0]);
                    int max = Integer.parseInt(amountInside[1]);
                    applyDurability(item, current, max);
                } catch (NumberFormatException e) {
                    ModLog.warnThrottled(LOGGER, "durability:haversack-parse", DURABILITY_LOG_THROTTLE_MS,
                            "Failed to parse haversack values {}", Arrays.toString(amountInside), e);
                }
            }
        } else if (startsWithSupportedPrefix(id)) {
            Map<String, int[]> stats = statsCache.computeIfAbsent(id, MineboxItemStatUtils::getStatsFor);

            int[] durabilityRange = stats.get(DURABILITY_STAT_KEY);
            if (durabilityRange != null && durabilityRange.length > 0) {
                int max = durabilityRange[0];
                Optional<Integer> currentOptional = MineboxItemDataUtils.getCurrentDurability(persistent);
                if (currentOptional.isEmpty()) return;
                int current = currentOptional.get();
                applyDurability(item, current, max);
            } else {
                ModLog.warnThrottled(LOGGER, "durability:missing-max:" + id, DURABILITY_LOG_THROTTLE_MS,
                        "No durability data found for item '{}'", id);
            }
        }
    }

    private static boolean startsWithSupportedPrefix(String id) {
        for (String prefix : SUPPORTED_PREFIXES) {
            if (id.startsWith(prefix)) return true;
        }
        return id.startsWith("harvester_")
                && !(id.contains("lumberjack") || id.contains("fisher") || id.contains("miner")
                || id.contains("alchemist"));
    }

    private static void applyDurability(ItemStack item, int current, int max) {
        if (current < 0 || max <= 0 || max < current) {
            ModLog.warnThrottled(LOGGER, "durability:invalid-values:" + current + ":" + max, DURABILITY_LOG_THROTTLE_MS,
                    "Invalid durability values current={} max={}", current, max);
            return;
        }

        Integer previous = durabilityCache.get(item);
        int damage = max - current;
        if (previous != null && previous == damage) return;

        if (item.get(DataComponents.UNBREAKABLE) != null) {
            item.remove(DataComponents.UNBREAKABLE);
        }

        Integer previousMax = item.get(DataComponents.MAX_DAMAGE);
        if (previousMax == null || previousMax != max) {
            item.set(DataComponents.MAX_DAMAGE, max);
        }
        item.set(DataComponents.DAMAGE, damage);
        durabilityCache.put(item, damage);
    }

    /**
     * Returns the fill fraction (0.0–1.0) for an item's custom durability, or empty
     * when the item is not managed by this handler.
     *
     * @param stack item stack to evaluate
     * @return fill fraction when available
     */
    public static OptionalDouble computeDurabilityFraction(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return OptionalDouble.empty();

        Optional<String> itemIdOpt = MineboxItemDataUtils.getItemId(stack);
        if (itemIdOpt.isEmpty()) return OptionalDouble.empty();
        String id = itemIdOpt.get();

        if (id.contains("haversack") || id.contains("block_infinite_chest")) {
            String[] amounts = getHaverackAmountInside(stack);
            if (amounts == null) return OptionalDouble.empty();
            try {
                int current = Integer.parseInt(amounts[0].trim());
                int max = Integer.parseInt(amounts[1].trim());
                if (max <= 0) return OptionalDouble.empty();
                return OptionalDouble.of((double) Math.min(current, max) / max);
            } catch (NumberFormatException e) {
                return OptionalDouble.empty();
            }
        }

        if (!startsWithSupportedPrefix(id)) return OptionalDouble.empty();
        if (MineboxItemDataUtils.isDisplayOnlyItem(stack)) return OptionalDouble.empty();

        Optional<Integer> currentDur = MineboxItemDataUtils.getCurrentDurability(stack);
        Optional<Integer> maxDur = MineboxItemDataUtils.getMaxDurability(id);
        if (currentDur.isEmpty() || maxDur.isEmpty()) return OptionalDouble.empty();

        int current = currentDur.get();
        int max = maxDur.get();
        if (max <= 0) return OptionalDouble.empty();
        return OptionalDouble.of((double) Math.min(current, max) / max);
    }

    /**
     * Parses current and maximum stored amount from a haversack lore line.
     *
     * @param item haversack item stack
     * @return two-element array [current, max], or {@code null} when not found
     */
    public static String[] getHaverackAmountInside(ItemStack item) {
        ItemLore lore = item.get(DataComponents.LORE);
        if (lore != null) {
            for (Component line : lore.lines()) {
                if (line.getContents() instanceof TranslatableContents translatable) {
                    if ("mbx.items.infinite_bag.amount_inside".equals(translatable.getKey())) {
                        Object arg = translatable.getArgs()[0];
                        String text = (arg instanceof Component t) ? t.getString() : (arg instanceof String s ? s : null);
                        if (text != null && text.contains("/")) {
                            String[] parts = text.split("/");
                            if (parts.length == 2) {
                                return parts;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
