package com.symbianx.minimalistlauncher.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimeFormatterTest {
    @Test
    fun formatRelativeTime_returnsNull_whenTimestampIsZero() {
        val result = TimeFormatter.formatRelativeTime(0L)
        assertNull(result)
    }

    @Test
    fun formatRelativeTime_returnsJustNow_whenLessThanOneMinute() {
        val now = System.currentTimeMillis()
        val thirtySecondsAgo = now - (30 * 1000)
        val result = TimeFormatter.formatRelativeTime(thirtySecondsAgo)
        assertEquals("just now", result)
    }

    @Test
    fun formatRelativeTime_returnsMinutesAgo_whenLessThanOneHour() {
        val now = System.currentTimeMillis()
        val fifteenMinutesAgo = now - (15 * 60 * 1000)
        val result = TimeFormatter.formatRelativeTime(fifteenMinutesAgo)
        assertEquals("15m ago", result)
    }

    @Test
    fun formatRelativeTime_returnsHoursAgo_whenLessThan24Hours() {
        val now = System.currentTimeMillis()
        val threeHoursAgo = now - (3 * 60 * 60 * 1000)
        val result = TimeFormatter.formatRelativeTime(threeHoursAgo)
        assertEquals("3h ago", result)
    }

    @Test
    fun formatRelativeTime_returnsNull_when24HoursOrMore() {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - (24 * 60 * 60 * 1000)
        val result = TimeFormatter.formatRelativeTime(oneDayAgo)
        assertNull(result)
    }

    @Test
    fun formatRelativeTime_handlesEdgeCase_exactlyOneMinute() {
        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - (60 * 1000)
        val result = TimeFormatter.formatRelativeTime(oneMinuteAgo)
        assertEquals("1m ago", result)
    }

    @Test
    fun formatRelativeTime_handlesEdgeCase_exactlyOneHour() {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000)
        val result = TimeFormatter.formatRelativeTime(oneHourAgo)
        assertEquals("1h ago", result)
    }
}
