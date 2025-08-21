package fr.silv.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Logger ModConfigLogger = LogManager.getLogger(ModConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "mineboxtools.settings.json");

    // Feature toggles
    public static boolean durabilityToggle = true;
    public static boolean tooltipToggle = true;

    // Insect toggles
    public static boolean antToggle = false;
    public static boolean atlasMothToggle = false;
    public static boolean birdwingToggle = false;
    public static boolean blueButterflyToggle = false;
    public static boolean blueDragonflyToggle = false;
    public static boolean brownAntToggle = false;
    public static boolean centipedeToggle = false;
    public static boolean cricketToggle = false;
    public static boolean cyclommatusToggle = false;
    public static boolean dungBeetleToggle = false;
    public static boolean fireflyToggle = false;
    public static boolean greenButterflyToggle = false;
    public static boolean greenDragonflyToggle = false;
    public static boolean ladybugToggle = false;
    public static boolean locustToggle = false;
    public static boolean mantisToggle = false;
    public static boolean mosquitoToggle = false;
    public static boolean nightButterflyToggle = false;
    public static boolean purpleEmperorToggle = false;
    public static boolean redDragonflyToggle = false;
    public static boolean scorpionToggle = false;
    public static boolean snailToggle = false;
    public static boolean spiderToggle = false;
    public static boolean stickInsectToggle = false;
    public static boolean sunsetMothToggle = false;
    public static boolean tarantulaToggle = false;
    public static boolean tigerButterflyToggle = false;
    public static boolean waspToggle = false;
    public static boolean whiteButterflyToggle = false;
    public static boolean yellowButterflyToggle = false;
    public static boolean yellowDragonflyToggle = false;

    // Shop toggles
    public static boolean coffeeShopToggle = false;
    public static boolean bakeryToggle = false;
    public static boolean cocktailBarToggle = false;
    public static boolean paintingShopToggle = false;
    public static boolean italianRestaurantToggle = false;
    public static boolean herbShopToggle = false;


    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ModConfigData data = GSON.fromJson(reader, ModConfigData.class);

                // Feature toggles
                durabilityToggle = data.durabilityToggle;
                tooltipToggle = data.tooltipToggle;

                // Insect toggles
                antToggle = data.antToggle;
                atlasMothToggle = data.atlasMothToggle;
                birdwingToggle = data.birdwingToggle;
                blueButterflyToggle = data.blueButterflyToggle;
                blueDragonflyToggle = data.blueDragonflyToggle;
                brownAntToggle = data.brownAntToggle;
                centipedeToggle = data.centipedeToggle;
                cricketToggle = data.cricketToggle;
                cyclommatusToggle = data.cyclommatusToggle;
                dungBeetleToggle = data.dungBeetleToggle;
                fireflyToggle = data.fireflyToggle;
                greenButterflyToggle = data.greenButterflyToggle;
                greenDragonflyToggle = data.greenDragonflyToggle;
                ladybugToggle = data.ladybugToggle;
                locustToggle = data.locustToggle;
                mantisToggle = data.mantisToggle;
                mosquitoToggle = data.mosquitoToggle;
                nightButterflyToggle = data.nightButterflyToggle;
                purpleEmperorToggle = data.purpleEmperorToggle;
                redDragonflyToggle = data.redDragonflyToggle;
                scorpionToggle = data.scorpionToggle;
                snailToggle = data.snailToggle;
                spiderToggle = data.spiderToggle;
                stickInsectToggle = data.stickInsectToggle;
                sunsetMothToggle = data.sunsetMothToggle;
                tarantulaToggle = data.tarantulaToggle;
                tigerButterflyToggle = data.tigerButterflyToggle;
                waspToggle = data.waspToggle;
                whiteButterflyToggle = data.whiteButterflyToggle;
                yellowButterflyToggle = data.yellowButterflyToggle;
                yellowDragonflyToggle = data.yellowDragonflyToggle;

                // Shop toggles
                coffeeShopToggle = data.coffeeShopToggle;
                bakeryToggle = data.bakeryToggle;
                cocktailBarToggle = data.cocktailBarToggle;
                paintingShopToggle = data.paintingShopToggle;
                italianRestaurantToggle = data.italianRestaurantToggle;
                herbShopToggle = data.herbShopToggle;

            } catch (IOException e) {
                ModConfigLogger.error(e.getMessage());
            }
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            ModConfigData data = new ModConfigData();

            // Feature toggles
            data.durabilityToggle = durabilityToggle;
            data.tooltipToggle = tooltipToggle;

            // Insect toggles
            data.antToggle = antToggle;
            data.atlasMothToggle = atlasMothToggle;
            data.birdwingToggle = birdwingToggle;
            data.blueButterflyToggle = blueButterflyToggle;
            data.blueDragonflyToggle = blueDragonflyToggle;
            data.brownAntToggle = brownAntToggle;
            data.centipedeToggle = centipedeToggle;
            data.cricketToggle = cricketToggle;
            data.cyclommatusToggle = cyclommatusToggle;
            data.dungBeetleToggle = dungBeetleToggle;
            data.fireflyToggle = fireflyToggle;
            data.greenButterflyToggle = greenButterflyToggle;
            data.greenDragonflyToggle = greenDragonflyToggle;
            data.ladybugToggle = ladybugToggle;
            data.locustToggle = locustToggle;
            data.mantisToggle = mantisToggle;
            data.mosquitoToggle = mosquitoToggle;
            data.nightButterflyToggle = nightButterflyToggle;
            data.purpleEmperorToggle = purpleEmperorToggle;
            data.redDragonflyToggle = redDragonflyToggle;
            data.scorpionToggle = scorpionToggle;
            data.snailToggle = snailToggle;
            data.spiderToggle = spiderToggle;
            data.stickInsectToggle = stickInsectToggle;
            data.sunsetMothToggle = sunsetMothToggle;
            data.tarantulaToggle = tarantulaToggle;
            data.tigerButterflyToggle = tigerButterflyToggle;
            data.waspToggle = waspToggle;
            data.whiteButterflyToggle = whiteButterflyToggle;
            data.yellowButterflyToggle = yellowButterflyToggle;
            data.yellowDragonflyToggle = yellowDragonflyToggle;

            // Shop toggles
            data.coffeeShopToggle = coffeeShopToggle;
            data.bakeryToggle = bakeryToggle;
            data.cocktailBarToggle = cocktailBarToggle;
            data.paintingShopToggle = paintingShopToggle;
            data.italianRestaurantToggle = italianRestaurantToggle;
            data.herbShopToggle = herbShopToggle;

            GSON.toJson(data, writer);
        } catch (IOException e) {
            ModConfigLogger.error(e.getMessage());
        }
    }

    private static class ModConfigData {

        // Feature toggles
        boolean durabilityToggle;
        boolean tooltipToggle;

        // Insect toggles
        boolean antToggle;
        boolean atlasMothToggle;
        boolean birdwingToggle;
        boolean blueButterflyToggle;
        boolean blueDragonflyToggle;
        boolean brownAntToggle;
        boolean centipedeToggle;
        boolean cricketToggle;
        boolean cyclommatusToggle;
        boolean dungBeetleToggle;
        boolean fireflyToggle;
        boolean greenButterflyToggle;
        boolean greenDragonflyToggle;
        boolean ladybugToggle;
        boolean locustToggle;
        boolean mantisToggle;
        boolean mosquitoToggle;
        boolean nightButterflyToggle;
        boolean purpleEmperorToggle;
        boolean redDragonflyToggle;
        boolean scorpionToggle;
        boolean snailToggle;
        boolean spiderToggle;
        boolean stickInsectToggle;
        boolean sunsetMothToggle;
        boolean tarantulaToggle;
        boolean tigerButterflyToggle;
        boolean waspToggle;
        boolean whiteButterflyToggle;
        boolean yellowButterflyToggle;
        boolean yellowDragonflyToggle;

        // Shop toggles
        boolean coffeeShopToggle;
        boolean bakeryToggle;
        boolean cocktailBarToggle;
        boolean paintingShopToggle;
        boolean italianRestaurantToggle;
        boolean herbShopToggle;
    }
}
