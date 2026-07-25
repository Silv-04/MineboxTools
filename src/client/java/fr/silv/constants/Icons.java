package fr.silv.constants;

import net.minecraft.resources.Identifier;

/**
 * Holds texture identifiers used by HUD elements and shop/insect icons.
 */
public final class Icons {
    private static final String NAMESPACE = "mineboxtools";

    private Icons() {
    }

    private static Identifier shop(String name) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, "textures/shops/" + name + ".png");
    }

    private static Identifier insect(String name) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, "textures/insects/" + name + ".png");
    }

    private static Identifier root(String name) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, "textures/" + name + ".png");
    }

    // Shops
    public static final Identifier BakeryICON = shop("yellow_macaron");
    public static final Identifier ItalianRestaurantICON = shop("cheese");
    public static final Identifier CocktailBarICON = shop("yellow_cocktail");
    public static final Identifier CoffeeShopICON = shop("yellow_coffee");
    public static final Identifier HerbShopICON = shop("herb");
    public static final Identifier PaintingICON = shop("painting");
    public static final Identifier SushiShopICON = shop("sushi");
    // Weather
    public static final Identifier ThunderICON = root("lightning");
    public static final Identifier RainICON = root("rain");

    // Insects
    public static final Identifier AntICON = insect("ant");
    public static final Identifier AtlasMothButterflyICON = insect("atlas_moth_butterfly");
    public static final Identifier BirdwingICON = insect("birdwing");
    public static final Identifier BlueButterflyICON = insect("blue_butterfly");
    public static final Identifier BlueDragonflyICON = insect("blue_dragonfly");
    public static final Identifier BrownAntICON = insect("brown_ant");
    public static final Identifier CentipedeICON = insect("centipede");
    public static final Identifier CricketICON = insect("cricket");
    public static final Identifier CyclommatusICON = insect("cyclommatus");
    public static final Identifier DungBeetleICON = insect("dung_beetle");
    public static final Identifier FireflyICON = insect("firefly");
    public static final Identifier GreenButterflyICON = insect("green_butterfly");
    public static final Identifier GreenDragonflyICON = insect("green_dragonfly");
    public static final Identifier LadybugICON = insect("ladybug");
    public static final Identifier LocustICON = insect("locust");
    public static final Identifier MantisICON = insect("mantis");
    public static final Identifier MosquitoICON = insect("mosquito");
    public static final Identifier NightButterflyICON = insect("night_butterfly");
    public static final Identifier PurpleEmperorICON = insect("purple_emperor");
    public static final Identifier RedDragonflyICON = insect("red_dragonfly");
    public static final Identifier ScorpionICON = insect("scorpion");
    public static final Identifier SnailICON = insect("snail");
    public static final Identifier SpiderICON = insect("spider");
    public static final Identifier StickInsectICON = insect("stick_insect");
    public static final Identifier SunsetMothICON = insect("sunset_moth");
    public static final Identifier TarantulaICON = insect("tarantula");
    public static final Identifier TigerButterflyICON = insect("tiger_butterfly");
    public static final Identifier WaspICON = insect("wasp");
    public static final Identifier WhiteButterflyICON = insect("white_butterfly");
    public static final Identifier YellowButterflyICON = insect("yellow_butterfly");
    public static final Identifier YellowDragonflyICON = insect("yellow_dragonfly");
}
