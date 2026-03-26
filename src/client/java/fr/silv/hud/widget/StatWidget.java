package fr.silv.hud.widget;

import fr.silv.ModConfig;
import fr.silv.constants.StatDefinition;
import fr.silv.hud.widget.config.ConfigOption;
import fr.silv.model.MineboxStat;
import fr.silv.utils.MineboxItemDataUtils;
import fr.silv.utils.MineboxItemStatUtils;
import fr.silv.utils.StatTextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * HUD widget that renders combined player statistics.
 */
public class StatWidget extends HudWidget {
    private static final Set<String> ALL_STATS = StatDefinition.keys();
    private static final List<String> STAT_ORDER = List.copyOf(ALL_STATS);
    private static final Map<LoreComponent, Map<String, Integer>> BASE_LORE_STATS_CACHE = new WeakHashMap<>();
    private static final Map<LoreComponent, Map<String, Integer>> BONUS_LORE_STATS_CACHE = new WeakHashMap<>();

    private static Map<String, Integer> cachedPlayerStats = Map.of();
    private static Map<String, Integer> cachedHandStats = Map.of();
    private static List<MineboxStat> cachedCombinedStats = List.of();

    /**
     * Creates a new StatWidget instance.
     */
    public StatWidget() {
        super("stat_widget",
                ModConfig.getWidgetPosition("stat_widget")[0],
                ModConfig.getWidgetPosition("stat_widget")[1],
                40, 90);
    }

    @Override
    /**
        * Renders aggregated player stats using the configured display mode.
        *
        * @param context draw context
        * @param client active client instance
     */
    public void render(DrawContext context, MinecraftClient client) {
        ConfigOption displayMode = ModConfig.getStatDisplay();
        if (displayMode == ConfigOption.OFF || client.options.hudHidden) {
            return;
        }

        List<MineboxStat> stats = getCombinedStats(client);
        if (stats.isEmpty()) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        int lineHeight = textRenderer.fontHeight + 2;
        int currentY = this.y;

        for (MineboxStat stat : stats) {
            Text text = StatTextUtils.statColor(formatStat(stat, displayMode), stat.getStat());
            context.drawTextWithShadow(textRenderer, text, this.x, currentY, Colors.WHITE);
            currentY += lineHeight;
        }
    }

    /**
        * Returns merged player and held-item stat values.
        * Results are cached and recomputed only when source stats change.
        *
        * @param client active client instance
        * @return combined stat list in stable declaration order
     */
    public static List<MineboxStat> getCombinedStats(MinecraftClient client) {
        if (client.player == null) {
            return List.of();
        }

        Map<String, Integer> playerStats = extractStats(client.player.getInventory().getStack(9), false);
        Map<String, Integer> handStats = extractHandStats(client.player.getMainHandStack());

        if (cachedPlayerStats.equals(playerStats) && cachedHandStats.equals(handStats)) {
            return cachedCombinedStats;
        }

        cachedPlayerStats = playerStats;
        cachedHandStats = handStats;
        cachedCombinedStats = toStatList(mergeStats(playerStats, handStats));
        return cachedCombinedStats;
    }

    private static String formatStat(MineboxStat stat, ConfigOption displayMode) {
        return switch (displayMode) {
            case SIMPLE -> StatTextUtils.formatStatSimple(stat.getStat()) + " " + stat.getValue();
            case ADVANCED -> StatTextUtils.formatStatAdvanced(stat.getStat()) + ": " + stat.getValue();
            case OFF -> "";
        };
    }

    private static Map<String, Integer> extractHandStats(ItemStack stack) {
        if (stack.isEmpty()) {
            return Map.of();
        }

        Optional<String> itemIdOptional = MineboxItemDataUtils.getItemId(stack);
        if (itemIdOptional.isEmpty() || MineboxItemDataUtils.isEquipmentOrAccessory(itemIdOptional.get())) {
            return Map.of();
        }

        return extractStats(stack, true);
    }

    private static Map<String, Integer> extractStats(ItemStack stack, boolean includeBonus) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) {
            return Map.of();
        }

        Map<LoreComponent, Map<String, Integer>> cache = includeBonus ? BONUS_LORE_STATS_CACHE : BASE_LORE_STATS_CACHE;
        Map<String, Integer> cachedStats = cache.get(lore);
        if (cachedStats != null) {
            return cachedStats;
        }

        Map<String, Integer> stats = new LinkedHashMap<>();
        for (Text line : lore.lines()) {
            MineboxStat stat = includeBonus
                    ? MineboxItemStatUtils.extractStatsFromLineWithBonus(line, ALL_STATS)
                    : MineboxItemStatUtils.extractStatsFromLine(line, ALL_STATS);
            if (stat != null) {
                stats.merge(stat.getStat(), stat.getValue(), Integer::sum);
            }
        }

        if (stats.isEmpty()) {
            cache.put(lore, Map.of());
            return Map.of();
        }

        Map<String, Integer> orderedStats = orderedStats(stats);
        cache.put(lore, orderedStats);
        return orderedStats;
    }

    private static Map<String, Integer> mergeStats(Map<String, Integer> playerStats, Map<String, Integer> handStats) {
        Map<String, Integer> merged = new LinkedHashMap<>();
        for (String statKey : STAT_ORDER) {
            boolean exists = playerStats.containsKey(statKey) || handStats.containsKey(statKey);
            if (!exists) {
                continue;
            }

            merged.put(statKey, playerStats.getOrDefault(statKey, 0) + handStats.getOrDefault(statKey, 0));
        }
        return merged;
    }

    private static Map<String, Integer> orderedStats(Map<String, Integer> stats) {
        Map<String, Integer> ordered = new LinkedHashMap<>();
        for (String statKey : STAT_ORDER) {
            Integer value = stats.get(statKey);
            if (value != null) {
                ordered.put(statKey, value);
            }
        }
        return ordered;
    }

    private static List<MineboxStat> toStatList(Map<String, Integer> stats) {
        List<MineboxStat> result = new ArrayList<>(stats.size());
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            result.add(new MineboxStat(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
