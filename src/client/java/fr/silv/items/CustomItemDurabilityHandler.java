package fr.silv.items;

import java.util.List;

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
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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

        if (id.contains("haversack") || id.contains("block_infinite_chest")) {
            float ratio = handleHaversackDurability(item);
            applyFakeDurability(item, ratio);
        }

        if (id.contains("harvester_") || id.contains("hammer") || id.contains("vein") || id.contains("watering_can")
                || id.contains("sponge") || id.contains("bucket") || id.contains("laborer") || id.contains("basket_seeds")) {
            float ratio = handleHarvesterDurability(item);
            applyFakeDurability(item, ratio);
        }
    }

    private static void applyFakeDurability(ItemStack item, float ratio) {
        if (ratio < 0f || ratio > 1f)
            return;

        int fakeMaxDamage = 1000;
        int fakeDamage = (int) (fakeMaxDamage * (1 - ratio));

        item.set(DataComponentTypes.MAX_DAMAGE, fakeMaxDamage);
        item.set(DataComponentTypes.DAMAGE, fakeDamage);
        item.remove(DataComponentTypes.UNBREAKABLE);
    }

    private static float handleHaversackDurability(ItemStack item) {
        LoreComponent loreComponent = item.get(DataComponentTypes.LORE);
        if (loreComponent != null) {
            for (Text lore : loreComponent.lines()) {
                if (lore.getContent() instanceof TranslatableTextContent translatable) {
                    if (translatable.getKey().equals("mbx.items.infinite_bag.amount_inside")) {
                        return parseDurabilityRatio(translatable.getArgs(), null);
                    }
                }
            }
        }
        return -1f;
    }

    private static float handleHarvesterDurability(ItemStack item) {
        LoreComponent loreComponent = item.get(DataComponentTypes.LORE);
        String harvesterID = item.get(DataComponentTypes.CUSTOM_DATA).copyNbt().getString("mbitems:id");
        if (loreComponent != null) {
            for (Text lore : loreComponent.lines()) {
                if (lore.getContent() instanceof TranslatableTextContent translatable) {
                    if (translatable.getKey().equals("mbx.durability")) {
                        return parseDurabilityRatio(translatable.getArgs(), harvesterID);
                    }
                }
            }
        }
        return -1f;
    }

    private static float parseDurabilityRatio(Object[] args, String harvesterID) {
        if (args.length > 0) {
            Object arg = args[0];
            String durabilityRatio = null;

            if (arg instanceof Text text) {
                durabilityRatio = text.getString();
            } else if (arg instanceof String str) {
                durabilityRatio = str;
            }

            if (durabilityRatio != null && durabilityRatio.contains("/")) {
                String[] parts = durabilityRatio.split("/");
                if (parts.length == 2) {
                    try {
                        float current = Float.parseFloat(parts[0]);
                        float max = Float.parseFloat(parts[1]);
                        return current / max;
                    } catch (NumberFormatException e) {
                        return -1f;
                    }
                }
            } else {
                if (durabilityRatio != null && durabilityRatio.contains("Non-repairable")) {
                    String[] parts = durabilityRatio.split(" ");
                    if (parts.length == 2) {
                        try {
                            float current = Float.parseFloat(parts[0]);
                            int max = ItemStatsRangeLoader.getStatsFor(harvesterID).get("mbx.durability")[0];
                            return current / max;
                        } catch (NumberFormatException e) {
                            return -1f;
                        }
                    }
                }
            }
        }
        return -1f;
    }
}
