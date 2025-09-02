package com.example.daydisplay;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TimeUtilsTest {
    @Test
    void ticksToDay_zero() {
        assertEquals(1, TimeUtils.ticksToDay(0));
    }

    @Test
    void ticksToDay_oneDay() {
        assertEquals(1, TimeUtils.ticksToDay(23999));
        assertEquals(2, TimeUtils.ticksToDay(24000));
    }

    @Test
    void formatDay() {
        assertEquals("Day 1", TimeUtils.formatDay(1));
        assertEquals("Day 10", TimeUtils.formatDay(10));
    }

    @Test
    void ticksToDay_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> TimeUtils.ticksToDay(-1));
    }

    @Test
    void formatDay_invalid_throws() {
        assertThrows(IllegalArgumentException.class, () -> TimeUtils.formatDay(0));
    }
}
