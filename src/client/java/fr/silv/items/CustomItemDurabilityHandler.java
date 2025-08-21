package fr.silv.items;

import java.util.*;

import fr.silv.utils.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.silv.utils.ItemStatsRangeLoader;
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

public class CustomItemDurabilityHandler {
    private static final Logger CustomItemDurabilityHandlerLogger = LogManager
            .getLogger(CustomItemDurabilityHandler.class);

    private static final Map<ItemStack, Integer> durabilityCache = new WeakHashMap<>();
    private static final Map<ItemStack, Integer> nbtHashCache = new WeakHashMap<>();
    private static final Map<String, Map<String, int[]>> statsCache = new HashMap<>();

    private static final List<String> SUPPORTED_PREFIXES = List.of(
            "hammer_", "vein_", "watering_can_", "sponge_", "bucket_",
            "laborer_", "basket_seeds_", "block_stick_", "leaf_blower");

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ModConfig.durabilityToggle) return;
            ClientPlayerEntity player = client.player;
            if (player != null) {
                scanInventory(player.getInventory().getMainStacks());
                scanItem(player.getOffHandStack());
                if (player.currentScreenHandler != null) {
                    for (Slot slot : player.currentScreenHandler.slots) {
                        scanItem(slot.getStack());
                    }
                }
            }
        });
    }

    private static void scanInventory(List<ItemStack> inventory) {
        for (ItemStack item : inventory) {
            scanItem(item);
        }
    }

    private static void scanItem(ItemStack item) {
        if (item == null || item.isEmpty())
            return;

        NbtComponent nbtComponent = item.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return;

        NbtCompound nbt = nbtComponent.copyNbt();

        int nbtHash = nbt.toString().hashCode();
        Integer previousHash = nbtHashCache.get(item);
        if (previousHash != null && previousHash == nbtHash)
            return;
        nbtHashCache.put(item, nbtHash);

        if (nbt.getInt("mbitems:display").isPresent() && nbt.getInt("mbitems:display").get() == 1)
            return;

        if (nbt.getString("mbitems:id").isEmpty()) return;
        String id = nbt.getString("mbitems:id").get();

        if (nbt.getCompound("mbitems:persistent").isEmpty()) return;
        NbtCompound persistent = nbt.getCompound("mbitems:persistent").get();

        if (id.contains("haversack") || id.contains("block_infinite_chest")) {
            String[] amountInside = getHaverackAmountInside(item);
            if (amountInside != null) {
                try {
                    int current = Integer.parseInt(amountInside[0]);
                    int max = Integer.parseInt(amountInside[1]);
                    CustomItemDurabilityHandlerLogger
                            .info("[Durability] Haversack Detected: ID=" + id + " Current=" + current + " Max=" + max);
                    applyDurability(item, current, max);
                } catch (NumberFormatException e) {
                    CustomItemDurabilityHandlerLogger
                            .error("[Durability] Error parsing haversack values: " + Arrays.toString(amountInside));
                }
            }
        } else if (startsWithSupportedPrefix(id)) {
            Map<String, int[]> stats = statsCache.get(id);
            if (stats == null) {
                CustomItemDurabilityHandlerLogger.info("[Durability] Loading stats for: " + id);
                stats = ItemStatsRangeLoader.getStatsFor(id);
                statsCache.put(id, stats);
            }

            int[] durabilityRange = stats.get("mbx.durability");
            if (durabilityRange != null && durabilityRange.length > 0) {
                int max = durabilityRange[0];
                if (persistent.getInt("mbitems:durability").isEmpty()) return;
                int current = persistent.getInt("mbitems:durability").get();
                CustomItemDurabilityHandlerLogger
                        .info("[Durability] Tool Detected: ID=" + id + " Current=" + current + " Max=" + max);
                applyDurability(item, current, max);
            } else {
                CustomItemDurabilityHandlerLogger.error("[Durability] No durability data found for: " + id);
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
            CustomItemDurabilityHandlerLogger
                    .error("[Durability] Invalid durability values: current=" + current + ", max=" + max);
            return;
        }

        Integer previous = durabilityCache.get(item);
        int damage = max - current;
        if (previous != null && previous == damage)
            return;

        if (item.get(DataComponentTypes.UNBREAKABLE) != null) {
            CustomItemDurabilityHandlerLogger.info("[Durability] Removing unbreakable flag for item.");
            item.remove(DataComponentTypes.UNBREAKABLE);
        }

        Integer previousMax = item.get(DataComponentTypes.MAX_DAMAGE);
        if (previousMax == null || previousMax != max) {
            item.set(DataComponentTypes.MAX_DAMAGE, max);
        }
        item.set(DataComponentTypes.DAMAGE, damage);
        durabilityCache.put(item, damage);
        CustomItemDurabilityHandlerLogger
                .info("[Durability] Set durability: " + current + "/" + max + " (Damage=" + damage + ")");
    }

    private static String[] getHaverackAmountInside(ItemStack item) {
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
                                CustomItemDurabilityHandlerLogger.info("[Durability] Parsed haversack lore: " + text);
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
