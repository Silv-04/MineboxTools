package fr.silv.items;

import java.util.*;

import fr.silv.ModConfig;
import org.slf4j.Logger;

import fr.silv.utils.MineboxItemDataUtils;
import fr.silv.utils.MineboxItemStatUtils;
import fr.silv.utils.ModLog;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

/**
 * Renders custom durability bars for supported items.
 */
public class DurabilityBarHandler {
    private static final Logger LOGGER = ModLog.getLogger(DurabilityBarHandler.class);
    private static final long DURABILITY_LOG_THROTTLE_MS = 10_000;

    private static final Map<ItemStack, Integer> durabilityCache = new WeakHashMap<>();
    private static final Map<ItemStack, Integer> nbtHashCache = new WeakHashMap<>();
    private static final Map<String, Map<String, int[]>> statsCache = new HashMap<>();

    private static final List<String> SUPPORTED_PREFIXES = List.of(
            "hammer_", "vein_", "watering_can_", "sponge_", "bucket_",
            "laborer_", "basket_seeds_", "block_stick_", "leaf_blower", "silk_touch_");

    /**
     * Registers command handlers.
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ModConfig.isEnabled(ModConfig.FeatureFlag.DURABILITY)) return;
            ClientPlayerEntity player = client.player;
            if (player != null) {
                Set<ItemStack> scannedItems = Collections.newSetFromMap(new IdentityHashMap<>());
                scanInventory(player.getInventory().getMainStacks(), scannedItems);
                scanItem(player.getOffHandStack(), scannedItems);
                if (player.currentScreenHandler != null) {
                    for (Slot slot : player.currentScreenHandler.slots) {
                        scanItem(slot.getStack(), scannedItems);
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
        if (item == null || item.isEmpty())
            return;
        if (!scannedItems.add(item))
            return;

        NbtComponent nbtComponent = item.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return;

        int nbtHash = nbtComponent.hashCode();
        Integer previousHash = nbtHashCache.get(item);
        if (previousHash != null && previousHash == nbtHash)
            return;
        nbtHashCache.put(item, nbtHash);

        if (MineboxItemDataUtils.isDisplayOnlyItem(item))
            return;

        Optional<MineboxItemDataUtils.PersistentItemData> itemDataOptional = MineboxItemDataUtils.getPersistentItemData(item);
        if (itemDataOptional.isEmpty()) return;
        MineboxItemDataUtils.PersistentItemData itemData = itemDataOptional.get();
        String id = itemData.itemId();
        NbtCompound persistent = itemData.persistentData();

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

            int[] durabilityRange = stats.get("mbx.durability");
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
            if (id.startsWith(prefix))
                return true;
        }
        return id.startsWith("harvester_") &&
                !(id.contains("lumberjack") || id.contains("fisher") || id.contains("miner")
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
        if (previous != null && previous == damage)
            return;

        if (item.get(DataComponentTypes.UNBREAKABLE) != null) {
            item.remove(DataComponentTypes.UNBREAKABLE);
        }

        Integer previousMax = item.get(DataComponentTypes.MAX_DAMAGE);
        if (previousMax == null || previousMax != max) {
            item.set(DataComponentTypes.MAX_DAMAGE, max);
        }
        item.set(DataComponentTypes.DAMAGE, damage);
        durabilityCache.put(item, damage);
    }

    /**
        * Parses current and maximum stored amount from a haversack lore line.
        *
        * @param item haversack item stack
        * @return two-element array [current, max], or {@code null} when not found
     */
    public static String[] getHaverackAmountInside(ItemStack item) {
        LoreComponent loreComponent = item.get(DataComponentTypes.LORE);
        if (loreComponent != null) {
            for (Text lore : loreComponent.lines()) {
                if (lore.getContent() instanceof TranslatableTextContent translatable) {
                    if ("mbx.items.infinite_bag.amount_inside".equals(translatable.getKey())) {
                        Object arg = translatable.getArgs()[0];
                        String text = (arg instanceof Text t) ? t.getString() : (arg instanceof String s ? s : null);
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
