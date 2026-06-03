package fr.silv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.silv.hud.widget.config.ConfigOption;
import fr.silv.utils.ModLog;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Manages persistent configuration state for MineboxTools.
 */
public final class ModConfig {
    private static final Logger LOGGER = ModLog.getLogger(ModConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("mineboxtools.settings.json");
    @SuppressWarnings("null") // Map.of infers @NonNull values; assignment to less-annotated type is safe
    private static final Map<String, WidgetPos> DEFAULT_WIDGET_POSITIONS = Map.of(
            "durability_widget", new WidgetPos(10, 10),
            "icon_widget", new WidgetPos(10, 40),
            "stat_widget", new WidgetPos(10, 80)
    );

    private static ConfigState state = new ConfigState();

    private ModConfig() {
    }

    /**
     * Returns the general configuration section.
     *
     * @return mutable general settings container
     */
    public static General general() {
        return state.general;
    }

    /**
     * Returns the feature-toggle configuration section.
     *
     * @return mutable feature settings container
     */
    public static Features features() {
        return state.features;
    }

    /**
     * Returns the insect-visibility configuration section.
     *
     * @return mutable insect settings container
     */
    public static Insects insects() {
        return state.insects;
    }

    /**
     * Returns the shop-visibility configuration section.
     *
     * @return mutable shop settings container
     */
    public static Shops shops() {
        return state.shops;
    }

    /**
     * Returns the HUD configuration section.
     *
     * @return mutable HUD settings container
     */
    public static Hud hud() {
        return state.hud;
    }

    /**
     * Returns the configured HUD icon size preset.
     * @return the HUD icon size preset
     */
    public static IconSize getHudIconSize() {
        return hud().iconSize != null ? hud().iconSize : IconSize.NORMAL;
    }

    /**
     * Updates the HUD icon size preset.
     * @param iconSize the selected icon size preset
     */
    public static void setHudIconSize(IconSize iconSize) {
        hud().iconSize = iconSize != null ? iconSize : IconSize.NORMAL;
    }

    /**
     * Returns the configured HUD icon orientation.
     * @return the HUD icon orientation
     */
    public static IconOrientation getHudIconOrientation() {
        return hud().iconOrientation != null ? hud().iconOrientation : IconOrientation.HORIZONTAL;
    }

    /**
     * Updates the HUD icon orientation.
     * @param iconOrientation the selected icon orientation
     */
    public static void setHudIconOrientation(IconOrientation iconOrientation) {
        hud().iconOrientation = iconOrientation != null ? iconOrientation : IconOrientation.HORIZONTAL;
    }

    /**
     * Returns the configured HUD icon growth direction.
     * @return the HUD icon growth direction
     */
    public static IconDirection getHudIconDirection() {
        return hud().iconDirection != null ? hud().iconDirection : IconDirection.AUTO;
    }

    /**
     * Updates the HUD icon growth direction.
     * @param iconDirection the selected icon growth direction
     */
    public static void setHudIconDirection(IconDirection iconDirection) {
        hud().iconDirection = iconDirection != null ? iconDirection : IconDirection.AUTO;
    }

    /**
     * Returns the current UI language code.
     *
     * @return language identifier such as {@code en_us}, {@code fr_fr}
     */
    public static String getLanguage() {
        return general().language;
    }

    /**
     * Updates the active UI language code.
     *
     * @param language language identifier to store
     */
    public static void setLanguage(String language) {
        general().language = language;
    }

    /**
     * Returns how stats should be displayed in HUD/tooltips.
     *
     * @return configured stat display mode
     */
    public static ConfigOption getStatDisplay() {
        return features().statDisplay;
    }

    /**
     * Updates the stat display mode used by UI rendering.
     *
     * @param statDisplay new display mode
     */
    public static void setStatDisplay(ConfigOption statDisplay) {
        features().statDisplay = statDisplay;
    }

    /**
     * Returns whether the item highlight feature is enabled.
     *
     * @return {@code true} when golden slot borders are rendered
     */
    public static boolean isHighlightEnabled() {
        return features().highlightEnabled;
    }

    /**
     * Updates the item highlight enabled state.
     *
     * @param value {@code true} to enable golden slot borders
     */
    public static void setHighlightEnabled(boolean value) {
        features().highlightEnabled = value;
    }

    /**
     * Returns the minimum item score (0–100) required to display a highlight border.
     *
     * @return highlight score threshold
     */
    public static int getHighlightThreshold() {
        return features().highlightThreshold;
    }

    /**
     * Updates the minimum item score threshold for highlight borders.
     * The value is clamped to the range [0, 100].
     *
     * @param value new threshold value
     */
    public static void setHighlightThreshold(int value) {
        features().highlightThreshold = Math.max(0, Math.min(100, value));
    }

    /**
     * Returns whether a global feature flag is enabled.
     *
     * @param flag feature flag to query
     * @return {@code true} when the flag is enabled
     */
    public static boolean isEnabled(FeatureFlag flag) {
        return flag.get(state);
    }

    /**
     * Updates a global feature flag.
     *
     * @param flag feature flag to modify
     * @param value new enabled state
     */
    public static void setEnabled(FeatureFlag flag, boolean value) {
        flag.set(state, value);
    }

    /**
     * Returns whether an insect flag is enabled.
     *
     * @param flag insect flag to query
     * @return {@code true} when the flag is enabled
     */
    public static boolean isEnabled(InsectFlag flag) {
        return flag.get(state);
    }

    /**
     * Updates an insect visibility flag.
     *
     * @param flag insect flag to modify
     * @param value new enabled state
     */
    public static void setEnabled(InsectFlag flag, boolean value) {
        flag.set(state, value);
    }

    /**
     * Returns whether a shop flag is enabled.
     *
     * @param flag shop flag to query
     * @return {@code true} when the flag is enabled
     */
    public static boolean isEnabled(ShopFlag flag) {
        return flag.get(state);
    }

    /**
     * Updates a shop visibility flag.
     *
     * @param flag shop flag to modify
     * @param value new enabled state
     */
    public static void setEnabled(ShopFlag flag, boolean value) {
        flag.set(state, value);
    }

    /**
     * Stores a HUD widget position and size, then persists the updated configuration.
     *
     * @param id     widget identifier
     * @param x      widget X coordinate in scaled screen space
     * @param y      widget Y coordinate in scaled screen space
     * @param width  widget width at save time
     * @param height widget height at save time
     */
    public static void setWidgetPosition(String id, int x, int y, int width, int height) {
        hud().widgetPositions.put(id, new WidgetPos(x, y, width, height));
        save();
    }

    /**
     * Resolves a widget position from persisted state or built-in defaults.
     *
     * @param id widget identifier
     * @return two-element array containing X and Y coordinates
     */
    public static int[] getWidgetPosition(String id) {
        WidgetPos pos = hud().widgetPositions.getOrDefault(id, DEFAULT_WIDGET_POSITIONS.get(id));
        if (pos == null) {
            return new int[]{0, 0};
        }
        return new int[]{pos.x, pos.y};
    }

    /**
     * Resolves the saved widget size from persisted state, or returns the provided defaults.
     *
     * @param id            widget identifier
     * @param defaultWidth  fallback width when no saved size exists
     * @param defaultHeight fallback height when no saved size exists
     * @return two-element array containing width and height
     */
    public static int[] getWidgetSavedSize(String id, int defaultWidth, int defaultHeight) {
        WidgetPos pos = hud().widgetPositions.get(id);
        if (pos != null && pos.savedWidth > 0 && pos.savedHeight > 0) {
            return new int[]{pos.savedWidth, pos.savedHeight};
        }
        return new int[]{defaultWidth, defaultHeight};
    }

    /**
     * Loads persisted data into memory.
     */
    @SuppressWarnings("null") // Gson.fromJson is not annotated with @NonNull
    public static void load() {
        if (!Files.exists(CONFIG_FILE)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            ConfigState loadedState = GSON.fromJson(reader, ConfigState.class);
            boolean hadLegacy = loadedState.hasLegacyFields();
            state = loadedState.withDefaults();
            if (hadLegacy) {
                save();
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load config from {}", CONFIG_FILE, e);
        }
    }

    /**
     * Persists the current state to disk.
     */
    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
            GSON.toJson(state, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config to {}", CONFIG_FILE, e);
        }
    }

    private static final class ConfigState {
        private General general = new General();
        private Features features = new Features();
        private Insects insects = new Insects();
        private Shops shops = new Shops();
        private Hud hud = new Hud();

        private String language;
        private Boolean durabilityToggle;
        private Boolean tooltipToggle;
        private ConfigOption statToggle;
        private Boolean handToggle;
        private Boolean locationToggle;
        private Boolean thunderToggle;
        private Boolean rainToggle;
        private Boolean antToggle;
        private Boolean atlasMothToggle;
        private Boolean birdwingToggle;
        private Boolean blueButterflyToggle;
        private Boolean blueDragonflyToggle;
        private Boolean brownAntToggle;
        private Boolean centipedeToggle;
        private Boolean cricketToggle;
        private Boolean cyclommatusToggle;
        private Boolean dungBeetleToggle;
        private Boolean fireflyToggle;
        private Boolean greenButterflyToggle;
        private Boolean greenDragonflyToggle;
        private Boolean ladybugToggle;
        private Boolean locustToggle;
        private Boolean mantisToggle;
        private Boolean mosquitoToggle;
        private Boolean nightButterflyToggle;
        private Boolean purpleEmperorToggle;
        private Boolean redDragonflyToggle;
        private Boolean scorpionToggle;
        private Boolean snailToggle;
        private Boolean spiderToggle;
        private Boolean stickInsectToggle;
        private Boolean sunsetMothToggle;
        private Boolean tarantulaToggle;
        private Boolean tigerButterflyToggle;
        private Boolean waspToggle;
        private Boolean whiteButterflyToggle;
        private Boolean yellowButterflyToggle;
        private Boolean yellowDragonflyToggle;
        private Boolean coffeeShopToggle;
        private Boolean bakeryToggle;
        private Boolean cocktailBarToggle;
        private Boolean paintingShopToggle;
        private Boolean italianRestaurantToggle;
        private Boolean herbShopToggle;
        private Map<String, WidgetPos> widgetPositions;

        private ConfigState withDefaults() {
            if (general == null) general = new General();
            if (features == null) features = new Features();
            if (insects == null) insects = new Insects();
            if (shops == null) shops = new Shops();
            if (hud == null) hud = new Hud();
            if (hud.widgetPositions == null) hud.widgetPositions = new HashMap<>();
            if (hud.iconSize == null) hud.iconSize = IconSize.NORMAL;
            if (hud.iconOrientation == null) hud.iconOrientation = IconOrientation.HORIZONTAL;
            if (hud.iconDirection == null) hud.iconDirection = IconDirection.AUTO;
            if (features.highlightThreshold == 0 && !features.highlightEnabled) features.highlightThreshold = 50;
            applyLegacyValues();
            return this;
        }

        private void applyLegacyValues() {
            applyLegacy(language, value -> general.language = value);
            applyLegacy(statToggle, value -> features.statDisplay = value);

            applyLegacy(durabilityToggle, FeatureFlag.DURABILITY);
            applyLegacy(tooltipToggle, FeatureFlag.TOOLTIP);
            applyLegacy(handToggle, FeatureFlag.HAND);
            applyLegacy(locationToggle, FeatureFlag.LOCATION);
            applyLegacy(thunderToggle, FeatureFlag.THUNDER);
            applyLegacy(rainToggle, FeatureFlag.RAIN);

            applyLegacy(antToggle, InsectFlag.ANT);
            applyLegacy(atlasMothToggle, InsectFlag.ATLAS_MOTH);
            applyLegacy(birdwingToggle, InsectFlag.BIRDWING);
            applyLegacy(blueButterflyToggle, InsectFlag.BLUE_BUTTERFLY);
            applyLegacy(blueDragonflyToggle, InsectFlag.BLUE_DRAGONFLY);
            applyLegacy(brownAntToggle, InsectFlag.BROWN_ANT);
            applyLegacy(centipedeToggle, InsectFlag.CENTIPEDE);
            applyLegacy(cricketToggle, InsectFlag.CRICKET);
            applyLegacy(cyclommatusToggle, InsectFlag.CYCLOMMATUS);
            applyLegacy(dungBeetleToggle, InsectFlag.DUNG_BEETLE);
            applyLegacy(fireflyToggle, InsectFlag.FIREFLY);
            applyLegacy(greenButterflyToggle, InsectFlag.GREEN_BUTTERFLY);
            applyLegacy(greenDragonflyToggle, InsectFlag.GREEN_DRAGONFLY);
            applyLegacy(ladybugToggle, InsectFlag.LADYBUG);
            applyLegacy(locustToggle, InsectFlag.LOCUST);
            applyLegacy(mantisToggle, InsectFlag.MANTIS);
            applyLegacy(mosquitoToggle, InsectFlag.MOSQUITO);
            applyLegacy(nightButterflyToggle, InsectFlag.NIGHT_BUTTERFLY);
            applyLegacy(purpleEmperorToggle, InsectFlag.PURPLE_EMPEROR);
            applyLegacy(redDragonflyToggle, InsectFlag.RED_DRAGONFLY);
            applyLegacy(scorpionToggle, InsectFlag.SCORPION);
            applyLegacy(snailToggle, InsectFlag.SNAIL);
            applyLegacy(spiderToggle, InsectFlag.SPIDER);
            applyLegacy(stickInsectToggle, InsectFlag.STICK_INSECT);
            applyLegacy(sunsetMothToggle, InsectFlag.SUNSET_MOTH);
            applyLegacy(tarantulaToggle, InsectFlag.TARANTULA);
            applyLegacy(tigerButterflyToggle, InsectFlag.TIGER_BUTTERFLY);
            applyLegacy(waspToggle, InsectFlag.WASP);
            applyLegacy(whiteButterflyToggle, InsectFlag.WHITE_BUTTERFLY);
            applyLegacy(yellowButterflyToggle, InsectFlag.YELLOW_BUTTERFLY);
            applyLegacy(yellowDragonflyToggle, InsectFlag.YELLOW_DRAGONFLY);

            applyLegacy(coffeeShopToggle, ShopFlag.COFFEE);
            applyLegacy(bakeryToggle, ShopFlag.BAKERY);
            applyLegacy(cocktailBarToggle, ShopFlag.COCKTAIL_BAR);
            applyLegacy(paintingShopToggle, ShopFlag.PAINTING);
            applyLegacy(italianRestaurantToggle, ShopFlag.ITALIAN_RESTAURANT);
            applyLegacy(herbShopToggle, ShopFlag.HERB);

            if (widgetPositions != null && hud.widgetPositions.isEmpty()) {
                hud.widgetPositions.putAll(widgetPositions);
            }

            clearLegacyFields();
        }

        private boolean hasLegacyFields() {
            return language != null || statToggle != null || durabilityToggle != null
                    || widgetPositions != null || antToggle != null || coffeeShopToggle != null;
        }

        private void clearLegacyFields() {
            language = null;
            statToggle = null;
            durabilityToggle = null;
            tooltipToggle = null;
            handToggle = null;
            locationToggle = null;
            thunderToggle = null;
            rainToggle = null;
            antToggle = null;
            atlasMothToggle = null;
            birdwingToggle = null;
            blueButterflyToggle = null;
            blueDragonflyToggle = null;
            brownAntToggle = null;
            centipedeToggle = null;
            cricketToggle = null;
            cyclommatusToggle = null;
            dungBeetleToggle = null;
            fireflyToggle = null;
            greenButterflyToggle = null;
            greenDragonflyToggle = null;
            ladybugToggle = null;
            locustToggle = null;
            mantisToggle = null;
            mosquitoToggle = null;
            nightButterflyToggle = null;
            purpleEmperorToggle = null;
            redDragonflyToggle = null;
            scorpionToggle = null;
            snailToggle = null;
            spiderToggle = null;
            stickInsectToggle = null;
            sunsetMothToggle = null;
            tarantulaToggle = null;
            tigerButterflyToggle = null;
            waspToggle = null;
            whiteButterflyToggle = null;
            yellowButterflyToggle = null;
            yellowDragonflyToggle = null;
            coffeeShopToggle = null;
            bakeryToggle = null;
            cocktailBarToggle = null;
            paintingShopToggle = null;
            italianRestaurantToggle = null;
            herbShopToggle = null;
            widgetPositions = null;
        }

        private void applyLegacy(String value, Consumer<String> setter) {
            if (value != null) {
                setter.accept(value);
            }
        }

        private void applyLegacy(ConfigOption value, Consumer<ConfigOption> setter) {
            if (value != null) {
                setter.accept(value);
            }
        }

        private void applyLegacy(Boolean value, FeatureFlag flag) {
            if (value != null) {
                flag.set(this, value);
            }
        }

        private void applyLegacy(Boolean value, InsectFlag flag) {
            if (value != null) {
                flag.set(this, value);
            }
        }

        private void applyLegacy(Boolean value, ShopFlag flag) {
            if (value != null) {
                flag.set(this, value);
            }
        }
    }

    public static final class General {
        public String language = "en_us";
    }

    public static final class Features {
        public boolean durability = true;
        public boolean tooltip = true;
        public ConfigOption statDisplay = ConfigOption.SIMPLE;
        public boolean hand = true;
        public boolean location = true;
        public boolean thunder = true;
        public boolean rain = true;
        public boolean highlightEnabled = false;
        public int highlightThreshold = 50;
    }

    public static final class Insects {
        public boolean ant = false;
        public boolean atlasMoth = false;
        public boolean birdwing = false;
        public boolean blueButterfly = false;
        public boolean blueDragonfly = false;
        public boolean brownAnt = false;
        public boolean centipede = false;
        public boolean cricket = false;
        public boolean cyclommatus = false;
        public boolean dungBeetle = false;
        public boolean firefly = false;
        public boolean greenButterfly = false;
        public boolean greenDragonfly = false;
        public boolean ladybug = false;
        public boolean locust = false;
        public boolean mantis = false;
        public boolean mosquito = false;
        public boolean nightButterfly = false;
        public boolean purpleEmperor = false;
        public boolean redDragonfly = false;
        public boolean scorpion = false;
        public boolean snail = false;
        public boolean spider = false;
        public boolean stickInsect = false;
        public boolean sunsetMoth = false;
        public boolean tarantula = false;
        public boolean tigerButterfly = false;
        public boolean wasp = false;
        public boolean whiteButterfly = false;
        public boolean yellowButterfly = false;
        public boolean yellowDragonfly = false;
    }

    public static final class Shops {
        public boolean coffee = false;
        public boolean bakery = false;
        public boolean cocktailBar = false;
        public boolean painting = false;
        public boolean italianRestaurant = false;
        public boolean herb = false;
    }

    public static final class Hud {
        public Map<String, WidgetPos> widgetPositions = new HashMap<>();
        public IconSize iconSize = IconSize.NORMAL;
        public IconOrientation iconOrientation = IconOrientation.HORIZONTAL;
        public IconDirection iconDirection = IconDirection.AUTO;
    }

    public static final class WidgetPos {
        int x;
        int y;
        int savedWidth;
        int savedHeight;

        WidgetPos() {
        }

        WidgetPos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        WidgetPos(int x, int y, int savedWidth, int savedHeight) {
            this.x = x;
            this.y = y;
            this.savedWidth = savedWidth;
            this.savedHeight = savedHeight;
        }
    }

    /**
     * Supported icon size presets for HUD icon rendering.
     */
    public enum IconSize {
        SMALL(16, "mineboxtools.menu.icon_size.small"),
        NORMAL(24, "mineboxtools.menu.icon_size.normal"),
        LARGE(32, "mineboxtools.menu.icon_size.large");

        private final int pixels;
        private final String labelKey;

        IconSize(int pixels, String labelKey) {
            this.pixels = pixels;
            this.labelKey = labelKey;
        }

        public int getPixels() {
            return pixels;
        }

        public String getLabelKey() {
            return labelKey;
        }
    }

    /**
     * Supported icon stacking orientations for HUD icon rendering.
     */
    public enum IconOrientation {
        HORIZONTAL("mineboxtools.menu.icon_orientation.horizontal"),
        VERTICAL("mineboxtools.menu.icon_orientation.vertical");

        private final String labelKey;

        IconOrientation(String labelKey) {
            this.labelKey = labelKey;
        }

        public String getLabelKey() {
            return labelKey;
        }
    }

    /**
     * Supported icon growth directions for HUD icon rendering.
     */
    public enum IconDirection {
        AUTO("mineboxtools.menu.icon_direction.auto"),
        LEFT("mineboxtools.menu.icon_direction.left"),
        RIGHT("mineboxtools.menu.icon_direction.right"),
        UP("mineboxtools.menu.icon_direction.up"),
        DOWN("mineboxtools.menu.icon_direction.down");

        private final String labelKey;

        IconDirection(String labelKey) {
            this.labelKey = labelKey;
        }

        public String getLabelKey() {
            return labelKey;
        }
    }

    /**
     * Functional accessor used by enum flags to read booleans from config sections.
     *
     * @param <T> section type containing the boolean flag
     */
    private interface BooleanGetter<T> {
        boolean get(T target);
    }

    /**
     * Functional mutator used by enum flags to write booleans into config sections.
     *
     * @param <T> section type containing the boolean flag
     */
    private interface BooleanSetter<T> {
        void set(T target, boolean value);
    }

    /**
     * Enumerates toggleable global features exposed by MineboxTools.
     */
    public enum FeatureFlag {
        DURABILITY(section -> section.durability, (section, value) -> section.durability = value),
        TOOLTIP(section -> section.tooltip, (section, value) -> section.tooltip = value),
        HAND(section -> section.hand, (section, value) -> section.hand = value),
        LOCATION(section -> section.location, (section, value) -> section.location = value),
        THUNDER(section -> section.thunder, (section, value) -> section.thunder = value),
        RAIN(section -> section.rain, (section, value) -> section.rain = value);

        private final BooleanGetter<Features> getter;
        private final BooleanSetter<Features> setter;

        FeatureFlag(BooleanGetter<Features> getter, BooleanSetter<Features> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        private boolean get(ConfigState state) {
            return getter.get(state.features);
        }

        private void set(ConfigState state, boolean value) {
            setter.set(state.features, value);
        }
    }

    /**
     * Enumerates insect toggles used by availability filters and HUD rendering.
     */
    public enum InsectFlag {
        ANT(section -> section.ant, (section, value) -> section.ant = value),
        ATLAS_MOTH(section -> section.atlasMoth, (section, value) -> section.atlasMoth = value),
        BIRDWING(section -> section.birdwing, (section, value) -> section.birdwing = value),
        BLUE_BUTTERFLY(section -> section.blueButterfly, (section, value) -> section.blueButterfly = value),
        BLUE_DRAGONFLY(section -> section.blueDragonfly, (section, value) -> section.blueDragonfly = value),
        BROWN_ANT(section -> section.brownAnt, (section, value) -> section.brownAnt = value),
        CENTIPEDE(section -> section.centipede, (section, value) -> section.centipede = value),
        CRICKET(section -> section.cricket, (section, value) -> section.cricket = value),
        CYCLOMMATUS(section -> section.cyclommatus, (section, value) -> section.cyclommatus = value),
        DUNG_BEETLE(section -> section.dungBeetle, (section, value) -> section.dungBeetle = value),
        FIREFLY(section -> section.firefly, (section, value) -> section.firefly = value),
        GREEN_BUTTERFLY(section -> section.greenButterfly, (section, value) -> section.greenButterfly = value),
        GREEN_DRAGONFLY(section -> section.greenDragonfly, (section, value) -> section.greenDragonfly = value),
        LADYBUG(section -> section.ladybug, (section, value) -> section.ladybug = value),
        LOCUST(section -> section.locust, (section, value) -> section.locust = value),
        MANTIS(section -> section.mantis, (section, value) -> section.mantis = value),
        MOSQUITO(section -> section.mosquito, (section, value) -> section.mosquito = value),
        NIGHT_BUTTERFLY(section -> section.nightButterfly, (section, value) -> section.nightButterfly = value),
        PURPLE_EMPEROR(section -> section.purpleEmperor, (section, value) -> section.purpleEmperor = value),
        RED_DRAGONFLY(section -> section.redDragonfly, (section, value) -> section.redDragonfly = value),
        SCORPION(section -> section.scorpion, (section, value) -> section.scorpion = value),
        SNAIL(section -> section.snail, (section, value) -> section.snail = value),
        SPIDER(section -> section.spider, (section, value) -> section.spider = value),
        STICK_INSECT(section -> section.stickInsect, (section, value) -> section.stickInsect = value),
        SUNSET_MOTH(section -> section.sunsetMoth, (section, value) -> section.sunsetMoth = value),
        TARANTULA(section -> section.tarantula, (section, value) -> section.tarantula = value),
        TIGER_BUTTERFLY(section -> section.tigerButterfly, (section, value) -> section.tigerButterfly = value),
        WASP(section -> section.wasp, (section, value) -> section.wasp = value),
        WHITE_BUTTERFLY(section -> section.whiteButterfly, (section, value) -> section.whiteButterfly = value),
        YELLOW_BUTTERFLY(section -> section.yellowButterfly, (section, value) -> section.yellowButterfly = value),
        YELLOW_DRAGONFLY(section -> section.yellowDragonfly, (section, value) -> section.yellowDragonfly = value);

        private final BooleanGetter<Insects> getter;
        private final BooleanSetter<Insects> setter;

        InsectFlag(BooleanGetter<Insects> getter, BooleanSetter<Insects> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        private boolean get(ConfigState state) {
            return getter.get(state.insects);
        }

        private void set(ConfigState state, boolean value) {
            setter.set(state.insects, value);
        }
    }

    /**
     * Enumerates shop toggles used by world availability rendering.
     */
    public enum ShopFlag {
        COFFEE(section -> section.coffee, (section, value) -> section.coffee = value),
        BAKERY(section -> section.bakery, (section, value) -> section.bakery = value),
        COCKTAIL_BAR(section -> section.cocktailBar, (section, value) -> section.cocktailBar = value),
        PAINTING(section -> section.painting, (section, value) -> section.painting = value),
        ITALIAN_RESTAURANT(section -> section.italianRestaurant, (section, value) -> section.italianRestaurant = value),
        HERB(section -> section.herb, (section, value) -> section.herb = value);

        private final BooleanGetter<Shops> getter;
        private final BooleanSetter<Shops> setter;

        ShopFlag(BooleanGetter<Shops> getter, BooleanSetter<Shops> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        private boolean get(ConfigState state) {
            return getter.get(state.shops);
        }

        private void set(ConfigState state, boolean value) {
            setter.set(state.shops, value);
        }
    }
}
