package fr.silv.items;

import fr.silv.ModConfig;
import fr.silv.constants.StatValue;
import fr.silv.model.MineboxItem;
import fr.silv.model.MineboxStat;
import fr.silv.utils.MineboxItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.silv.utils.MineboxItemStatUtils;
import fr.silv.utils.StatTextUtils;

public class TooltipHandler {
    private static final Logger TooltipHandlerLogger = LogManager
            .getLogger(TooltipHandler.class);
    private static final Map<ItemStack, Long> recentLogs = new WeakHashMap<>();
    private static final long LOG_THROTTLE_MS = 5000;

    private static final Map<String, Integer> STAT_WEIGHTS = Map.of(
            "mbx.stats.fortune", StatValue.FORTUNE,
            "mbx.stats.agility", StatValue.AGILITY,
            "mbx.stats.strength", StatValue.STRENGTH,
            "mbx.stats.luck", StatValue.LUCK,
            "mbx.stats.intelligence", StatValue.INTELLIGENCE,
            "mbx.stats.wisdom", StatValue.WISDOM,
            "mbx.stats.defense", StatValue.DEFENSE,
            "mbx.stats.health", StatValue.HEALTH);

    public static void addStatRangesToTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type,
                                              List<Text> lines) {
        if (!ModConfig.tooltipToggle) return;

        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null)
            return;

        NbtCompound nbt = nbtComponent.copyNbt();
        if (!nbt.contains("mbitems:id"))
            return;

        if (nbt.getInt("mbitems:display").isPresent() && nbt.getInt("mbitems:display").get() == 1)
            return;

        if (nbt.getCompound("mbitems:persistent").isEmpty())
            return;

        NbtCompound persistent = nbt.getCompound("mbitems:persistent").get();
        if (persistent.toString().equals("{}"))
            return;

        if (nbt.getString("mbitems:id").isEmpty()) return;
        String itemId = nbt.getString("mbitems:id").get();
        long now = System.currentTimeMillis();
        Long lastLogTime = recentLogs.get(stack);

        boolean canLog = lastLogTime == null || now - lastLogTime > LOG_THROTTLE_MS;

        if (canLog) {
            TooltipHandlerLogger.info("[Tooltip] Processing item: " + itemId);
            recentLogs.put(stack, now);
        }

        Map<String, int[]> statRanges = MineboxItemStatUtils.getStatsFor(itemId);
        if (statRanges.isEmpty())
            return;

        Set<String> statKeys = statRanges.keySet();
        Map<String, Integer> actualStats = new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);
            MineboxStat stat = MineboxItemStatUtils.extractStatsFromLine(line, statKeys);
            if (stat != null) {
                int[] range = statRanges.get(stat.getStat().toLowerCase());
                if (range != null) {
                    StringBuilder suffix = new StringBuilder(" [");
                    suffix.append(range[0]);
                    if (range[0] != range[1]) {
                        suffix.append(" | ")
                                .append(range[1]);
                    }
                    suffix.append("]");

                    Text newLine = line.copy()
                            .append(StatTextUtils.statColor(suffix.toString(), stat.getStat().toUpperCase()));
                    lines.set(i, newLine);

                    actualStats.put(stat.getStat().toLowerCase(), stat.getValue());
                }
            }
        }
        if (persistent.getCompound("mbitems:stats").isEmpty())
            return;
        NbtCompound stats = persistent.getCompound("mbitems:stats").get();
        if (stats.toString().equals("{}"))
            return;

        if (!actualStats.isEmpty()) {
            int score = (int) computeGlobalStatScore(actualStats, statRanges);
            Style style = getColorFromScore(score);
            Text scoreLine = lines.getFirst().copy()
                    .append(Text.literal(" " + score + "%").setStyle(style.withBold(true)));
            lines.set(0, scoreLine);
            if (canLog) {
                TooltipHandlerLogger
                        .info("[Tooltip] Global score for item " + itemId + ": " + score + "%");
            }
        }
    }

    public static void addInfoToTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type,
                                        List<Text> lines) {
        NbtComponent nbtComponent = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbtComponent == null)
            return;
        NbtCompound nbt = nbtComponent.copyNbt();
        String itemId = nbt.getString("mbitems:id").orElse("");
        if (itemId.isEmpty())
            return;
        MineboxItem item = MineboxItemUtils.get(itemId);

        if (item != null) {
            lines.add(Text.literal("§6Location:"));
            for (String location : item.getLocation()) {
                lines.add(Text.literal("§7- " + location));
            }
            String condition = item.getCondition();
            if (!condition.isEmpty()) {
                lines.add(Text.literal("§eCondition: " + item.getCondition()));
            }
        }
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