package fr.silv.items;

import fr.silv.Lang;
import fr.silv.ModConfig;
import fr.silv.constants.StatDefinition;
import fr.silv.model.MineboxItem;
import fr.silv.model.MineboxStat;
import fr.silv.utils.MineboxItemDataUtils;
import fr.silv.utils.MineboxItemStatUtils;
import fr.silv.utils.MineboxItemUtils;
import fr.silv.utils.ModLog;
import fr.silv.utils.StatTextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableTextContent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Builds and injects custom item tooltip sections.
 */
public class TooltipHandler {
    private static final Logger LOGGER = ModLog.getLogger(TooltipHandler.class);
    private static final String TOOLTIP_BULLET = "- ";

    /**
     * Adds stat ranges to tooltip.
        *
        * @param stack item stack being rendered
        * @param context tooltip rendering context
        * @param type tooltip type provided by Minecraft
        * @param lines mutable tooltip line list
     */
    public static void addStatRangesToTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type,
                                              List<Text> lines) {
        if (!ModConfig.isEnabled(ModConfig.FeatureFlag.TOOLTIP)) {
            return;
        }

        Optional<MineboxItemDataUtils.PersistentItemData> itemDataOptional = MineboxItemDataUtils.getPersistentItemData(stack);
        if (itemDataOptional.isEmpty()) {
            return;
        }

        MineboxItemDataUtils.PersistentItemData itemData = itemDataOptional.get();
        if (MineboxItemDataUtils.isDisplayOnlyItem(itemData.customData())) {
            return;
        }

        NbtCompound persistent = itemData.persistentData();
        String itemId = itemData.itemId();

        Map<String, int[]> statRanges = MineboxItemStatUtils.getStatsFor(itemId);
        if (statRanges.isEmpty()) {
            return;
        }

        Set<String> statKeys = statRanges.keySet();
        Map<String, Integer> actualStats = new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);
            MineboxStat stat = MineboxItemStatUtils.extractStatsFromLine(line, statKeys);
            if (stat == null) {
                continue;
            }

            int[] range = statRanges.get(stat.getStat().toLowerCase());
            if (range == null) {
                continue;
            }

            StringBuilder suffix = new StringBuilder(" [");
            suffix.append(range[0]);
            if (range[0] != range[1]) {
                suffix.append(" | ").append(range[1]);
            }
            suffix.append("]");

            Text newLine = line.copy().append(StatTextUtils.statColor(suffix.toString(), stat.getStat()));
            lines.set(i, newLine);
            actualStats.put(stat.getStat().toLowerCase(), stat.getValue());
        }

        if (MineboxItemDataUtils.getStatsData(persistent).isEmpty() || actualStats.isEmpty()) {
            return;
        }

        int score = (int) computeGlobalStatScore(actualStats, statRanges);
        Style style = getColorFromScore(score);
        Text scoreLine = lines.getFirst().copy()
                .append(Text.literal(" " + score + "%").setStyle(style.withBold(true)));
        lines.set(0, scoreLine);
    }

    /**
     * Adds info to tooltip.
        *
        * @param stack item stack being rendered
        * @param context tooltip rendering context
        * @param type tooltip type provided by Minecraft
        * @param lines mutable tooltip line list
     */
    public static void addInfoToTooltip(ItemStack stack, Item.TooltipContext context, TooltipType type,
                                        List<Text> lines) {
        if (!ModConfig.isEnabled(ModConfig.FeatureFlag.LOCATION)) {
            return;
        }

        MineboxItem item = resolveItem(stack);
        if (item == null) {
            return;
        }

        int seeMoreIndex = findIndexOfTranslateKey(lines, "mbx.see_more");
        Text seeMoreText = Text.literal("");

        if (seeMoreIndex != -1) {
            seeMoreText = lines.remove(seeMoreIndex);
        } else {
            seeMoreIndex = findIndexOfTranslateKey(lines, "mbx.actions.open");
            if (seeMoreIndex != -1) {
                seeMoreText = lines.remove(seeMoreIndex);
            }
        }

        lines.add(sectionTitle("mineboxtools.menu.tooltip.location", 0xFFA500));
        for (String location : item.getLocation()) {
            lines.add(detailLine(location));
        }

        String condition = item.getCondition();
        if (!condition.isEmpty()) {
            lines.add(Text.literal(""));
            lines.add(sectionTitle("mineboxtools.menu.tooltip.condition", 0xFFFF00));
            lines.add(detailLine(condition));
        }

        String boost = item.getBoost();
        if (!boost.isEmpty()) {
            lines.add(Text.literal(""));
            lines.add(sectionTitle("mineboxtools.menu.tooltip.boost", 0x00FF00));
            lines.add(detailLine(boost));
        }

        if (seeMoreIndex != -1) {
            lines.add(Text.literal(""));
            lines.add(seeMoreText);
        }
    }

    private static MineboxItem resolveItem(ItemStack stack) {
        String itemIdFromNbt = MineboxItemDataUtils.getItemId(stack).orElse("");
        String itemIdFromName = "";

        Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
        if (customName != null) {
            for (Text sibling : customName.getSiblings()) {
                if (sibling.getContent() instanceof TranslatableTextContent translatable
                        && translatable.getKey().startsWith("mbx.items.")) {
                    itemIdFromName = translatable.getKey()
                            .replace("mbx.items.", "")
                            .replace(".name", "");
                    break;
                }
            }
        }

        MineboxItem directMatch = MineboxItemUtils.get(itemIdFromNbt);
        return directMatch != null ? directMatch : MineboxItemUtils.get(itemIdFromName);
    }

    private static Text sectionTitle(String translationKey, int color) {
        return Text.literal(Lang.get(translationKey))
                .setStyle(Style.EMPTY.withColor(color).withBold(true));
    }

    private static Text detailLine(String translationKey) {
        return Text.literal(TOOLTIP_BULLET + Lang.get(translationKey))
                .setStyle(Style.EMPTY.withColor(0xFFFFFF));
    }

    private static double computeGlobalStatScore(Map<String, Integer> actualStats, Map<String, int[]> statRanges) {
        double totalWeightedScore = 0;
        double totalWeight = 0;

        for (Map.Entry<String, int[]> entry : statRanges.entrySet()) {
            String statKey = entry.getKey().toLowerCase();
            int min = entry.getValue()[0];
            int max = entry.getValue()[1];
            if (max <= min) {
                continue;
            }

            int actualValue = actualStats.getOrDefault(statKey, 0);
            Optional<StatDefinition> definition = StatDefinition.fromKey(statKey);
            if (definition.isEmpty()) {
                ModLog.warnThrottled(LOGGER, "tooltip:missing-weight:" + statKey, 10_000,
                        "Missing stat weight for key '{}'", statKey);
                continue;
            }

            int weight = definition.get().weight();
            double filledRatio = (actualValue - min) / (double) (max - min);
            totalWeightedScore += filledRatio * weight;
            totalWeight += weight;
        }

        return totalWeight == 0 ? 0 : (totalWeightedScore / totalWeight) * 100;
    }

    /**
        * Computes a red-to-green color gradient based on a normalized score.
        *
        * @param score score in the inclusive range 0-100
        * @return bold text style using the computed gradient color
     */
    public static Style getColorFromScore(int score) {
        score = Math.max(0, Math.min(score, 100));
        int red = (int) (255 * (100 - score) / 100.0);
        int green = (int) (255 * score / 100.0);
        int rgb = (red << 16) | (green << 8);
        return Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(true);
    }

    /**
        * Recursively checks whether a text tree contains a specific translation key.
        *
        * @param text root text node to inspect
        * @param keyToFind translation key to search for
        * @return {@code true} when the key is present in this node or descendants
     */
    public static boolean containsTranslateKey(Text text, String keyToFind) {
        if (text.getContent() instanceof TranslatableTextContent translatable && keyToFind.equals(translatable.getKey())) {
            return true;
        }

        for (Text sibling : text.getSiblings()) {
            if (containsTranslateKey(sibling, keyToFind)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Finds the index of translate key.
        *
        * @param lines tooltip lines to inspect
        * @param keyToFind translation key to search for
        * @return index of the first matching line, or {@code -1} when not found
     */
    public static int findIndexOfTranslateKey(List<Text> lines, String keyToFind) {
        for (int i = 0; i < lines.size(); i++) {
            if (containsTranslateKey(lines.get(i), keyToFind)) {
                return i;
            }
        }
        return -1;
    }
}
