package fr.silv.availability;

import fr.silv.ModConfig;
import fr.silv.constants.DaylightCycle;
import fr.silv.constants.Icons;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Central registry for item availability lookups.
 */
public final class AvailabilityRegistry {
        private static final List<AvailabilityEntry> ALL_ENTRIES = List.of(
                        insect("mineboxtools.insect.ant", Icons.AntICON, ModConfig.InsectFlag.ANT,
                                        clearDuring(AvailabilitySlot.MORNING, AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.MORNING, 0, AvailabilitySlot.AFTERNOON, 0)),
                        insect("mineboxtools.insect.atlas_moth", Icons.AtlasMothButterflyICON,
                                        ModConfig.InsectFlag.ATLAS_MOTH,
                                        clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.EVENING, 10, AvailabilitySlot.NIGHT, 10)),
                        insect("mineboxtools.insect.birdwing", Icons.BirdwingICON, ModConfig.InsectFlag.BIRDWING,
                                        clearDuring(AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.AFTERNOON, 11)),
                        insect("mineboxtools.insect.blue_butterfly", Icons.BlueButterflyICON,
                                        ModConfig.InsectFlag.BLUE_BUTTERFLY,
                                        clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.EVENING, 6, AvailabilitySlot.NIGHT, 6)),
                        insect("mineboxtools.insect.blue_dragonfly", Icons.BlueDragonflyICON,
                                        ModConfig.InsectFlag.BLUE_DRAGONFLY,
                                        clearDuring(AvailabilitySlot.MORNING, AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.MORNING, 6, AvailabilitySlot.AFTERNOON, 6)),
                        insect("mineboxtools.insect.brown_ant", Icons.BrownAntICON, ModConfig.InsectFlag.BROWN_ANT,
                                        clearDuring(AvailabilitySlot.MORNING, AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.MORNING, 7, AvailabilitySlot.AFTERNOON, 9)),
                        insect("mineboxtools.insect.centipede", Icons.CentipedeICON, ModConfig.InsectFlag.CENTIPEDE,
                                        clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.EVENING, 4, AvailabilitySlot.NIGHT, 4)),
                        insect("mineboxtools.insect.cricket", Icons.CricketICON, ModConfig.InsectFlag.CRICKET,
                                        clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.EVENING, 0, AvailabilitySlot.NIGHT, 0)),
                        insect("mineboxtools.insect.cyclommatus", Icons.CyclommatusICON,
                                        ModConfig.InsectFlag.CYCLOMMATUS,
                                        clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.EVENING, 1, AvailabilitySlot.NIGHT, 1)),
                        insect("mineboxtools.insect.dung_beetle", Icons.DungBeetleICON,
                                        ModConfig.InsectFlag.DUNG_BEETLE,
                                        clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.EVENING, 5, AvailabilitySlot.NIGHT, 5)),
                        insect("mineboxtools.insect.firefly", Icons.FireflyICON, ModConfig.InsectFlag.FIREFLY,
                                        clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.EVENING, 7, AvailabilitySlot.NIGHT, 7)),
                        insect("mineboxtools.insect.green_butterfly", Icons.GreenButterflyICON,
                                        ModConfig.InsectFlag.GREEN_BUTTERFLY,
                                        clearDuring(AvailabilitySlot.MORNING),
                                        order(AvailabilitySlot.MORNING, 2)),
                        insect("mineboxtools.insect.green_dragonfly", Icons.GreenDragonflyICON,
                                        ModConfig.InsectFlag.GREEN_DRAGONFLY,
                                        clearDuring(AvailabilitySlot.MORNING, AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.MORNING, 4, AvailabilitySlot.AFTERNOON, 4)),
                        insect("mineboxtools.insect.ladybug", Icons.LadybugICON, ModConfig.InsectFlag.LADYBUG,
                                        clearDuring(AvailabilitySlot.MORNING),
                                        order(AvailabilitySlot.MORNING, 9)),
                        insect("mineboxtools.insect.locust", Icons.LocustICON, ModConfig.InsectFlag.LOCUST,
                                        clearDuring(AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.AFTERNOON, 1)),
                        insect("mineboxtools.insect.mantis", Icons.MantisICON, ModConfig.InsectFlag.MANTIS,
                                        clearDuring(AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.AFTERNOON, 8)),
                        insect("mineboxtools.insect.mosquito", Icons.MosquitoICON, ModConfig.InsectFlag.MOSQUITO,
                                        during(AvailabilitySlot.ALL_DAY),
                                        order(AvailabilitySlot.ALL_DAY, 0)),
                        insect("mineboxtools.insect.night_butterfly", Icons.NightButterflyICON,
                                        ModConfig.InsectFlag.NIGHT_BUTTERFLY,
                                        clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.EVENING, 8, AvailabilitySlot.NIGHT, 8)),
                        insect("mineboxtools.insect.purple_emperor", Icons.PurpleEmperorICON,
                                        ModConfig.InsectFlag.PURPLE_EMPEROR,
                                        fullOrNewMoonNight(),
                                        order(AvailabilitySlot.SPECIAL, 1)),
                        insect("mineboxtools.insect.red_dragonfly", Icons.RedDragonflyICON,
                                        ModConfig.InsectFlag.RED_DRAGONFLY,
                                        clearDuring(AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.AFTERNOON, 7)),
                        insect("mineboxtools.insect.scorpion", Icons.ScorpionICON, ModConfig.InsectFlag.SCORPION,
                                        during(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.SPECIAL, 0)),
                        insect("mineboxtools.insect.snail", Icons.SnailICON, ModConfig.InsectFlag.SNAIL,
                                        badWeather(),
                                        order(AvailabilitySlot.BAD_WEATHER, 0)),
                        insect("mineboxtools.insect.spider", Icons.SpiderICON, ModConfig.InsectFlag.SPIDER,
                                        clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.EVENING, 3, AvailabilitySlot.NIGHT, 3)),
                        insect("mineboxtools.insect.stick_insect", Icons.StickInsectICON,
                                        ModConfig.InsectFlag.STICK_INSECT,
                                        clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.EVENING, 2, AvailabilitySlot.NIGHT, 2)),
                        insect("mineboxtools.insect.sunset_moth", Icons.SunsetMothICON,
                                        ModConfig.InsectFlag.SUNSET_MOTH,
                                        clearDuring(AvailabilitySlot.EVENING),
                                        order(AvailabilitySlot.EVENING, 11)),
                        insect("mineboxtools.insect.tarantula", Icons.TarantulaICON, ModConfig.InsectFlag.TARANTULA,
                                        clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                                        order(AvailabilitySlot.EVENING, 9, AvailabilitySlot.NIGHT, 9)),
                        insect("mineboxtools.insect.tiger_butterfly", Icons.TigerButterflyICON,
                                        ModConfig.InsectFlag.TIGER_BUTTERFLY,
                                        clearDuring(AvailabilitySlot.MORNING, AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.MORNING, 8, AvailabilitySlot.AFTERNOON, 12)),
                        insect("mineboxtools.insect.wasp", Icons.WaspICON, ModConfig.InsectFlag.WASP,
                                        clearDuring(AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.AFTERNOON, 10)),
                        insect("mineboxtools.insect.white_butterfly", Icons.WhiteButterflyICON,
                                        ModConfig.InsectFlag.WHITE_BUTTERFLY,
                                        clearDuring(AvailabilitySlot.MORNING, AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.MORNING, 1, AvailabilitySlot.AFTERNOON, 2)),
                        insect("mineboxtools.insect.yellow_butterfly", Icons.YellowButterflyICON,
                                        ModConfig.InsectFlag.YELLOW_BUTTERFLY,
                                        clearDuring(AvailabilitySlot.MORNING, AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.MORNING, 3, AvailabilitySlot.AFTERNOON, 3)),
                        insect("mineboxtools.insect.yellow_dragonfly", Icons.YellowDragonflyICON,
                                        ModConfig.InsectFlag.YELLOW_DRAGONFLY,
                                        clearDuring(AvailabilitySlot.MORNING, AvailabilitySlot.AFTERNOON),
                                        order(AvailabilitySlot.MORNING, 5, AvailabilitySlot.AFTERNOON, 5)),
                        worldFeature("mineboxtools.weather.thunder", Icons.ThunderICON, ModConfig.FeatureFlag.THUNDER,
                                        thundering(),
                                        order(AvailabilitySlot.WEATHER, 0)),
                        worldFeature("mineboxtools.weather.rain", Icons.RainICON, ModConfig.FeatureFlag.RAIN,
                                        rainWithoutThunder(),
                                        order(AvailabilitySlot.WEATHER, 1)),
                        worldShop("mineboxtools.shop.coffee", Icons.CoffeeShopICON, ModConfig.ShopFlag.COFFEE,
                                        notThunderingAnd(during(AvailabilitySlot.MORNING)),
                                        order(AvailabilitySlot.SHOP, 0)),
                        worldShop("mineboxtools.shop.bakery", Icons.BakeryICON, ModConfig.ShopFlag.BAKERY,
                                        notThunderingAnd(during(AvailabilitySlot.AFTERNOON)),
                                        order(AvailabilitySlot.SHOP, 1)),
                        worldShop("mineboxtools.shop.bar", Icons.CocktailBarICON, ModConfig.ShopFlag.COCKTAIL_BAR,
                                        cocktailAndMonkeyShopOpen(),
                                        order(AvailabilitySlot.SHOP, 2)),
                        worldShop("mineboxtools.shop.paint", Icons.PaintingICON, ModConfig.ShopFlag.PAINTING,
                                        cocktailAndMonkeyShopOpen(),
                                        order(AvailabilitySlot.SHOP, 3)),
                        worldShop("mineboxtools.shop.restaurant", Icons.ItalianRestaurantICON,
                                        ModConfig.ShopFlag.ITALIAN_RESTAURANT,
                                        italianRestaurantOpen(),
                                        order(AvailabilitySlot.SHOP, 4)),
                        worldShop("mineboxtools.shop.herb", Icons.HerbShopICON, ModConfig.ShopFlag.HERB,
                                        herbShopOpenOnFullMoon(),
                                        order(AvailabilitySlot.SHOP, 5)),
                        worldShop("mineboxtools.shop.sushi", Icons.SushiShopICON, ModConfig.ShopFlag.SUSHI,
                                        cocktailAndMonkeyShopOpen(),
                                        order(AvailabilitySlot.SHOP, 6)));
        private static final Map<AvailabilitySection, List<AvailabilityEntry>> ENTRIES_BY_SECTION = buildEntriesBySection();
        private static final Map<AvailabilitySlot, List<AvailabilityEntry>> ENTRIES_BY_SLOT = buildEntriesBySlot();

        private AvailabilityRegistry() {
        }

        /**
         * Returns all entries that belong to the requested section.
         * This method does not apply time/weather visibility filtering and is mainly
         * intended for configuration UI lists.
         *
         * @param section section to retrieve entries for
         * @return immutable list of entries for the section, or an empty list
         */
        public static List<AvailabilityEntry> entriesForSection(AvailabilitySection section) {
                return ENTRIES_BY_SECTION.getOrDefault(section, List.of());
        }

        /**
         * Returns visible entries for a specific availability slot.
         * Each entry is evaluated against its runtime visibility rule using world
         * conditions and current time.
         *
         * @param slot slot to evaluate
         * @param level current world state
         * @param now current reference time
         * @return ordered list of entries that are currently visible for the slot
         */
        public static List<AvailabilityEntry> entriesForSlot(AvailabilitySlot slot, Level level, LocalTime now) {
                return ENTRIES_BY_SLOT.getOrDefault(slot, List.of()).stream()
                                .filter(entry -> entry.isVisible(level, now))
                                .toList();
        }

        /**
         * Builds an insect availability entry bound to an insect config flag.
         *
         * @param langKey translation key for the display label
         * @param icon icon identifier shown in HUD/menu
         * @param flag config flag controlling enabled state
         * @param visibilityRule runtime visibility rule
         * @param displayOrder ordering map per availability slot
         * @return configured insect entry
         */
        private static AvailabilityEntry insect(String langKey, Identifier icon, ModConfig.InsectFlag flag,
                        BiPredicate<Level, LocalTime> visibilityRule,
                        Map<AvailabilitySlot, Integer> displayOrder) {
                return new AvailabilityEntry(
                                langKey,
                                icon,
                                AvailabilitySection.INSECTS,
                                () -> ModConfig.isEnabled(flag),
                                value -> ModConfig.setEnabled(flag, value),
                                visibilityRule,
                                displayOrder);
        }

        /**
         * Builds a world-feature entry (for example rain or thunder indicators).
         *
         * @param langKey translation key for the display label
         * @param icon icon identifier shown in HUD/menu
         * @param flag config flag controlling enabled state
         * @param visibilityRule runtime visibility rule
         * @param displayOrder ordering map per availability slot
         * @return configured world-feature entry
         */
        private static AvailabilityEntry worldFeature(String langKey, Identifier icon, ModConfig.FeatureFlag flag,
                        BiPredicate<Level, LocalTime> visibilityRule,
                        Map<AvailabilitySlot, Integer> displayOrder) {
                return new AvailabilityEntry(
                                langKey,
                                icon,
                                AvailabilitySection.WORLD,
                                () -> ModConfig.isEnabled(flag),
                                value -> ModConfig.setEnabled(flag, value),
                                visibilityRule,
                                displayOrder);
        }

        /**
         * Builds a world-shop entry controlled by a shop config flag.
         *
         * @param langKey translation key for the display label
         * @param icon icon identifier shown in HUD/menu
         * @param flag config flag controlling enabled state
         * @param visibilityRule runtime visibility rule
         * @param displayOrder ordering map per availability slot
         * @return configured shop entry
         */
        private static AvailabilityEntry worldShop(String langKey, Identifier icon, ModConfig.ShopFlag flag,
                        BiPredicate<Level, LocalTime> visibilityRule,
                        Map<AvailabilitySlot, Integer> displayOrder) {
                return new AvailabilityEntry(
                                langKey,
                                icon,
                                AvailabilitySection.WORLD,
                                () -> ModConfig.isEnabled(flag),
                                value -> ModConfig.setEnabled(flag, value),
                                visibilityRule,
                                displayOrder);
        }

        /**
         * Creates a slot-order map from paired varargs values.
         * Expected format: {@code slot1, order1, slot2, order2, ...}.
         *
         * @param values alternating slot and order values
         * @return immutable order map keyed by slot
         */
        private static Map<AvailabilitySlot, Integer> order(Object... values) {
                Map<AvailabilitySlot, Integer> order = new EnumMap<>(AvailabilitySlot.class);
                for (int i = 0; i < values.length; i += 2) {
                        order.put((AvailabilitySlot) values[i], (Integer) values[i + 1]);
                }
                return Map.copyOf(order);
        }

        /**
         * Indexes entries by section to speed up configuration/UI lookups.
         *
         * @return immutable map of section to section entries
         */
        private static Map<AvailabilitySection, List<AvailabilityEntry>> buildEntriesBySection() {
                Map<AvailabilitySection, List<AvailabilityEntry>> entriesBySection = new EnumMap<>(
                                AvailabilitySection.class);
                for (AvailabilitySection section : AvailabilitySection.values()) {
                        entriesBySection.put(section, ALL_ENTRIES.stream()
                                        .filter(entry -> entry.isInSection(section))
                                        .toList());
                }
                return Map.copyOf(entriesBySection);
        }

        /**
         * Indexes and sorts entries by availability slot.
         * Sorting is based on per-slot priorities returned by
         * {@link AvailabilityEntry#orderFor(AvailabilitySlot)}.
         *
         * @return immutable map of slot to sorted entries
         */
        private static Map<AvailabilitySlot, List<AvailabilityEntry>> buildEntriesBySlot() {
                Map<AvailabilitySlot, List<AvailabilityEntry>> entriesBySlot = new EnumMap<>(AvailabilitySlot.class);
                for (AvailabilitySlot slot : AvailabilitySlot.values()) {
                        entriesBySlot.put(slot, ALL_ENTRIES.stream()
                                        .filter(entry -> entry.orderFor(slot) != null)
                                        .sorted(Comparator.comparingInt(entry -> entry.orderFor(slot)))
                                        .toList());
                }
                return Map.copyOf(entriesBySlot);
        }

        /**
         * Creates a rule that matches one or more time slots.
         *
         * @param slots accepted slots
         * @return predicate that is true when current time matches at least one slot
         */
        private static BiPredicate<Level, LocalTime> during(AvailabilitySlot... slots) {
                return (level, now) -> {
                        for (AvailabilitySlot slot : slots) {
                                if (matchesSlot(now, slot)) {
                                        return true;
                                }
                        }
                        return false;
                };
        }

        /**
         * Variant of {@link #during(AvailabilitySlot...)} that also requires clear weather.
         *
         * @param slots time-based accepted slots
         * @return predicate that is true only in clear weather and matching slot
         */
        private static BiPredicate<Level, LocalTime> clearDuring(AvailabilitySlot... slots) {
                return clearWeather(during(slots));
        }

        /**
         * Composes a rule with an additional "no bad weather" constraint.
         *
         * @param rule base rule to compose
         * @return composed rule valid only in clear weather
         */
        private static BiPredicate<Level, LocalTime> clearWeather(BiPredicate<Level, LocalTime> rule) {
                return (level, now) -> !isBadWeather(level) && rule.test(level, now);
        }

        /**
         * Composes a rule that excludes thunderstorm periods.
         *
         * @param rule base rule to compose
         * @return composed rule valid only when not thundering and base rule passes
         */
        private static BiPredicate<Level, LocalTime> notThunderingAnd(BiPredicate<Level, LocalTime> rule) {
                return (level, now) -> !level.isThundering() && rule.test(level, now);
        }

        /**
         * Creates a visibility rule active only during bad weather.
         *
         * @return predicate that is true when raining or thundering
         */
        private static BiPredicate<Level, LocalTime> badWeather() {
                return (level, now) -> isBadWeather(level);
        }

        /**
         * Creates a visibility rule active only during thunderstorms.
         *
         * @return predicate that is true when the world is thundering
         */
        private static BiPredicate<Level, LocalTime> thundering() {
                return (level, now) -> level.isThundering();
        }

        /**
         * Creates a visibility rule active during rain without thunderstorms.
         *
         * @return predicate that is true when raining and not thundering
         */
        private static BiPredicate<Level, LocalTime> rainWithoutThunder() {
                return (level, now) -> level.isRaining() && !level.isThundering();
        }

        /**
         * Creates the special night rule for full-moon or new-moon windows.
         *
         * @return predicate that is true at night when moon cycle matches
         */
        private static BiPredicate<Level, LocalTime> fullOrNewMoonNight() {
                return (level, now) -> matchesSlot(now, AvailabilitySlot.NIGHT)
                                && (DaylightCycle.isFullMoon(now) || DaylightCycle.isNewMoon(now));
        }

        /**
         * Creates the opening rule shared by Cocktail Bar and Monkey Shop.
         *
         * @return predicate that is true during their opening time window
         */
        private static BiPredicate<Level, LocalTime> cocktailAndMonkeyShopOpen() {
                return (level, now) -> DaylightCycle.isCocktailAndMonkeyShopOpen(now);
        }

        /**
         * Creates the opening rule for the Italian restaurant.
         *
         * @return predicate that is true during the restaurant opening window
         */
        private static BiPredicate<Level, LocalTime> italianRestaurantOpen() {
                return (level, now) -> DaylightCycle.isItalianRestaurantOpen(now);
        }

        /**
         * Creates the Herb Shop opening rule restricted to full-moon periods.
         *
         * @return predicate that is true when full moon and opening window both match
         */
        private static BiPredicate<Level, LocalTime> herbShopOpenOnFullMoon() {
                return (level, now) -> DaylightCycle.isFullMoon(now) && DaylightCycle.isHerbShopOpen(now);
        }

        /**
         * Evaluates whether the current time belongs to an abstract availability slot.
         *
         * @param now current time to evaluate
         * @param slot slot to test
         * @return {@code true} if time belongs to the slot
         */
        private static boolean matchesSlot(LocalTime now, AvailabilitySlot slot) {
                return switch (slot) {
                        case ALL_DAY -> true;
                        case MORNING -> DaylightCycle.isMorning(now);
                        case AFTERNOON -> DaylightCycle.isAfternoon(now);
                        case EVENING -> DaylightCycle.isEvening(now);
                        case NIGHT -> DaylightCycle.isNight(now);
                        default -> false;
                };
        }

        /**
         * Indicates whether weather conditions are considered bad for clear-only entries.
         *
         * @param level current world state
         * @return {@code true} when raining or thundering
         */
        private static boolean isBadWeather(Level level) {
                return level.isRaining() || level.isThundering();
        }
}
