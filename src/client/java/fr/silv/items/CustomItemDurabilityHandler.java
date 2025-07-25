package fr.silv.items;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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
    private static int tickCounter = 0;
    private static final Map<ItemStack, Integer> durabilityCache = new WeakHashMap<>();

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter % 10 != 0)
                return;
            ClientPlayerEntity player = client.player;
            if (player != null) {
                scanInventory(player.getInventory().main);

                scanItem(player.getInventory().offHand.get(0));

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
        if (nbtComponent == null)
            return;

        NbtCompound nbt = nbtComponent.copyNbt();
        if (nbt == null)
            return;

        String id = nbt.getString("mbitems:id");
        NbtCompound persistent = nbt.getCompound("mbitems:persistent");

        if (id.contains("haversack") || id.contains("block_infinite_chest")) {
            String[] amountInside = getHaverackAmountInside(item);
            int currentAmount = Integer.valueOf(amountInside[0]);
            int maxAmount = Integer.valueOf(amountInside[1]);
            applyFakeDurability(item, currentAmount, maxAmount, id);
        }

        if (id.contains("harvester_") || id.contains("hammer") || id.contains("vein") || id.contains("watering_can")
                || id.contains("sponge") || id.contains("bucket") || id.contains("laborer")
                || id.contains("basket_seeds")
                || id.contains("block_stick") || id.contains("leaf_blower")) {

            Integer currentDurability = persistent.getInt("mbitems:durability");
            Integer maxDurability = ItemStatsRangeLoader.getStatsFor(id).get("mbx.durability")[0];
            applyFakeDurability(item, currentDurability, maxDurability, id);
        }
    }

    private static void applyFakeDurability(ItemStack item, Integer currentDamage, Integer maxDamage, String id) {
        if (currentDamage == null || currentDamage <= 0)
            return;
        if (maxDamage == null || maxDamage <= 0 || maxDamage < currentDamage)
            return;

        Integer previous = durabilityCache.get(item);
        if (previous != null && previous.equals(currentDamage)) return;
        if (item.get(DataComponentTypes.UNBREAKABLE) != null) {
            item.remove(DataComponentTypes.UNBREAKABLE);
        }
        item.set(DataComponentTypes.MAX_DAMAGE, maxDamage);
        item.set(DataComponentTypes.DAMAGE, currentDamage);
        durabilityCache.put(item, currentDamage);
    }

    private static String[] getHaverackAmountInside(ItemStack item) {
        LoreComponent loreComponent = item.get(DataComponentTypes.LORE);
        if (loreComponent != null) {
            for (Text lore : loreComponent.lines()) {
                if (lore.getContent() instanceof TranslatableTextContent translatable) {
                    if (translatable.getKey().equals("mbx.items.infinite_bag.amount_inside")) {
                        Object arg = translatable.getArgs()[0];
                        String obj = null;
                        if (arg instanceof Text text) {
                            obj = text.getString();
                        } else if (arg instanceof String str) {
                            obj = str;
                        }
                        if (obj != null && obj.contains("/")) {
                            String[] parts = obj.split("/");
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
