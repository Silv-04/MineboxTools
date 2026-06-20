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
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemLore;
import java.util.OptionalInt;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Builds and injects custom item tooltip sections.
 */
public final class TooltipHandler {
    private static final Logger LOGGER = ModLog.getLogger(TooltipHandler.class);
    private static final String TOOLTIP_BULLET = "• ";
    private static final int LOCATION_TITLE_COLOR = 0xFFA500;
    private static final int CONDITION_TITLE_COLOR = 0xFFFF00;
    private static final int BOOST_TITLE_COLOR = 0x00FF00;
    private static final int DETAIL_COLOR = 0xFFFFFF;
    private static final String SEE_MORE_KEY = "mbx.see_more";
    private static final String OPEN_ACTION_KEY = "mbx.actions.open";

    private TooltipHandler() {
    }

    /**
     * Appends stat ranges and a quality score to the tooltip.
     *
     * @param stack   item stack being rendered
     * @param context tooltip rendering context
     * @param flag    tooltip flag provided by Minecraft
     * @param lines   mutable tooltip line list
     */
    public static void addStatRangesToTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag flag,
                                              List<Component> lines) {
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

        CompoundTag persistent = itemData.persistentData();
        String itemId = itemData.itemId();

        Map<String, int[]> statRanges = MineboxItemStatUtils.getStatsFor(itemId);
        if (statRanges.isEmpty()) {
            return;
        }

        Set<String> statKeys = statRanges.keySet();
        Map<String, Integer> actualStats = new HashMap<>();

        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
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

            Component newLine = line.copy().append(StatTextUtils.statColor(suffix.toString(), stat.getStat()));
            lines.set(i, newLine);
            actualStats.put(stat.getStat().toLowerCase(), stat.getValue());
        }

        if (MineboxItemDataUtils.getStatsData(persistent).isEmpty() || actualStats.isEmpty()) {
            return;
        }

        int score = (int) computeGlobalStatScore(actualStats, statRanges);
        Style style = getColorFromScore(score);
        Component scoreLine = lines.getFirst().copy()
                .append(Component.literal(" " + score + "%").setStyle(style.withBold(true)));
        lines.set(0, scoreLine);
    }

    /**
     * Appends harvest location, weather condition, and drop boost info to the tooltip.
     *
     * @param stack   item stack being rendered
     * @param context tooltip rendering context
     * @param flag    tooltip flag provided by Minecraft
     * @param lines   mutable tooltip line list
     */
    public static void addInfoToTooltip(ItemStack stack, Item.TooltipContext context, TooltipFlag flag,
                                        List<Component> lines) {
        if (!ModConfig.isEnabled(ModConfig.FeatureFlag.LOCATION)) {
            return;
        }

        MineboxItem item = resolveItem(stack);
        if (item == null) {
            return;
        }

        int seeMoreIndex = findIndexOfTranslateKey(lines, SEE_MORE_KEY);
        Component seeMoreText = Component.literal("");

        if (seeMoreIndex != -1) {
            seeMoreText = lines.remove(seeMoreIndex);
        } else {
            seeMoreIndex = findIndexOfTranslateKey(lines, OPEN_ACTION_KEY);
            if (seeMoreIndex != -1) {
                seeMoreText = lines.remove(seeMoreIndex);
            }
        }

        lines.add(sectionTitle("mineboxtools.menu.tooltip.location", LOCATION_TITLE_COLOR));
        for (String location : item.getLocation()) {
            lines.add(detailLine(location));
        }

        List<String> conditions = item.getConditions();
        if (!conditions.isEmpty()) {
            lines.add(Component.literal(""));
            lines.add(sectionTitle("mineboxtools.menu.tooltip.condition", CONDITION_TITLE_COLOR));
            for (String condition : conditions) {
                lines.add(detailLine(condition));
            }
        }

        String boost = item.getBoost();
        if (!boost.isEmpty()) {
            lines.add(Component.literal(""));
            lines.add(sectionTitle("mineboxtools.menu.tooltip.boost", BOOST_TITLE_COLOR));
            lines.add(detailLine(boost));
        }

        if (seeMoreIndex != -1) {
            lines.add(Component.literal(""));
            lines.add(seeMoreText);
        }
    }

    private static MineboxItem resolveItem(ItemStack stack) {
        String itemIdFromNbt = MineboxItemDataUtils.getItemId(stack).orElse("");
        String itemIdFromName = "";

        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
            for (Component sibling : customName.getSiblings()) {
                if (sibling.getContents() instanceof TranslatableContents translatable
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

    private static Component sectionTitle(String translationKey, int color) {
        return Component.literal(Lang.get(translationKey))
                .setStyle(Style.EMPTY.withColor(color).withBold(true));
    }

    private static Component detailLine(String translationKey) {
        return Component.literal(TOOLTIP_BULLET + Lang.get(translationKey))
                .setStyle(Style.EMPTY.withColor(DETAIL_COLOR));
    }

    /**
     * Computes the quality score (0–100) for an item stack, or empty when the item
     * has no stat ranges defined.
     *
     * @param stack item stack to evaluate
     * @return score between 0 and 100, or empty when not applicable
     */
    public static OptionalInt computeItemScore(ItemStack stack) {
        Optional<MineboxItemDataUtils.PersistentItemData> itemDataOpt = MineboxItemDataUtils.getPersistentItemData(stack);
        if (itemDataOpt.isEmpty()) {
            return OptionalInt.empty();
        }

        MineboxItemDataUtils.PersistentItemData itemData = itemDataOpt.get();
        if (MineboxItemDataUtils.isDisplayOnlyItem(itemData.customData())) {
            return OptionalInt.empty();
        }

        Map<String, int[]> statRanges = MineboxItemStatUtils.getStatsFor(itemData.itemId());
        if (statRanges.isEmpty()) {
            return OptionalInt.empty();
        }

        if (MineboxItemDataUtils.getStatsData(itemData.persistentData()).isEmpty()) {
            return OptionalInt.empty();
        }

        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) {
            return OptionalInt.empty();
        }

        Set<String> statKeys = statRanges.keySet();
        Map<String, Integer> actualStats = new HashMap<>();
        for (Component line : lore.lines()) {
            MineboxStat stat = MineboxItemStatUtils.extractStatsFromLine(line, statKeys);
            if (stat != null) {
                actualStats.put(stat.getStat().toLowerCase(), stat.getValue());
            }
        }

        if (actualStats.isEmpty()) {
            return OptionalInt.empty();
        }

        return OptionalInt.of((int) computeGlobalStatScore(actualStats, statRanges));
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
     * @param text      root text node to inspect
     * @param keyToFind translation key to search for
     * @return {@code true} when the key is present in this node or descendants
     */
    public static boolean containsTranslateKey(Component text, String keyToFind) {
        if (text.getContents() instanceof TranslatableContents translatable && keyToFind.equals(translatable.getKey())) {
            return true;
        }
        for (Component sibling : text.getSiblings()) {
            if (containsTranslateKey(sibling, keyToFind)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the index of the first tooltip line containing the specified translation key.
     *
     * @param lines     tooltip lines to inspect
     * @param keyToFind translation key to search for
     * @return index of the first matching line, or {@code -1} when not found
     */
    public static int findIndexOfTranslateKey(List<Component> lines, String keyToFind) {
        for (int i = 0; i < lines.size(); i++) {
            if (containsTranslateKey(lines.get(i), keyToFind)) {
                return i;
            }
        }
        return -1;
    }
}
