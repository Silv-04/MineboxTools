package fr.silv.items;

import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;

import fr.silv.utils.ItemStatsRangeLoader;

public class CustomItemTooltipHandler {

    public static void addStatRangesToTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type,
            List<Text> lines) {

        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null)
            return;

        NbtCompound nbt = nbtComponent.copyNbt();
        if (nbt == null)
            return;

        String itemId = nbt.getString("mbitems:id");

        Map<String, int[]> statRanges = ItemStatsRangeLoader.getStatsFor(itemId);
        if (statRanges.isEmpty())
            return;

        for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);
            String plainText = line.getString();

            for (Map.Entry<String, int[]> entry : statRanges.entrySet()) {
                String translationKey = entry.getKey();
                String translated = Text.translatable(translationKey).getString();

                if (plainText.contains(translated)) {
                    int[] range = entry.getValue();
                    String suffix = (range[0] == range[1]) ? " [" + range[0] + "]"
                            : " [" + range[0] + " to " + range[1] + "]";
                    Text newLine = line.copy().append(Text.literal(suffix).formatted(Formatting.GRAY));
                    lines.set(i, newLine);
                    break;
                }
            }
        }
    }
}