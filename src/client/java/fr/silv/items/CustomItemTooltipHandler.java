package fr.silv.items;

import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.List;
import java.util.Map;

import fr.silv.utils.ItemStatsRangeLoader;
import fr.silv.utils.TooltipStatColor;

public class CustomItemTooltipHandler {

    public static void addStatRangesToTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type,
            List<Text> lines) {
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null) return;
        NbtCompound nbt = nbtComponent.copyNbt();
        if (nbt == null || !nbt.contains("mbitems:id")) return;
        if (nbt.getInt("mbitems:display") == 1) return;
        String itemId = nbt.getString("mbitems:id");
        Map<String, int[]> statRanges = ItemStatsRangeLoader.getStatsFor(itemId);
        if (statRanges.isEmpty())
            return;
        for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);
            TranslatableTextContent content = findTranslatable(line, statRanges);
            if (content != null) {
                String key = content.getKey();
                int[] range = statRanges.get(key);
                String suffix = (range[0] == range[1]) ? " [" + range[0] + "]"
                        : " [" + range[0] + " | " + range[1] + "]";
                Text newLine = line.copy().append(TooltipStatColor.statColor(suffix, key.toUpperCase()));
                lines.set(i, newLine);
            }
        }
    }

    private static TranslatableTextContent findTranslatable(Text text, Map<String, int[]> statRanges) {
        if (text.getContent() instanceof TranslatableTextContent content) {
            if (statRanges.containsKey(content.getKey())) {
                return content;
            }
        }

        for (Text sibling : text.getSiblings()) {
            TranslatableTextContent result = findTranslatable(sibling, statRanges);
            if (result != null)
                return result;
        }
        return null;
    }

}