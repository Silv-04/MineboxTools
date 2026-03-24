package fr.silv.constants;

import java.time.LocalTime;

/**
 * Provides time-window helpers for full-moon and new-moon checks.
 */
public class DaylightCycle {
    // Fullmoon
    private static final LocalTime FullMoonStart1 = LocalTime.of(0, 0);
    private static final LocalTime FullMoonEnd1 = LocalTime.of(1, 0);
    private static final LocalTime FullMoonStart2 = LocalTime.of(8, 0);
    private static final LocalTime FullMoonEnd2 = LocalTime.of(9, 0);
    private static final LocalTime FullMoonStart3 = LocalTime.of(16, 0);
    private static final LocalTime FullMoonEnd3 = LocalTime.of(17, 0);

    private static final LocalTime NewMoonStart1 = LocalTime.of(4, 0);
    private static final LocalTime NewMoonEnd1 = LocalTime.of(5, 0);
    private static final LocalTime NewMoonStart2 = LocalTime.of(12, 0);
    private static final LocalTime NewMoonEnd2 = LocalTime.of(13, 0);
    private static final LocalTime NewMoonStart3 = LocalTime.of(20, 0);
    private static final LocalTime NewMoonEnd3 = LocalTime.of(21, 0);

    /**
     * Checks whether full moon.
     * @param now value for now
     * @return true if the condition is met; otherwise false
     */
    public static boolean isFullMoon(LocalTime now) {
        return (now.isAfter(FullMoonStart1) && now.isBefore(FullMoonEnd1)) ||
                (now.isAfter(FullMoonStart2) && now.isBefore(FullMoonEnd2)) ||
                (now.isAfter(FullMoonStart3) && now.isBefore(FullMoonEnd3));
    }

    /**
     * Checks whether new moon.
     * @param now value for now
     * @return true if the condition is met; otherwise false
     */
    public static boolean isNewMoon(LocalTime now) {
        return (now.isAfter(NewMoonStart1) && now.isBefore(NewMoonEnd1)) ||
                (now.isAfter(NewMoonStart2) && now.isBefore(NewMoonEnd2)) ||
                (now.isAfter(NewMoonStart3) && now.isBefore(NewMoonEnd3));
    }

    /**
     * Checks whether new day.
     * @param now value for now
     * @return true if the condition is met; otherwise false
     */
    public static boolean isNewDay(LocalTime now) {
        int minute = now.getMinute();
        return minute <= 15;
    }

    // Night
    /**
     * Checks whether night.
     * @param now value for now
     * @return true if the condition is met; otherwise false
     */
    public static boolean isNight(LocalTime now) {
        int minute = now.getMinute();
        return minute < 15;
    }

    // Morning
    /**
     * Checks whether morning.
     * @param now value for now
     * @return true if the condition is met; otherwise false
     */
    public static boolean isMorning(LocalTime now) {
        int minute = now.getMinute();
        return minute >= 15 && minute < 30;
    }

    // Afternoon
    /**
     * Checks whether afternoon.
     * @param now value for now
     * @return true if the condition is met; otherwise false
     */
    public static boolean isAfternoon(LocalTime now) {
        int minute = now.getMinute();
        return minute >= 30 && minute < 45;
    }

    // Evening
    /**
     * Checks whether evening.
     * @param now value for now
     * @return true if the condition is met; otherwise false
     */
    public static boolean isEvening(LocalTime now) {
        int minute = now.getMinute();
        return minute >= 45;
    }

    // Herb shop timer
    /**
     * Checks whether herb shop open.
     * @param now value for now
     * @return true if the condition is met; otherwise false
     */
    public static boolean isHerbShopOpen(LocalTime now) {
        int minute = now.getMinute();
        int second = now.getSecond();
        int total = minute * 60 + second;
        return (total < 750) || (total >= 2925);
    }

    // Cocktail shop and monkey shop timer
    /**
     * Checks whether cocktail and monkey shop open.
     * @param now value for now
     * @return true if the condition is met; otherwise false
     */
    public static boolean isCocktailAndMonkeyShopOpen(LocalTime now) {
        int minute = now.getMinute();
        int second = now.getSecond();
        int total = minute * 60 + second;
        return (total >= 2700 && total < 2925);
    }

    // Italian restaurant timer
    /**
     * Checks whether italian restaurant open.
     * @param now value for now
     * @return true if the condition is met; otherwise false
     */
    public static boolean isItalianRestaurantOpen(LocalTime now) {
        int minute = now.getMinute();
        int second = now.getSecond();
        int total = minute * 60 + second;
        return (total >= 2925 || total < 750);
    }
}
