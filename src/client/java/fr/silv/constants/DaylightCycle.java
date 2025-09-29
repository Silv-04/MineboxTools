package fr.silv.constants;

import java.time.LocalTime;

public class DaylightCycle {
    // Fullmoon
    private static final LocalTime FullMoonStart1 = LocalTime.of(2, 0);
    private static final LocalTime FullMoonEnd1 = LocalTime.of(3, 0);
    private static final LocalTime FullMoonStart2 = LocalTime.of(10, 0);
    private static final LocalTime FullMoonEnd2 = LocalTime.of(11, 0);
    private static final LocalTime FullMoonStart3 = LocalTime.of(18, 0);
    private static final LocalTime FullMoonEnd3 = LocalTime.of(19, 0);

    public static boolean isFullMoon(LocalTime now) {
        return (now.isAfter(FullMoonStart1) && now.isBefore(FullMoonEnd1)) ||
               (now.isAfter(FullMoonStart2) && now.isBefore(FullMoonEnd2)) ||
               (now.isAfter(FullMoonStart3) && now.isBefore(FullMoonEnd3));
    }

    // Night
    public static boolean isNight(LocalTime now) {
        int minute = now.getMinute();
        return minute < 15;
    }

    // Morning
    public static boolean isMorning(LocalTime now) {
        int minute = now.getMinute();
        return minute >= 15 && minute < 30;
    }

    // Afternoon
    public static boolean isAfternoon(LocalTime now) {
        int minute = now.getMinute();
        return minute >= 30 && minute < 45;
    }

    // Evening
    public static boolean isEvening(LocalTime now) {
        int minute = now.getMinute();
        return minute >= 45;
    }

    // Herb shop timer
    public static boolean isHerbShopOpen(LocalTime now) {
        int minute = now.getMinute();
        int second = now.getSecond();
        int total = minute * 60 + second;
        return (total < 600) || (total >= 2925);
    }

    // Cocktail shop and monkey shop timer
    public static boolean isCocktailAndMonkeyShopOpen(LocalTime now) {
        int minute = now.getMinute();
        int second = now.getSecond();
        int total = minute * 60 + second;
        return (total >= 2700 && total < 2925);
    }

    // Italian restaurant timer
    public static boolean isItalianRestaurantOpen(LocalTime now) {
        int minute = now.getMinute();
        int second = now.getSecond();
        int total = minute * 60 + second;
        return (total >= 2925 || total < 750);
    }
}
