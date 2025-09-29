package fr.silv.hud;

import fr.silv.constants.Icons;
import fr.silv.hud.widget.HudWidgetManager;
import fr.silv.hud.widget.config.CheckboxListWidget;
import fr.silv.hud.widget.config.ConfigOption;
import fr.silv.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class MenuHUD extends Screen {
    public MenuHUD() {
        super(Text.of("MineboxTools Menu"));
    }

    @Override
    public void init() {
        super.init();

        // Durability
        CyclingButtonWidget<Boolean> toggleButtonDurability = CyclingButtonWidget.onOffBuilder(ModConfig.durabilityToggle)
                .build(20, 20, 130, 20, Text.literal("Durability Toggle"),
                        (button, value) -> {
                    ModConfig.durabilityToggle = value;
                    ModConfig.save();
                        });

        // Tooltip
        CyclingButtonWidget<Boolean> toggleButtonTooltip = CyclingButtonWidget.onOffBuilder(ModConfig.tooltipToggle)
                .build(20, 40, 130, 20, Text.literal("Tooltip Toggle"),
                        (button, value) -> {
                            ModConfig.tooltipToggle = value;
                            ModConfig.save();
                        });

        // Stats
        CyclingButtonWidget<ConfigOption> toggleButtonStats = CyclingButtonWidget.builder(ConfigOption::getDisplayName)
                .values(ConfigOption.values())
                .initially(ModConfig.statToggle)
                .build(20, 60, 130, 20, Text.literal("Stat Toggle"),
                        (button, value) -> {
                            ModConfig.statToggle = value;
                            ModConfig.save();
                        });

        // Off hand haversack amount inside toggle
        CyclingButtonWidget<Boolean> toggleButtonOffHand = CyclingButtonWidget.onOffBuilder(ModConfig.offHandToggle)
                .build(20, 80, 130, 20, Text.literal("OffHand Toggle"),
                        (button, value) -> {
                            ModConfig.offHandToggle = value;
                            ModConfig.save();
                        });

        // Custom HUD
        ButtonWidget customHUDButton = ButtonWidget.builder(Text.literal("Custom HUD"), button -> {
            MinecraftClient.getInstance().setScreen(new HudConfigScreen(HudWidgetManager.getWidgets()));
        }).dimensions(20, 100, 130, 20).build();

        // Insects
        int areaWidthInsect = 140;
        int areaHeightInsect = this.height - 80;
        int rowHeightInsect = 20;
        int xInsect = this.width - 160;
        int yInsect = 20;

        CheckboxListWidget insectList = new CheckboxListWidget(MinecraftClient.getInstance(), xInsect, yInsect, areaWidthInsect, areaHeightInsect, rowHeightInsect);
        insectList.addOption("Ant", ModConfig.antToggle, checked -> {
            ModConfig.antToggle = checked;
            ModConfig.save();
        }, Icons.AntICON);
        insectList.addOption("Atlas moth", ModConfig.atlasMothToggle, checked -> {
            ModConfig.atlasMothToggle = checked;
            ModConfig.save();
        }, Icons.AtlasMothButterflyICON);
        insectList.addOption("Birdwing", ModConfig.birdwingToggle, checked -> {
            ModConfig.birdwingToggle = checked;
            ModConfig.save();
        }, Icons.BirdwingICON);
        insectList.addOption("Blue butterfly", ModConfig.blueButterflyToggle, checked -> {
            ModConfig.blueButterflyToggle = checked;
            ModConfig.save();
        }, Icons.BlueButterflyICON);
        insectList.addOption("Blue dragonfly", ModConfig.blueDragonflyToggle, checked -> {
            ModConfig.blueDragonflyToggle = checked;
            ModConfig.save();
        }, Icons.BlueDragonflyICON);
        insectList.addOption("Brown ant", ModConfig.brownAntToggle, checked -> {
            ModConfig.brownAntToggle = checked;
            ModConfig.save();
        }, Icons.BrownAntICON);
        insectList.addOption("Centipede", ModConfig.centipedeToggle, checked -> {
            ModConfig.centipedeToggle = checked;
            ModConfig.save();
        }, Icons.CentipedeICON);
        insectList.addOption("Cricket", ModConfig.cricketToggle, checked -> {
            ModConfig.cricketToggle = checked;
            ModConfig.save();
        }, Icons.CricketICON);
        insectList.addOption("Cyclommatus", ModConfig.cyclommatusToggle, checked -> {
            ModConfig.cyclommatusToggle = checked;
            ModConfig.save();
        }, Icons.CyclommatusICON);
        insectList.addOption("Dung beetle", ModConfig.dungBeetleToggle, checked -> {
            ModConfig.dungBeetleToggle = checked;
            ModConfig.save();
        }, Icons.DungleBeetleICON);
        insectList.addOption("Firefly", ModConfig.fireflyToggle, checked -> {
            ModConfig.fireflyToggle = checked;
            ModConfig.save();
        }, Icons.FireflyICON);
        insectList.addOption("Green butterfly", ModConfig.greenButterflyToggle, checked -> {
            ModConfig.greenButterflyToggle = checked;
            ModConfig.save();
        }, Icons.GreenButterflyICON);
        insectList.addOption("Green dragonfly", ModConfig.greenDragonflyToggle, checked -> {
            ModConfig.greenDragonflyToggle = checked;
            ModConfig.save();
        }, Icons.GreenDragonflyICON);
        insectList.addOption("Ladybug", ModConfig.ladybugToggle, checked -> {
            ModConfig.ladybugToggle = checked;
            ModConfig.save();
        }, Icons.LadybugICON);
        insectList.addOption("Locust", ModConfig.locustToggle, checked -> {
            ModConfig.locustToggle = checked;
            ModConfig.save();
        }, Icons.LocustICON);
        insectList.addOption("Mantis", ModConfig.mantisToggle, checked -> {
            ModConfig.mantisToggle = checked;
            ModConfig.save();
        }, Icons.MantisICON);
        insectList.addOption("Night butterfly", ModConfig.nightButterflyToggle, checked -> {
            ModConfig.nightButterflyToggle = checked;
            ModConfig.save();
        }, Icons.NightButterflyICON);
        insectList.addOption("Purple emperor", ModConfig.purpleEmperorToggle, checked -> {
            ModConfig.purpleEmperorToggle = checked;
            ModConfig.save();
        }, Icons.PurpleEmperorICON);
        insectList.addOption("Red dragonfly", ModConfig.redDragonflyToggle, checked -> {
            ModConfig.redDragonflyToggle = checked;
            ModConfig.save();
        }, Icons.RedDragonflyICON);
        insectList.addOption("Scorpion", ModConfig.scorpionToggle, checked -> {
            ModConfig.scorpionToggle = checked;
            ModConfig.save();
        }, Icons.ScorpionICON);
        insectList.addOption("Spider", ModConfig.spiderToggle, checked -> {
            ModConfig.spiderToggle = checked;
            ModConfig.save();
        }, Icons.SpiderICON);
        insectList.addOption("Stick insect", ModConfig.stickInsectToggle, checked -> {
            ModConfig.stickInsectToggle = checked;
            ModConfig.save();
        }, Icons.StickInsectICON);
        insectList.addOption("Snail", ModConfig.snailToggle, checked -> {
            ModConfig.snailToggle = checked;
            ModConfig.save();
        }, Icons.SnailICON);
        insectList.addOption("Sunset moth", ModConfig.sunsetMothToggle, checked -> {
            ModConfig.sunsetMothToggle = checked;
            ModConfig.save();
        }, Icons.SunsetMothICON);
        insectList.addOption("Tarantula", ModConfig.tarantulaToggle, checked -> {
            ModConfig.tarantulaToggle = checked;
            ModConfig.save();
        }, Icons.TarantulaICON);
        insectList.addOption("Tiger butterfly", ModConfig.tigerButterflyToggle, checked -> {
            ModConfig.tigerButterflyToggle = checked;
            ModConfig.save();
        }, Icons.TigerButterflyICON);
        insectList.addOption("Wasp", ModConfig.waspToggle, checked -> {
            ModConfig.waspToggle = checked;
            ModConfig.save();
        }, Icons.WaspICON);
        insectList.addOption("White butterfly", ModConfig.whiteButterflyToggle, checked -> {
            ModConfig.whiteButterflyToggle = checked;
            ModConfig.save();
        }, Icons.WhiteButterflyICON);
        insectList.addOption("Yellow butterfly", ModConfig.yellowButterflyToggle, checked -> {
            ModConfig.yellowButterflyToggle = checked;
            ModConfig.save();
        }, Icons.YellowButterflyICON);
        insectList.addOption("Yellow dragonfly", ModConfig.yellowDragonflyToggle, checked -> {
            ModConfig.yellowDragonflyToggle = checked;
            ModConfig.save();
        }, Icons.YellowDragonflyICON);

        // Shops
        int areaWidthShop = 140;
        int areaHeightShop = this.height - 80;
        int rowHeightShop = 20;
        int xShop = this.width - 160 - 150;
        int yShop = 20;

        CheckboxListWidget shopList = new CheckboxListWidget(MinecraftClient.getInstance(), xShop, yShop, areaWidthShop, areaHeightShop, rowHeightShop);
        shopList.addOption("Coffee Shop", ModConfig.coffeeShopToggle, checked -> {
            ModConfig.coffeeShopToggle = checked;
            ModConfig.save();
        }, Icons.CoffeeShopICON);
        shopList.addOption("Bakery", ModConfig.bakeryToggle, checked -> {
            ModConfig.bakeryToggle = checked;
            ModConfig.save();
        }, Icons.BakeryICON);
        shopList.addOption("Cocktail Bar", ModConfig.cocktailBarToggle, checked -> {
            ModConfig.cocktailBarToggle = checked;
            ModConfig.save();
        }, Icons.CocktailBarICON);
        shopList.addOption("Painting Shop", ModConfig.paintingShopToggle, checked -> {
            ModConfig.paintingShopToggle = checked;
            ModConfig.save();
        }, Icons.PaintingICON);
        shopList.addOption("Italian Restaurant", ModConfig.italianRestaurantToggle, checked -> {
            ModConfig.italianRestaurantToggle = checked;
            ModConfig.save();
        }, Icons.ItalianRestaurantICON);
        shopList.addOption("Herb Shop", ModConfig.herbShopToggle, checked -> {
            ModConfig.herbShopToggle = checked;
            ModConfig.save();
        }, Icons.HerbShopICON);


        this.addDrawableChild(toggleButtonDurability);
        this.addDrawableChild(toggleButtonTooltip);
        this.addDrawableChild(toggleButtonStats);
        this.addDrawableChild(toggleButtonOffHand);

        this.addDrawableChild(insectList);
        this.addDrawableChild(shopList);
        this.addDrawableChild(customHUDButton);

        // Close button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> this.close())
                .dimensions(xInsect + areaWidthInsect - 80, yInsect + areaHeightInsect + 20, 80, 20)
                .build());
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.fill(0, 0, this.width, this.height, 0x90000000);

        super.render(drawContext, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        super.close();
    }
}
