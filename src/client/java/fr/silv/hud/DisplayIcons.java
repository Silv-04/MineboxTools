package fr.silv.hud;

import java.time.LocalTime;
import java.time.ZoneId;

import fr.silv.constants.DaylightCycle;
import fr.silv.constants.Icons;
import fr.silv.utils.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public class DisplayIcons {
    public static void register() {
        HudRenderCallback.EVENT.register(DisplayIcons::onHudRender);
    }

    public static void onHudRender(DrawContext drawContext, RenderTickCounter tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden)
            return;

        int screenWidth = client.getWindow().getScaledWidth();
        int xBase = screenWidth - 32;
        int y = 10;

        int iconWidth = 24;
        int spacing = 2;
        int offsetIndex = 0;

        LocalTime now = LocalTime.now(ZoneId.of("Europe/Paris"));

        // Insects
        if (DaylightCycle.isMorning(now)) {
            if (ModConfig.antToggle) {
                drawIcon(drawContext, Icons.AntICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.whiteButterflyToggle) {
                drawIcon(drawContext, Icons.WhiteButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.greenButterflyToggle) {
                drawIcon(drawContext, Icons.GreenButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.yellowButterflyToggle) {
                drawIcon(drawContext, Icons.YellowButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.greenDragonflyToggle) {
                drawIcon(drawContext, Icons.GreenDragonflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.yellowDragonflyToggle) {
                drawIcon(drawContext, Icons.YellowDragonflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.blueDragonflyToggle) {
                drawIcon(drawContext, Icons.BlueDragonflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.brownAntToggle) {
                drawIcon(drawContext, Icons.BrownAntICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.tigerButterflyToggle) {
                drawIcon(drawContext, Icons.TigerButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.ladybugToggle) {
                drawIcon(drawContext, Icons.LadybugICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
        }

        if (DaylightCycle.isAfternoon(now)) {
            if (ModConfig.antToggle) {
                drawIcon(drawContext, Icons.AntICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.locustToggle) {
                drawIcon(drawContext, Icons.LocustICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.whiteButterflyToggle) {
                drawIcon(drawContext, Icons.WhiteButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.yellowButterflyToggle) {
                drawIcon(drawContext, Icons.YellowButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.greenDragonflyToggle) {
                drawIcon(drawContext, Icons.GreenDragonflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.yellowDragonflyToggle) {
                drawIcon(drawContext, Icons.YellowDragonflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.blueDragonflyToggle) {
                drawIcon(drawContext, Icons.BlueDragonflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.redDragonflyToggle) {
                drawIcon(drawContext, Icons.RedDragonflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.mantisToggle) {
                drawIcon(drawContext, Icons.MantisICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.brownAntToggle) {
                drawIcon(drawContext, Icons.BrownAntICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.waspToggle) {
                drawIcon(drawContext, Icons.WaspICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.birdwingToggle) {
                drawIcon(drawContext, Icons.BirdwingICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.tigerButterflyToggle) {
                drawIcon(drawContext, Icons.TigerButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
        }

        if (DaylightCycle.isEvening(now)) {
            if (ModConfig.cricketToggle) {
                drawIcon(drawContext, Icons.CricketICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.cyclommatusToggle) {
                drawIcon(drawContext, Icons.CyclommatusICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.stickInsectToggle) {
                drawIcon(drawContext, Icons.StickInsectICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.spiderToggle) {
                drawIcon(drawContext, Icons.SpiderICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.centipedeToggle) {
                drawIcon(drawContext, Icons.CentipedeICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.dungBeetleToggle) {
                drawIcon(drawContext, Icons.DungleBeetleICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.blueButterflyToggle) {
                drawIcon(drawContext, Icons.BlueButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.fireflyToggle) {
                drawIcon(drawContext, Icons.FireflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.nightButterflyToggle) {
                drawIcon(drawContext, Icons.NightButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.tarantulaToggle) {
                drawIcon(drawContext, Icons.TarantulaICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.atlasMothToggle) {
                drawIcon(drawContext, Icons.AtlasMothButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.sunsetMothToggle) {
                drawIcon(drawContext, Icons.SunsetMothICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.scorpionToggle) {
                drawIcon(drawContext, Icons.ScorpionICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
        }

        if (DaylightCycle.isNight(now)) {
            if (ModConfig.cricketToggle) {
                drawIcon(drawContext, Icons.CricketICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.cyclommatusToggle) {
                drawIcon(drawContext, Icons.CyclommatusICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.stickInsectToggle) {
                drawIcon(drawContext, Icons.StickInsectICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.spiderToggle) {
                drawIcon(drawContext, Icons.SpiderICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.centipedeToggle) {
                drawIcon(drawContext, Icons.CentipedeICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.dungBeetleToggle) {
                drawIcon(drawContext, Icons.DungleBeetleICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.blueButterflyToggle) {
                drawIcon(drawContext, Icons.BlueButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.fireflyToggle) {
                drawIcon(drawContext, Icons.FireflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.nightButterflyToggle) {
                drawIcon(drawContext, Icons.NightButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.tarantulaToggle) {
                drawIcon(drawContext, Icons.TarantulaICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.atlasMothToggle) {
                drawIcon(drawContext, Icons.AtlasMothButterflyICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.scorpionToggle) {
                drawIcon(drawContext, Icons.ScorpionICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (DaylightCycle.isFullMoon(now) && ModConfig.purpleEmperorToggle) {
                drawIcon(drawContext, Icons.PurpleEmperorICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
        }

        // Coffee shop
        if (DaylightCycle.isMorning(now) && ModConfig.coffeeShopToggle) {
            drawIcon(drawContext, Icons.CoffeeShopICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
            offsetIndex++;
        }

        // Bakery
        if (DaylightCycle.isAfternoon(now) && ModConfig.bakeryToggle) {
            drawIcon(drawContext, Icons.BakeryICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
            offsetIndex++;
        }

        // Cocktail bar + Painting
        if (DaylightCycle.isCocktailAndMonkeyShopOpen(now)) {
            if (ModConfig.cocktailBarToggle) {
                drawIcon(drawContext, Icons.CocktailBarICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (ModConfig.paintingShopToggle) {
                drawIcon(drawContext, Icons.PaintingICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
        }

        // Italian restaurant
        if (DaylightCycle.isItalianRestaurantOpen(now) && ModConfig.italianRestaurantToggle) {
            drawIcon(drawContext, Icons.ItalianRestaurantICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
            offsetIndex++;
        }

        // Full moon cycles
        if (DaylightCycle.isFullMoon(now) && DaylightCycle.isHerbShopOpen(now) && ModConfig.herbShopToggle) {
            drawIcon(drawContext, Icons.HerbShopICON, xBase - (offsetIndex * (iconWidth + spacing)), y);
            offsetIndex++;
        }
    }

    private static void drawIcon(DrawContext context, Identifier icon, int x, int y) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, icon, x, y, 0f, 0f, 24, 24, 24, 24);
    }
}