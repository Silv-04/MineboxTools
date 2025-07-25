package fr.silv.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class TooltipStatColor {
    public static Text statColor(String suffix, String stat) {
        MutableText text = Text.literal(suffix);
        switch (stat.toUpperCase()) {
            case "MBX.STATS.HEALTH":
                return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE24A63)));
            case "MBX.STATS.STRENGTH":
                return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xA55F26)));
            case "MBX.STATS.AGILITY":
                return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x6BC047)));
            case "MBX.STATS.INTELLIGENCE":
                return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE24A2E)));
            case "MBX.STATS.DEFENSE":
                return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x1F8ECD)));
            case "MBX.STATS.WISDOM":
                return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x9457D3)));
            case "MBX.STATS.LUCK":
                return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x3D84A8)));
            case "MBX.STATS.FORTUNE":
                return text.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xEC8C2E)));
            default:
                return text.setStyle(Style.EMPTY.withColor(Formatting.WHITE));
        }
    }
}
