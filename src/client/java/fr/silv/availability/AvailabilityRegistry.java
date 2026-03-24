package fr.silv.availability;

import fr.silv.ModConfig;
import fr.silv.constants.DaylightCycle;
import fr.silv.constants.Icons;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

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
            insect("mineboxtools.insect.atlas_moth", Icons.AtlasMothButterflyICON, ModConfig.InsectFlag.ATLAS_MOTH,
                    clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                    order(AvailabilitySlot.EVENING, 10, AvailabilitySlot.NIGHT, 10)),
            insect("mineboxtools.insect.birdwing", Icons.BirdwingICON, ModConfig.InsectFlag.BIRDWING,
                    clearDuring(AvailabilitySlot.AFTERNOON),
                    order(AvailabilitySlot.AFTERNOON, 11)),
            insect("mineboxtools.insect.blue_butterfly", Icons.BlueButterflyICON, ModConfig.InsectFlag.BLUE_BUTTERFLY,
                    clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                    order(AvailabilitySlot.EVENING, 6, AvailabilitySlot.NIGHT, 6)),
            insect("mineboxtools.insect.blue_dragonfly", Icons.BlueDragonflyICON, ModConfig.InsectFlag.BLUE_DRAGONFLY,
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
            insect("mineboxtools.insect.cyclommatus", Icons.CyclommatusICON, ModConfig.InsectFlag.CYCLOMMATUS,
                    clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                    order(AvailabilitySlot.EVENING, 1, AvailabilitySlot.NIGHT, 1)),
            insect("mineboxtools.insect.dung_beetle", Icons.DungBeetleICON, ModConfig.InsectFlag.DUNG_BEETLE,
                    clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                    order(AvailabilitySlot.EVENING, 5, AvailabilitySlot.NIGHT, 5)),
            insect("mineboxtools.insect.firefly", Icons.FireflyICON, ModConfig.InsectFlag.FIREFLY,
                    clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                    order(AvailabilitySlot.EVENING, 7, AvailabilitySlot.NIGHT, 7)),
            insect("mineboxtools.insect.green_butterfly", Icons.GreenButterflyICON, ModConfig.InsectFlag.GREEN_BUTTERFLY,
                    clearDuring(AvailabilitySlot.MORNING),
                    order(AvailabilitySlot.MORNING, 2)),
            insect("mineboxtools.insect.green_dragonfly", Icons.GreenDragonflyICON, ModConfig.InsectFlag.GREEN_DRAGONFLY,
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
            insect("mineboxtools.insect.night_butterfly", Icons.NightButterflyICON, ModConfig.InsectFlag.NIGHT_BUTTERFLY,
                    clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                    order(AvailabilitySlot.EVENING, 8, AvailabilitySlot.NIGHT, 8)),
            insect("mineboxtools.insect.purple_emperor", Icons.PurpleEmperorICON, ModConfig.InsectFlag.PURPLE_EMPEROR,
                    fullOrNewMoonNight(),
                    order(AvailabilitySlot.SPECIAL, 1)),
            insect("mineboxtools.insect.red_dragonfly", Icons.RedDragonflyICON, ModConfig.InsectFlag.RED_DRAGONFLY,
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
            insect("mineboxtools.insect.stick_insect", Icons.StickInsectICON, ModConfig.InsectFlag.STICK_INSECT,
                    clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                    order(AvailabilitySlot.EVENING, 2, AvailabilitySlot.NIGHT, 2)),
            insect("mineboxtools.insect.sunset_moth", Icons.SunsetMothICON, ModConfig.InsectFlag.SUNSET_MOTH,
                    clearDuring(AvailabilitySlot.EVENING),
                    order(AvailabilitySlot.EVENING, 11)),
            insect("mineboxtools.insect.tarantula", Icons.TarantulaICON, ModConfig.InsectFlag.TARANTULA,
                    clearDuring(AvailabilitySlot.EVENING, AvailabilitySlot.NIGHT),
                    order(AvailabilitySlot.EVENING, 9, AvailabilitySlot.NIGHT, 9)),
            insect("mineboxtools.insect.tiger_butterfly", Icons.TigerButterflyICON, ModConfig.InsectFlag.TIGER_BUTTERFLY,
                    clearDuring(AvailabilitySlot.MORNING, AvailabilitySlot.AFTERNOON),
                    order(AvailabilitySlot.MORNING, 8, AvailabilitySlot.AFTERNOON, 12)),
            insect("mineboxtools.insect.wasp", Icons.WaspICON, ModConfig.InsectFlag.WASP,
                    clearDuring(AvailabilitySlot.AFTERNOON),
                    order(AvailabilitySlot.AFTERNOON, 10)),
            insect("mineboxtools.insect.white_butterfly", Icons.WhiteButterflyICON, ModConfig.InsectFlag.WHITE_BUTTERFLY,
                    clearDuring(AvailabilitySlot.MORNING, AvailabilitySlot.AFTERNOON),
                    order(AvailabilitySlot.MORNING, 1, AvailabilitySlot.AFTERNOON, 2)),
            insect("mineboxtools.insect.yellow_butterfly", Icons.YellowButterflyICON, ModConfig.InsectFlag.YELLOW_BUTTERFLY,
                    clearDuring(AvailabilitySlot.MORNING, AvailabilitySlot.AFTERNOON),
                    order(AvailabilitySlot.MORNING, 3, AvailabilitySlot.AFTERNOON, 3)),
            insect("mineboxtools.insect.yellow_dragonfly", Icons.YellowDragonflyICON, ModConfig.InsectFlag.YELLOW_DRAGONFLY,
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
            worldShop("mineboxtools.shop.restaurant", Icons.ItalianRestaurantICON, ModConfig.ShopFlag.ITALIAN_RESTAURANT,
                    italianRestaurantOpen(),
                    order(AvailabilitySlot.SHOP, 4)),
            worldShop("mineboxtools.shop.herb", Icons.HerbShopICON, ModConfig.ShopFlag.HERB,
                    herbShopOpenOnFullMoon(),
                    order(AvailabilitySlot.SHOP, 5))
    );
    private static final Map<AvailabilitySection, List<AvailabilityEntry>> ENTRIES_BY_SECTION = buildEntriesBySection();
    private static final Map<AvailabilitySlot, List<AvailabilityEntry>> ENTRIES_BY_SLOT = buildEntriesBySlot();

    private AvailabilityRegistry() {
    }

    /**
     * Executes the entries for section operation.
     * @param section value for section
     * @return the computed entries for section value
     */
    public static List<AvailabilityEntry> entriesForSection(AvailabilitySection section) {
        return ENTRIES_BY_SECTION.getOrDefault(section, List.of());
    }

    /**
     * Executes the entries for slot operation.
     * @param slot value for slot
     * @param world value for world
     * @param now value for now
     * @return the computed entries for slot value
     */
    public static List<AvailabilityEntry> entriesForSlot(AvailabilitySlot slot, World world, LocalTime now) {
        return ENTRIES_BY_SLOT.getOrDefault(slot, List.of()).stream()
                .filter(entry -> entry.isVisible(world, now))
                .toList();
    }

    private static AvailabilityEntry insect(String langKey, Identifier icon, ModConfig.InsectFlag flag,
                                            BiPredicate<World, LocalTime> visibilityRule,
                                            Map<AvailabilitySlot, Integer> displayOrder) {
        return new AvailabilityEntry(
                langKey,
                icon,
                AvailabilitySection.INSECTS,
                () -> ModConfig.isEnabled(flag),
                value -> ModConfig.setEnabled(flag, value),
                visibilityRule,
                displayOrder
        );
    }

    private static AvailabilityEntry worldFeature(String langKey, Identifier icon, ModConfig.FeatureFlag flag,
                                                  BiPredicate<World, LocalTime> visibilityRule,
                                                  Map<AvailabilitySlot, Integer> displayOrder) {
        return new AvailabilityEntry(
                langKey,
                icon,
                AvailabilitySection.WORLD,
                () -> ModConfig.isEnabled(flag),
                value -> ModConfig.setEnabled(flag, value),
                visibilityRule,
                displayOrder
        );
    }

    private static AvailabilityEntry worldShop(String langKey, Identifier icon, ModConfig.ShopFlag flag,
                                               BiPredicate<World, LocalTime> visibilityRule,
                                               Map<AvailabilitySlot, Integer> displayOrder) {
        return new AvailabilityEntry(
                langKey,
                icon,
                AvailabilitySection.WORLD,
                () -> ModConfig.isEnabled(flag),
                value -> ModConfig.setEnabled(flag, value),
                visibilityRule,
                displayOrder
        );
    }

    private static Map<AvailabilitySlot, Integer> order(Object... values) {
        Map<AvailabilitySlot, Integer> order = new EnumMap<>(AvailabilitySlot.class);
        for (int i = 0; i < values.length; i += 2) {
            order.put((AvailabilitySlot) values[i], (Integer) values[i + 1]);
        }
        return Map.copyOf(order);
    }

    private static Map<AvailabilitySection, List<AvailabilityEntry>> buildEntriesBySection() {
        Map<AvailabilitySection, List<AvailabilityEntry>> entriesBySection = new EnumMap<>(AvailabilitySection.class);
        for (AvailabilitySection section : AvailabilitySection.values()) {
            entriesBySection.put(section, ALL_ENTRIES.stream()
                    .filter(entry -> entry.isInSection(section))
                    .toList());
        }
        return Map.copyOf(entriesBySection);
    }

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

    private static BiPredicate<World, LocalTime> during(AvailabilitySlot... slots) {
        return (world, now) -> {
            for (AvailabilitySlot slot : slots) {
                if (matchesSlot(now, slot)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static BiPredicate<World, LocalTime> clearDuring(AvailabilitySlot... slots) {
        return clearWeather(during(slots));
    }

    private static BiPredicate<World, LocalTime> clearWeather(BiPredicate<World, LocalTime> rule) {
        return (world, now) -> !isBadWeather(world) && rule.test(world, now);
    }

    private static BiPredicate<World, LocalTime> notThunderingAnd(BiPredicate<World, LocalTime> rule) {
        return (world, now) -> !world.isThundering() && rule.test(world, now);
    }

    private static BiPredicate<World, LocalTime> badWeather() {
        return (world, now) -> isBadWeather(world);
    }

    private static BiPredicate<World, LocalTime> thundering() {
        return (world, now) -> world.isThundering();
    }

    private static BiPredicate<World, LocalTime> rainWithoutThunder() {
        return (world, now) -> world.isRaining() && !world.isThundering();
    }

    private static BiPredicate<World, LocalTime> fullOrNewMoonNight() {
        return (world, now) -> matchesSlot(now, AvailabilitySlot.NIGHT)
                && (DaylightCycle.isFullMoon(now) || DaylightCycle.isNewMoon(now));
    }

    private static BiPredicate<World, LocalTime> cocktailAndMonkeyShopOpen() {
        return (world, now) -> DaylightCycle.isCocktailAndMonkeyShopOpen(now);
    }

    private static BiPredicate<World, LocalTime> italianRestaurantOpen() {
        return (world, now) -> DaylightCycle.isItalianRestaurantOpen(now);
    }

    private static BiPredicate<World, LocalTime> herbShopOpenOnFullMoon() {
        return (world, now) -> DaylightCycle.isFullMoon(now) && DaylightCycle.isHerbShopOpen(now);
    }

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

    private static boolean isBadWeather(World world) {
        return world.isRaining() || world.isThundering();
    }
}
