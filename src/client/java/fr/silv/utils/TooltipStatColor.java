package fr.silv.utils;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class TooltipStatColor {
    public static Text statColor(String suffix, String stat) {
        MutableText text = Text.literal(suffix);
        switch (stat) {
            case "HEALTH":
                return text.formatted(Formatting.DARK_RED);
            case "STRENGTH":
                TextColor brown = TextColor.fromRgb(0x8B4513);
                return text.setStyle(Style.EMPTY.withColor(brown));
            case "AGILITY":
                return text.formatted(Formatting.GREEN);
            case "INTELLIGENCE":
                return text.formatted(Formatting.RED);
            case "LUCK":
                return text.formatted(Formatting.BLUE);
            case "WISDOM":
                return text.formatted(Formatting.LIGHT_PURPLE);
            case "FORTUNE":
                return text.formatted(Formatting.GOLD);
            case "DEFENSE":
                return text.formatted(Formatting.DARK_AQUA);
            default:
                return text.formatted(Formatting.GRAY);
        }
    }
}
