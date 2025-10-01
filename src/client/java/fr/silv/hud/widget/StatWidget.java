package fr.silv.hud.widget;

import fr.silv.Lang;
import fr.silv.hud.widget.config.ConfigOption;
import fr.silv.model.MineboxStat;
import fr.silv.utils.MineboxItemStatUtils;
import fr.silv.ModConfig;
import fr.silv.utils.StatTextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StatWidget extends HudWidget {
    private static List<MineboxStat> cachedStatsTotal = new ArrayList<>();
    private static List<MineboxStat> cachedHandStats = new ArrayList<>();
    private static List<MineboxStat> cachedPlayerStats = new ArrayList<>();
    private static Logger DisplayStatsLogger = LogManager.getLogger(StatWidget.class);

    public StatWidget() {
        super("stat_widget",
                ModConfig.getWidgetPosition("stat_widget")[0],
                ModConfig.getWidgetPosition("stat_widget")[1],
                40, 90);
    }

    public static final Set<String> ALL_STATS = Set.of(
            "mbx.stats.fortune",
            "mbx.stats.luck",
            "mbx.stats.intelligence",
            "mbx.stats.strength",
            "mbx.stats.health",
            "mbx.stats.agility",
            "mbx.stats.wisdom",
            "mbx.stats.defense"
    );

    @Override
    public void render(DrawContext context, MinecraftClient client) {
            if (ModConfig.statToggle == ConfigOption.OFF) return;
            if (client.options.hudHidden) return;
            Lang.load(ModConfig.language);

            TextRenderer textRenderer = client.textRenderer;

            List<MineboxStat> stats = getCombinedStats(client);
            if (stats.isEmpty()) return;

            int lineHeight = textRenderer.fontHeight + 2;
            int y = this.y;
            int x = this.x;

            for (MineboxStat stat : stats) {
                String statString = "";
                if (ModConfig.statToggle == ConfigOption.SIMPLE) {
                    statString = StatTextUtils.formatStatSimple(stat.getStat()) + " " + stat.getValue();
                } else if (ModConfig.statToggle == ConfigOption.ADVANCED) {
                    statString = StatTextUtils.formatStatAdvanced(stat.getStat()) + ": " + stat.getValue();
                }
                Text text = StatTextUtils.statColor(statString, stat.getStat());
                context.drawTextWithShadow(textRenderer, text, x, y, Colors.WHITE);
                y += lineHeight;
            }
    }

    public static List<MineboxStat> getCombinedStats(MinecraftClient client) {
        List<MineboxStat> statsPlayer = new ArrayList<>();
        List<MineboxStat> statsItem = new ArrayList<>();
        if (client.player == null) return statsPlayer;

        ItemStack slot9 = client.player.getInventory().getStack(9);
        if (!slot9.isEmpty()) {
            statsPlayer = getStats(slot9, false);
        }

        ItemStack main = client.player.getMainHandStack();
        if (!main.isEmpty()) {
            statsItem = getStats(main, true);
        }

        if (MineboxItemStatUtils.areEquals(cachedPlayerStats,statsPlayer) && MineboxItemStatUtils.areEquals(cachedHandStats,statsItem)) {
            return cachedStatsTotal;
        }

        cachedPlayerStats =  new ArrayList<>(statsPlayer);;
        cachedHandStats = new ArrayList<>(statsItem);;

        for (MineboxStat statPlayer : statsPlayer) {
            for (MineboxStat statItem : statsItem) {
                if (statPlayer.getStat().equals(statItem.getStat())) {
                    statPlayer.setValue(statPlayer.getValue() + statItem.getValue());
                    break;
                }
            }
        }
        cachedStatsTotal = new ArrayList<>(statsPlayer);
        return statsPlayer;
    }

    private static List<MineboxStat> getStats(ItemStack stack, boolean includeBonus) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return new ArrayList<>();

        List<Text> lines = lore.lines();
        List<MineboxStat> stats = new ArrayList<>();

        for (Text line : lines) {
            MineboxStat stat;
            if (includeBonus) {
                stat = MineboxItemStatUtils.extractStatsFromLineWithBonus(line, ALL_STATS);
            }
            else {
                stat = MineboxItemStatUtils.extractStatsFromLine(line, ALL_STATS);
            }
            if (stat != null) {
                stats.add(stat);
            }
        }
        return stats;
    }
}
