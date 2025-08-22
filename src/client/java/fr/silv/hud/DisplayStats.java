package fr.silv.hud;

import fr.silv.model.MineboxStat;
import fr.silv.utils.ItemStatUtils;
import fr.silv.utils.ModConfig;
import fr.silv.utils.StatTextUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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


public class DisplayStats {
    private static List<MineboxStat> cachedStatsTotal = new ArrayList<>();
    private static List<MineboxStat> cachedHandStats = new ArrayList<>();
    private static List<MineboxStat> cachedPlayerStats = new ArrayList<>();
    private static Logger DisplayStatsLogger = LogManager.getLogger(DisplayStats.class);

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

    public static void register() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!ModConfig.statToggle) return;
            MinecraftClient client = MinecraftClient.getInstance();
            TextRenderer textRenderer = client.textRenderer;

            List<MineboxStat> stats = getCombinedStats(client);
            if (stats.isEmpty()) return;

            int screenHeight = client.getWindow().getScaledHeight();
            int lineHeight = textRenderer.fontHeight + 2;
            int totalHeight = stats.size() * lineHeight;
            int y = Math.abs(screenHeight - totalHeight) / 2;
            int x = 4;

            for (MineboxStat stat : stats) {
                String statString = StatTextUtils.formatStat(stat.getStat()) + ": " + stat.getValue();
                Text text = StatTextUtils.statColor(statString, stat.getStat());
                drawContext.drawTextWithShadow(textRenderer, text, x, y, Colors.WHITE);
                y += lineHeight;
            }
        });
    }

    public static List<MineboxStat> getCombinedStats(MinecraftClient client) {
        List<MineboxStat> statsPlayer = new ArrayList<>();
        List<MineboxStat> statsItem = new ArrayList<>();
        if (client.player == null) return statsPlayer;

        ItemStack slot9 = client.player.getInventory().getStack(9);
        if (!slot9.isEmpty()) {
            statsPlayer = getStats(slot9);
        }

        ItemStack main = client.player.getMainHandStack();
        if (!main.isEmpty()) {
            statsItem = getStats(main);
        }

        if (ItemStatUtils.areEquals(cachedPlayerStats,statsPlayer) && ItemStatUtils.areEquals(cachedHandStats,statsItem)) {
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

    private static List<MineboxStat> getStats(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return new ArrayList<>();

        List<Text> lines = lore.lines();
        List<MineboxStat> stats = new ArrayList<>();

        for (Text line : lines) {
            MineboxStat stat = ItemStatUtils.extractStatsFromLine(line, ALL_STATS);
            if (stat != null) {
                stats.add(stat);
            }
        }
        return stats;
    }
}
