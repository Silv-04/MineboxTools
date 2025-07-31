package fr.silv.hud;

import java.time.LocalTime;
import java.time.ZoneId;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public class DailyShopTimer {
    private static final Identifier BakeryICON = Identifier.of("mineboxtools", "textures/img/yellow_macaron.png");
    private static final Identifier ItalianRestaurantICON = Identifier.of("mineboxtools", "textures/img/cheese.png");
    private static final Identifier CocktailBarICON = Identifier.of("mineboxtools", "textures/img/yellow_cocktail.png");
    private static final Identifier CoffeeShopICON = Identifier.of("mineboxtools", "textures/img/yellow_coffee.png");
    private static final Identifier PurpleEmperorICON = Identifier.of("mineboxtools", "textures/img/purple_emperor.png");
    private static final Identifier HerbShopICON = Identifier.of("mineboxtools", "textures/img/herb.png");
    private static final Identifier PaintingICON = Identifier.of("mineboxtools", "textures/img/painting.png");

    private static final LocalTime FullMoonStart = LocalTime.of(2, 0);


    public static void register() {
        HudRenderCallback.EVENT.register(DailyShopTimer::onHudRender);
    }

    public static void onHudRender(DrawContext drawContext, RenderTickCounter tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden)
            return;

        LocalTime now = LocalTime.now(ZoneId.of("Europe/Paris"));
        int minute = now.getMinute();
        int second = now.getSecond();
        int total = minute * 60 + second;

        int screenWidth = client.getWindow().getScaledWidth();
        int xBase = screenWidth - 32;
        int y = 10;

        int iconWidth = 24;
        int spacing = 2;
        int offsetIndex = 0;

        // Coffee shop
        if (total >= 900 && total < 1800) {
            drawIcon(drawContext, CoffeeShopICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
            offsetIndex++;
        }

        // Bakery
        if (total >= 1800 && total < 2700) {
            drawIcon(drawContext, BakeryICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
            offsetIndex++;
        }

        // Cocktail bar + Painting
        if (total >= 2700 && total < 2925) {
            drawIcon(drawContext, CocktailBarICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
            offsetIndex++;
            drawIcon(drawContext, PaintingICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
            offsetIndex++;
        }

        // Italian restaurant
        if (total >= 2925 || total < 750) {
            drawIcon(drawContext, ItalianRestaurantICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
            offsetIndex++;
        }

        // Full moon cycles
        if (isFullMoonTime(now)) {
            if (total >= 0 && total < 900) {
                drawIcon(drawContext, PurpleEmperorICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }

            if ((total >= 0 && total < 600) || (total >= 3000 && total <=3599)) {
                drawIcon(drawContext, HerbShopICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
        }
    }

    private static void drawIcon(DrawContext context, Identifier icon, int x, int y) {
        context.drawTexture(RenderLayer::getGuiTextured, icon, x, y, 0f, 0f, 24, 24, 24, 24);
    }

    private static boolean isFullMoonTime(LocalTime now) {
        return isBetween(now, FullMoonStart, FullMoonStart.plusHours(1))
                || isBetween(now, FullMoonStart.plusHours(8), FullMoonStart.plusHours(9))
                || isBetween(now, FullMoonStart.plusHours(16), FullMoonStart.plusHours(17));
    }

    private static boolean isBetween(LocalTime now, LocalTime start, LocalTime end) {
        return !now.isBefore(start) && now.isBefore(end);
    }
}