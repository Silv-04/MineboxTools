package fr.silv.utils;

import fr.silv.Lang;
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

    public static String formatStatAdvanced(String stat) {
        return switch (stat.toUpperCase()) {
            case "MBX.STATS.FORTUNE" -> ("🔱 " + Lang.get("mineboxtools.stat.fortune"));
            case "MBX.STATS.LUCK" -> ("🌊 " + Lang.get("mineboxtools.stat.luck"));
            case "MBX.STATS.INTELLIGENCE" -> ("🔥 " + Lang.get("mineboxtools.stat.intelligence"));
            case "MBX.STATS.STRENGTH" -> ("₪ " + Lang.get("mineboxtools.stat.strength"));
            case "MBX.STATS.HEALTH" -> ("❤ " + Lang.get("mineboxtools.stat.health"));
            case "MBX.STATS.AGILITY" -> ("☄ " + Lang.get("mineboxtools.stat.agility"));
            case "MBX.STATS.WISDOM" -> ("☽ " + Lang.get("mineboxtools.stat.wisdom"));
            case "MBX.STATS.DEFENSE" -> ("🛡 " + Lang.get("mineboxtools.stat.defense"));
            default -> (stat);
        };
    }

    public static String formatStatSimple(String stat) {
        return switch (stat.toUpperCase()) {
            case "MBX.STATS.FORTUNE" -> ("🔱");
            case "MBX.STATS.LUCK" -> ("🌊");
            case "MBX.STATS.INTELLIGENCE" -> ("🔥");
            case "MBX.STATS.STRENGTH" -> ("₪");
            case "MBX.STATS.HEALTH" -> ("❤");
            case "MBX.STATS.AGILITY" -> ("☄");
            case "MBX.STATS.WISDOM" -> ("☽");
            case "MBX.STATS.DEFENSE" -> ("🛡");
            default -> (stat);
        };
    }
}
