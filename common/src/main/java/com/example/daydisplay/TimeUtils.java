package com.example.daydisplay;

public final class TimeUtils {
    private TimeUtils() {}

    // Minecraft: 24000 ticks == 1 day
    public static long ticksToDay(long ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks must be >= 0");
        return ticks / 24000L + 1; // Day 1 starts at tick 0
    }

    public static String formatDay(long day) {
        if (day < 1) throw new IllegalArgumentException("day must be >= 1");
        return "Day " + day;
    }
}
