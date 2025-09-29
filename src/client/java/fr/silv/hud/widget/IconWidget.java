package fr.silv.hud.widget;

import java.time.LocalTime;
import java.time.ZoneId;

import fr.silv.constants.DaylightCycle;
import fr.silv.constants.Icons;
import fr.silv.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class IconWidget extends HudWidget {

    public IconWidget() {
        super("icon_widget",
                ModConfig.getWidgetPosition("icon_widget")[0],
                ModConfig.getWidgetPosition("icon_widget")[1],
                24, 24);
    }

    @Override
    public void render(DrawContext drawContext, MinecraftClient client) {
        World world = client.world;

        if (client.player == null || client.options.hudHidden)
            return;

        int x = this.x;
        int y = this.y;

        int iconWidth = 24;
        int spacing = 2;
        int offsetIndex = 0;

        LocalTime now = LocalTime.now(ZoneId.of("Europe/Paris"));

        // Insects
        if (world.isRaining() || world.isThundering()) {
            if (ModConfig.snailToggle) {
                drawIcon(drawContext, Icons.SnailICON, x - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
        }
        if (!world.isRaining() && !world.isThundering()) {
            if (DaylightCycle.isMorning(now)) {
                if (ModConfig.antToggle) {
                    drawIcon(drawContext, Icons.AntICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.whiteButterflyToggle) {
                    drawIcon(drawContext, Icons.WhiteButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.greenButterflyToggle) {
                    drawIcon(drawContext, Icons.GreenButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.yellowButterflyToggle) {
                    drawIcon(drawContext, Icons.YellowButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.greenDragonflyToggle) {
                    drawIcon(drawContext, Icons.GreenDragonflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.yellowDragonflyToggle) {
                    drawIcon(drawContext, Icons.YellowDragonflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.blueDragonflyToggle) {
                    drawIcon(drawContext, Icons.BlueDragonflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.brownAntToggle) {
                    drawIcon(drawContext, Icons.BrownAntICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.tigerButterflyToggle) {
                    drawIcon(drawContext, Icons.TigerButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.ladybugToggle) {
                    drawIcon(drawContext, Icons.LadybugICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
            }

            if (DaylightCycle.isAfternoon(now)) {
                if (ModConfig.antToggle) {
                    drawIcon(drawContext, Icons.AntICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.locustToggle) {
                    drawIcon(drawContext, Icons.LocustICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.whiteButterflyToggle) {
                    drawIcon(drawContext, Icons.WhiteButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.yellowButterflyToggle) {
                    drawIcon(drawContext, Icons.YellowButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.greenDragonflyToggle) {
                    drawIcon(drawContext, Icons.GreenDragonflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.yellowDragonflyToggle) {
                    drawIcon(drawContext, Icons.YellowDragonflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.blueDragonflyToggle) {
                    drawIcon(drawContext, Icons.BlueDragonflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.redDragonflyToggle) {
                    drawIcon(drawContext, Icons.RedDragonflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.mantisToggle) {
                    drawIcon(drawContext, Icons.MantisICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.brownAntToggle) {
                    drawIcon(drawContext, Icons.BrownAntICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.waspToggle) {
                    drawIcon(drawContext, Icons.WaspICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.birdwingToggle) {
                    drawIcon(drawContext, Icons.BirdwingICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.tigerButterflyToggle) {
                    drawIcon(drawContext, Icons.TigerButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
            }

            if (DaylightCycle.isEvening(now)) {
                if (ModConfig.cricketToggle) {
                    drawIcon(drawContext, Icons.CricketICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.cyclommatusToggle) {
                    drawIcon(drawContext, Icons.CyclommatusICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.stickInsectToggle) {
                    drawIcon(drawContext, Icons.StickInsectICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.spiderToggle) {
                    drawIcon(drawContext, Icons.SpiderICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.centipedeToggle) {
                    drawIcon(drawContext, Icons.CentipedeICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.dungBeetleToggle) {
                    drawIcon(drawContext, Icons.DungleBeetleICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.blueButterflyToggle) {
                    drawIcon(drawContext, Icons.BlueButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.fireflyToggle) {
                    drawIcon(drawContext, Icons.FireflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.nightButterflyToggle) {
                    drawIcon(drawContext, Icons.NightButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.tarantulaToggle) {
                    drawIcon(drawContext, Icons.TarantulaICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.atlasMothToggle) {
                    drawIcon(drawContext, Icons.AtlasMothButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.sunsetMothToggle) {
                    drawIcon(drawContext, Icons.SunsetMothICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
            }

            if (DaylightCycle.isNight(now)) {
                if (ModConfig.cricketToggle) {
                    drawIcon(drawContext, Icons.CricketICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.cyclommatusToggle) {
                    drawIcon(drawContext, Icons.CyclommatusICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.stickInsectToggle) {
                    drawIcon(drawContext, Icons.StickInsectICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.spiderToggle) {
                    drawIcon(drawContext, Icons.SpiderICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.centipedeToggle) {
                    drawIcon(drawContext, Icons.CentipedeICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.dungBeetleToggle) {
                    drawIcon(drawContext, Icons.DungleBeetleICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.blueButterflyToggle) {
                    drawIcon(drawContext, Icons.BlueButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.fireflyToggle) {
                    drawIcon(drawContext, Icons.FireflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.nightButterflyToggle) {
                    drawIcon(drawContext, Icons.NightButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.tarantulaToggle) {
                    drawIcon(drawContext, Icons.TarantulaICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.atlasMothToggle) {
                    drawIcon(drawContext, Icons.AtlasMothButterflyICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
            }
        }

        if (DaylightCycle.isEvening(now)) {
            if (ModConfig.scorpionToggle) {
                drawIcon(drawContext, Icons.ScorpionICON, x - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
        }

        if (DaylightCycle.isNight(now)){
            if (ModConfig.scorpionToggle) {
                drawIcon(drawContext, Icons.ScorpionICON, x - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
            if (DaylightCycle.isFullMoon(now) && ModConfig.purpleEmperorToggle) {
                drawIcon(drawContext, Icons.PurpleEmperorICON, x - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
        }

        // Coffee shop
        if (!world.isThundering()) {
            if (DaylightCycle.isMorning(now) && ModConfig.coffeeShopToggle) {
                drawIcon(drawContext, Icons.CoffeeShopICON, x - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }

            // Bakery
            if (DaylightCycle.isAfternoon(now) && ModConfig.bakeryToggle) {
                drawIcon(drawContext, Icons.BakeryICON, x - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }

            // Cocktail bar + Painting
            if (DaylightCycle.isCocktailAndMonkeyShopOpen(now)) {
                if (ModConfig.cocktailBarToggle) {
                    drawIcon(drawContext, Icons.CocktailBarICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
                if (ModConfig.paintingShopToggle) {
                    drawIcon(drawContext, Icons.PaintingICON, x - (offsetIndex * (iconWidth + spacing)), y);
                    offsetIndex++;
                }
            }

            // Italian restaurant
            if (DaylightCycle.isItalianRestaurantOpen(now) && ModConfig.italianRestaurantToggle) {
                drawIcon(drawContext, Icons.ItalianRestaurantICON, x - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }

            // Full moon cycles
            if (DaylightCycle.isFullMoon(now) && DaylightCycle.isHerbShopOpen(now) && ModConfig.herbShopToggle) {
                drawIcon(drawContext, Icons.HerbShopICON, x - (offsetIndex * (iconWidth + spacing)), y);
                offsetIndex++;
            }
        }
    }

    private static void drawIcon(DrawContext context, Identifier icon, int x, int y) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, icon, x, y, 0f, 0f, 24, 24, 24, 24);
    }
}