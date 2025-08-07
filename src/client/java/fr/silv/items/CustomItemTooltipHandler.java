package fr.silv.items;

import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableTextContent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.silv.utils.ItemStatsRangeLoader;
import fr.silv.utils.TooltipStatColor;

public class CustomItemTooltipHandler {
    private static final Logger CustomItemDurabilityHandlerLogger = LogManager
            .getLogger(CustomItemTooltipHandler.class);
    private static final Map<ItemStack, Long> recentLogs = new WeakHashMap<>();
    private static final long LOG_THROTTLE_MS = 5000;

    private static final Map<String, Integer> STAT_WEIGHTS = Map.of(
            "mbx.stats.fortune", 15,
            "mbx.stats.agility", 5,
            "mbx.stats.strength", 5,
            "mbx.stats.luck", 7,
            "mbx.stats.intelligence", 9,
            "mbx.stats.wisdom", 12,
            "mbx.stats.defense", 1,
            "mbx.stats.health", 1);

    public static void addStatRangesToTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type,
            List<Text> lines) {

        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null)
            return;

        NbtCompound nbt = nbtComponent.copyNbt();
        if (nbt == null || !nbt.contains("mbitems:id"))
            return;

        if (nbt.getInt("mbitems:display") == 1)
            return;

        NbtCompound persistent = nbt.getCompound("mbitems:persistent");
        if (persistent == null || persistent.toString().equals("{}"))
            return;

        String itemId = nbt.getString("mbitems:id");
        long now = System.currentTimeMillis();
        Long lastLogTime = recentLogs.get(stack);

        boolean canLog = lastLogTime == null || now - lastLogTime > LOG_THROTTLE_MS;

        if (canLog) {
            CustomItemDurabilityHandlerLogger.info("[Tooltip] Processing item: " + itemId);
            recentLogs.put(stack, now);
        }

        Map<String, int[]> statRanges = ItemStatsRangeLoader.getStatsFor(itemId);
        if (statRanges.isEmpty())
            return;

        Set<String> statKeys = statRanges.keySet();
        Map<String, Integer> actualStats = new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);
            Stat stat = extractStatsFromLine(line, statKeys);
            if (stat != null) {
                int[] range = statRanges.get(stat.stat.toLowerCase());
                if (range != null) {
                    StringBuilder suffix = new StringBuilder(" [");
                    suffix.append(range[0]);
                    if (range[0] != range[1]) {
                        suffix.append(" | ")
                                .append(range[1]);
                    }
                    suffix.append("]");

                    Text newLine = line.copy()
                            .append(TooltipStatColor.statColor(suffix.toString(), stat.stat().toUpperCase()));
                    lines.set(i, newLine);

                    actualStats.put(stat.stat.toLowerCase(), stat.value);
                }
            }
        }
        NbtCompound stats = persistent.getCompound("mbitems:stats");
        if (stats == null || stats.toString().equals("{}"))
            return;
            
        if (!actualStats.isEmpty()) {
            int score = (int) computeGlobalStatScore(actualStats, statRanges);
            Style style = getColorFromScore(score);
            Text scoreLine = lines.getFirst().copy()
                    .append(Text.literal(" " + score + "%").setStyle(style.withBold(true)));
            lines.set(0, scoreLine);
            if (canLog) {
                CustomItemDurabilityHandlerLogger
                        .info("[Tooltip] Global score for item " + itemId + ": " + score + "%");
            }
        }
    }

    private record Stat(String stat, Integer value) {
    }

    private static Stat extractStatsFromLine(Text line, Set<String> validKeys) {
        TranslatableTextContent content = findTranslatable(line, validKeys);
        if (content == null)
            return null;

        String key = content.getKey();
        if (!validKeys.contains(key))
            return null;

        List<Text> flat = flattenText(line);

        for (Text segment : flat) {
            if (segment.getContent() instanceof TranslatableTextContent trContent) {
                if (trContent.getKey().equals("mbx.bonus"))
                    continue;
            }

            String raw = segment.getString();
            Matcher matcher = Pattern.compile("-?\\d+").matcher(raw);
            if (matcher.find()) {
                try {
                    int value = Integer.parseInt(matcher.group());
                    return new Stat(key, value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }

    private static List<Text> flattenText(Text text) {
        List<Text> result = new java.util.ArrayList<>();
        result.add(text);
        for (Text sibling : text.getSiblings()) {
            result.addAll(flattenText(sibling));
        }
        return result;
    }

    private static TranslatableTextContent findTranslatable(Text text, Set<String> validKeys) {
        if (text.getContent() instanceof TranslatableTextContent content && validKeys.contains(content.getKey())) {
            return content;
        }
        for (Text sibling : text.getSiblings()) {
            TranslatableTextContent result = findTranslatable(sibling, validKeys);
            if (result != null)
                return result;
        }
        return null;
    }

    private static double computeGlobalStatScore(Map<String, Integer> actualStats, Map<String, int[]> statRanges) {
        double totalWeightedScore = 0;
        double totalWeight = 0;

        for (Map.Entry<String, int[]> entry : statRanges.entrySet()) {
            String statKey = entry.getKey().toLowerCase();
            int min = entry.getValue()[0];
            int max = entry.getValue()[1];
            if (max <= min)
                continue;
            Integer actualValue = actualStats.get(statKey);
            if (actualValue == null) {
                actualValue = 0;
            }
            Integer weight = STAT_WEIGHTS.get(statKey);

            double filledRatio = (actualValue - min) / (double) (max - min);

            totalWeightedScore += filledRatio * weight;
            totalWeight += weight;
        }
        return totalWeight == 0 ? 0 : (totalWeightedScore / totalWeight) * 100;
    }

    public static Style getColorFromScore(int score) {
        score = Math.max(0, Math.min(score, 100));
        int red = (int) (255 * (100 - score) / 100.0);
        int green = (int) (255 * score / 100.0);
        int rgb = (red << 16) | (green << 8);
        return Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(true);
    }
}
