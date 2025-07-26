package fr.silv.hud;

import java.time.LocalTime;

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
    private static final LocalTime PurpleEmperorStart = LocalTime.of(2, 0);
    private static final LocalTime PurpleEmperorEnd = LocalTime.of(2, 15);

    public static void register() {
        HudRenderCallback.EVENT.register(DailyShopTimer::onHudRender);
    }

    public static void onHudRender(DrawContext drawContext, RenderTickCounter tickDelta) {
        boolean isCocktailBarOpen = false;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;
        
        LocalTime now = LocalTime.now();
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
        if (total > 2775 && total < 2925) {
            drawContext.drawTexture(RenderLayer::getGuiTextured, CocktailBarICON, x, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
            isCocktailBarOpen = true;
        } else {isCocktailBarOpen = false;}
        // Italian restaurant
        if (total > 2850 || total < 750) {
            if (isCocktailBarOpen) {
                drawContext.drawTexture(RenderLayer::getGuiTextured, ItalianRestaurantICON, x - 32, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
            }
            else {
                drawContext.drawTexture(RenderLayer::getGuiTextured, ItalianRestaurantICON, x, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
            }
        }
        // Purple Emperor
        if ((now.isAfter(PurpleEmperorStart) && now.isBefore(PurpleEmperorEnd))
            || (now.isAfter(PurpleEmperorStart.plusHours(8)) && now.isBefore(PurpleEmperorEnd.plusHours(8)))
            || (now.isAfter(PurpleEmperorStart.plusHours(16)) && now.isBefore(PurpleEmperorEnd.plusHours(16)))
        ) {
            drawContext.drawTexture(RenderLayer::getGuiTextured, PurpleEmperorICON, x - 16, y, 0f, 0f, iconWidth, iconHeight, iconWidth, iconHeight);
        }
    }
}