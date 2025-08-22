package fr.silv.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class StatTextUtils {
    public static Text statColor(String suffix, String stat) {
        MutableText text = Text.literal(suffix);
        return switch (stat.toUpperCase()) {
            case "MBX.STATS.HEALTH" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE24A63)));
            case "MBX.STATS.STRENGTH" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xA55F26)));
            case "MBX.STATS.AGILITY" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x6BC047)));
            case "MBX.STATS.INTELLIGENCE" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE24A2E)));
            case "MBX.STATS.DEFENSE" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x1F8ECD)));
            case "MBX.STATS.WISDOM" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x9457D3)));
            case "MBX.STATS.LUCK" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x3D84A8)));
            case "MBX.STATS.FORTUNE" -> text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xEC8C2E)));
            default -> text.setStyle(Style.EMPTY.withColor(Formatting.WHITE));
        };
    }

    public static String formatStat(String stat) {
        return switch (stat.toUpperCase()) {
            case "MBX.STATS.FORTUNE" -> ("🔱 Fortune");
            case "MBX.STATS.LUCK" -> ("🌊 Luck");
            case "MBX.STATS.INTELLIGENCE" -> ("🔥 Intelligence");
            case "MBX.STATS.STRENGTH" -> ("₪ Strength");
            case "MBX.STATS.HEALTH" -> ("❤ Health");
            case "MBX.STATS.AGILITY" -> ("☄ Agility");
            case "MBX.STATS.WISDOM" -> ("☽ Wisdom");
            case "MBX.STATS.DEFENSE" -> ("🛡 Defense");
            default -> (stat);
        };
    }

    public static int getStatColor(String stat) {
        return switch (stat.toUpperCase()) {
            case "MBX.STATS.HEALTH" ->
                    0xE24A63;
            case "MBX.STATS.STRENGTH" -> 0xA55F26;
            case "MBX.STATS.AGILITY" -> 0x6BC047;
            case "MBX.STATS.INTELLIGENCE" -> 0xE24A2E;
            case "MBX.STATS.DEFENSE" -> 0x1F8ECD;
            case "MBX.STATS.WISDOM" -> 0x9457D3;
            case "MBX.STATS.LUCK" -> 0x3D84A8;
            case "MBX.STATS.FORTUNE" -> 0xEC8C2E;
            default -> 0xFFFFFF;
        };
    }

}
