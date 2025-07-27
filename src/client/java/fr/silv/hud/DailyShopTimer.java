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
    private static final LocalTime FullMoonEnd = LocalTime.of(3, 0);

    public static void register() {
        HudRenderCallback.EVENT.register(DailyShopTimer::onHudRender);
    }

    public static void onHudRender(DrawContext drawContext, RenderTickCounter tickDelta) {
        boolean isPurpleEmperor = false;
        boolean isItalianRestaurantOpen = false;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        
        LocalTime now = LocalTime.now(ZoneId.of("Europe/Paris"));
        int minute = now.getMinute();
        int second = now.getSecond();
        int total = minute* 60 + second;

        int screenWidth = client.getWindow().getScaledWidth();
        int x = screenWidth - 32;
        int y = 10;

        int iconWidth = 24;
        int iconHeight = 24;

        // Coffee shop
        if (minute > 15 && minute < 30 ) {
            drawContext.drawTexture(RenderLayer::getGuiTextured, CoffeeShopICON, x, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
        }

        // Bakery
        if (minute > 30 && minute < 45) {
            drawContext.drawTexture(RenderLayer::getGuiTextured, BakeryICON, x, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
        }

        // Cocktail bar
        if (minute > 45 && total < 2925) {
            drawContext.drawTexture(RenderLayer::getGuiTextured, CocktailBarICON, x, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
            drawContext.drawTexture(RenderLayer::getGuiTextured, PaintingICON, x - 32, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
        }
        // Italian restaurant
        if (total > 2925 || total < 750) {
            drawContext.drawTexture(RenderLayer::getGuiTextured, ItalianRestaurantICON, x, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
            isItalianRestaurantOpen = true;
        }
        else {
            isItalianRestaurantOpen = false;
        }

        // Full
        if ((now.isAfter(FullMoonStart) && now.isBefore(FullMoonEnd))
            || (now.isAfter(FullMoonStart.plusHours(8)) && now.isBefore(FullMoonStart.plusHours(8)))
            || (now.isAfter(FullMoonStart.plusHours(16)) && now.isBefore(FullMoonStart.plusHours(16)))
        ) {
            // Purple Emperor
            if (minute > 0 && minute < 15) {
                drawContext.drawTexture(RenderLayer::getGuiTextured, PurpleEmperorICON, x, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
                isPurpleEmperor = true;
            }
            else {
                isPurpleEmperor = false;
            }

            // Herb shop
            if (minute > 0 && minute < 10 || minute > 50 && minute < 60) {
                if (isPurpleEmperor || isItalianRestaurantOpen) {
                    drawContext.drawTexture(RenderLayer::getGuiTextured, HerbShopICON, x - 32, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
                }
                else {
                    drawContext.drawTexture(RenderLayer::getGuiTextured, HerbShopICON, x, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
                }
            }
        }
    }
}