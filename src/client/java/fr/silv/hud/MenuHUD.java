package fr.silv.hud;

import fr.silv.Lang;
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
        Lang.load(ModConfig.language);

        ButtonWidget langButton = ButtonWidget.builder(
                Text.of(ModConfig.language.equals("en_us") ? "EN" : "FR"),
                b -> {
                    ModConfig.language = ModConfig.language.equals("en_us") ? "fr_fr" : "en_us";
                    Lang.load(ModConfig.language);
                    ModConfig.save();
                    this.clearAndInit();
                }
        ).dimensions(20, 20, 160, 20).build();

        // Durability
        CyclingButtonWidget<Boolean> toggleButtonDurability = CyclingButtonWidget.onOffBuilder(ModConfig.durabilityToggle)
                .build(20, 60, 160, 20, Text.literal(Lang.get("mineboxtools.menu.durability")),
                        (button, value) -> {
                    ModConfig.durabilityToggle = value;
                    ModConfig.save();
                        });

        // Tooltip
        CyclingButtonWidget<Boolean> toggleButtonTooltip = CyclingButtonWidget.onOffBuilder(ModConfig.tooltipToggle)
                .build(20, 80, 160, 20, Text.literal(Lang.get("mineboxtools.menu.tooltip")),
                        (button, value) -> {
                            ModConfig.tooltipToggle = value;
                            ModConfig.save();
                        });

        // Stats
        CyclingButtonWidget<ConfigOption> toggleButtonStats = CyclingButtonWidget.builder(ConfigOption::getDisplayName)
                .values(ConfigOption.values())
                .initially(ModConfig.statToggle)
                .build(20, 100, 160, 20, Text.literal(Lang.get("mineboxtools.menu.stats")),
                        (button, value) -> {
                            ModConfig.statToggle = value;
                            ModConfig.save();
                        });

        // Off hand haversack amount inside toggle
        CyclingButtonWidget<Boolean> toggleButtonOffHand = CyclingButtonWidget.onOffBuilder(ModConfig.handToggle)
                .build(20, 120, 160, 20, Text.literal(Lang.get("mineboxtools.menu.hand")),
                        (button, value) -> {
                            ModConfig.handToggle = value;
                            ModConfig.save();
                        });

        // Location of the harvestable on the item tooltip
        CyclingButtonWidget<Boolean> toggleLocation = CyclingButtonWidget.onOffBuilder(ModConfig.locationToggle)
                .build(20, 140, 160, 20, Text.literal(Lang.get("mineboxtools.menu.location")),
                        (button, value) -> {
                            ModConfig.locationToggle = value;
                            ModConfig.save();
                        });

        // Custom HUD
        ButtonWidget customHUDButton = ButtonWidget.builder(Text.literal(Lang.get("mineboxtools.menu.hud")), button -> {
            MinecraftClient.getInstance().setScreen(new HudConfigScreen(HudWidgetManager.getWidgets()));
        }).dimensions(20, 180, 160, 20).build();

        // Insects
        int areaWidthInsect = 140;
        int areaHeightInsect = this.height - 80;
        int rowHeightInsect = 20;
        int xInsect = this.width - 160;
        int yInsect = 20;

        CheckboxListWidget insectList = new CheckboxListWidget(MinecraftClient.getInstance(), xInsect, yInsect, areaWidthInsect, areaHeightInsect, rowHeightInsect);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.ant")), ModConfig.antToggle, checked -> {
            ModConfig.antToggle = checked;
            ModConfig.save();
        }, Icons.AntICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.atlas_moth")), ModConfig.atlasMothToggle, checked -> {
            ModConfig.atlasMothToggle = checked;
            ModConfig.save();
        }, Icons.AtlasMothButterflyICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.birdwing")), ModConfig.birdwingToggle, checked -> {
            ModConfig.birdwingToggle = checked;
            ModConfig.save();
        }, Icons.BirdwingICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.blue_butterfly")), ModConfig.blueButterflyToggle, checked -> {
            ModConfig.blueButterflyToggle = checked;
            ModConfig.save();
        }, Icons.BlueButterflyICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.blue_dragonfly")), ModConfig.blueDragonflyToggle, checked -> {
            ModConfig.blueDragonflyToggle = checked;
            ModConfig.save();
        }, Icons.BlueDragonflyICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.brown_ant")), ModConfig.brownAntToggle, checked -> {
            ModConfig.brownAntToggle = checked;
            ModConfig.save();
        }, Icons.BrownAntICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.centipede")), ModConfig.centipedeToggle, checked -> {
            ModConfig.centipedeToggle = checked;
            ModConfig.save();
        }, Icons.CentipedeICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.cricket")), ModConfig.cricketToggle, checked -> {
            ModConfig.cricketToggle = checked;
            ModConfig.save();
        }, Icons.CricketICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.cyclommatus")), ModConfig.cyclommatusToggle, checked -> {
            ModConfig.cyclommatusToggle = checked;
            ModConfig.save();
        }, Icons.CyclommatusICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.dung_bettle")), ModConfig.dungBeetleToggle, checked -> {
            ModConfig.dungBeetleToggle = checked;
            ModConfig.save();
        }, Icons.DungleBeetleICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.firefly")), ModConfig.fireflyToggle, checked -> {
            ModConfig.fireflyToggle = checked;
            ModConfig.save();
        }, Icons.FireflyICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.green_butterfly")), ModConfig.greenButterflyToggle, checked -> {
            ModConfig.greenButterflyToggle = checked;
            ModConfig.save();
        }, Icons.GreenButterflyICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.green_dragonfly")), ModConfig.greenDragonflyToggle, checked -> {
            ModConfig.greenDragonflyToggle = checked;
            ModConfig.save();
        }, Icons.GreenDragonflyICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.ladybug")), ModConfig.ladybugToggle, checked -> {
            ModConfig.ladybugToggle = checked;
            ModConfig.save();
        }, Icons.LadybugICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.locust")), ModConfig.locustToggle, checked -> {
            ModConfig.locustToggle = checked;
            ModConfig.save();
        }, Icons.LocustICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.mantis")), ModConfig.mantisToggle, checked -> {
            ModConfig.mantisToggle = checked;
            ModConfig.save();
        }, Icons.MantisICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.night_butterfly")), ModConfig.nightButterflyToggle, checked -> {
            ModConfig.nightButterflyToggle = checked;
            ModConfig.save();
        }, Icons.NightButterflyICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.purple_emperor")), ModConfig.purpleEmperorToggle, checked -> {
            ModConfig.purpleEmperorToggle = checked;
            ModConfig.save();
        }, Icons.PurpleEmperorICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.red_dragonfly")), ModConfig.redDragonflyToggle, checked -> {
            ModConfig.redDragonflyToggle = checked;
            ModConfig.save();
        }, Icons.RedDragonflyICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.scorpion")), ModConfig.scorpionToggle, checked -> {
            ModConfig.scorpionToggle = checked;
            ModConfig.save();
        }, Icons.ScorpionICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.spider")), ModConfig.spiderToggle, checked -> {
            ModConfig.spiderToggle = checked;
            ModConfig.save();
        }, Icons.SpiderICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.stick_insect")), ModConfig.stickInsectToggle, checked -> {
            ModConfig.stickInsectToggle = checked;
            ModConfig.save();
        }, Icons.StickInsectICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.snail")), ModConfig.snailToggle, checked -> {
            ModConfig.snailToggle = checked;
            ModConfig.save();
        }, Icons.SnailICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.sunset_moth")), ModConfig.sunsetMothToggle, checked -> {
            ModConfig.sunsetMothToggle = checked;
            ModConfig.save();
        }, Icons.SunsetMothICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.tarantula")), ModConfig.tarantulaToggle, checked -> {
            ModConfig.tarantulaToggle = checked;
            ModConfig.save();
        }, Icons.TarantulaICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.tiger_butterfly")), ModConfig.tigerButterflyToggle, checked -> {
            ModConfig.tigerButterflyToggle = checked;
            ModConfig.save();
        }, Icons.TigerButterflyICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.wasp")), ModConfig.waspToggle, checked -> {
            ModConfig.waspToggle = checked;
            ModConfig.save();
        }, Icons.WaspICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.white_butterfly")), ModConfig.whiteButterflyToggle, checked -> {
            ModConfig.whiteButterflyToggle = checked;
            ModConfig.save();
        }, Icons.WhiteButterflyICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.yellow_butterfly")), ModConfig.yellowButterflyToggle, checked -> {
            ModConfig.yellowButterflyToggle = checked;
            ModConfig.save();
        }, Icons.YellowButterflyICON);
        insectList.addOption(Text.of(Lang.get("mineboxtools.insect.yellow_dragonfly")), ModConfig.yellowDragonflyToggle, checked -> {
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
        shopList.addOption(Text.of(Lang.get("mineboxtools.shop.coffee")), ModConfig.coffeeShopToggle, checked -> {
            ModConfig.coffeeShopToggle = checked;
            ModConfig.save();
        }, Icons.CoffeeShopICON);
        shopList.addOption(Text.of(Lang.get("mineboxtools.shop.bakery")), ModConfig.bakeryToggle, checked -> {
            ModConfig.bakeryToggle = checked;
            ModConfig.save();
        }, Icons.BakeryICON);
        shopList.addOption(Text.of(Lang.get("mineboxtools.shop.bar")), ModConfig.cocktailBarToggle, checked -> {
            ModConfig.cocktailBarToggle = checked;
            ModConfig.save();
        }, Icons.CocktailBarICON);
        shopList.addOption(Text.of(Lang.get("mineboxtools.shop.paint")), ModConfig.paintingShopToggle, checked -> {
            ModConfig.paintingShopToggle = checked;
            ModConfig.save();
        }, Icons.PaintingICON);
        shopList.addOption(Text.of(Lang.get("mineboxtools.shop.restaurant")), ModConfig.italianRestaurantToggle, checked -> {
            ModConfig.italianRestaurantToggle = checked;
            ModConfig.save();
        }, Icons.ItalianRestaurantICON);
        shopList.addOption(Text.of(Lang.get("mineboxtools.shop.herb")), ModConfig.herbShopToggle, checked -> {
            ModConfig.herbShopToggle = checked;
            ModConfig.save();
        }, Icons.HerbShopICON);


        this.addDrawableChild(langButton);
        this.addDrawableChild(toggleButtonDurability);
        this.addDrawableChild(toggleButtonTooltip);
        this.addDrawableChild(toggleButtonStats);
        this.addDrawableChild(toggleButtonOffHand);
        this.addDrawableChild(toggleLocation);

        this.addDrawableChild(insectList);
        this.addDrawableChild(shopList);
        this.addDrawableChild(customHUDButton);

        // Close button
        this.addDrawableChild(ButtonWidget.builder(Text.literal(Lang.get("mineboxtools.menu.close")), b -> this.close())
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
