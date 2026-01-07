package com.symbianx.minimalistlauncher.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for BatteryThresholdMode enum.
 *
 * Tests the shouldShow() logic for all modes and various battery percentages.
 */
class BatteryThresholdModeTest {
    @Test
    fun `ALWAYS mode always returns true`() {
        // Given
        val mode = BatteryThresholdMode.ALWAYS

        // When/Then - should show for all battery levels
        assertTrue(mode.shouldShow(0))
        assertTrue(mode.shouldShow(10))
        assertTrue(mode.shouldShow(20))
        assertTrue(mode.shouldShow(49))
        assertTrue(mode.shouldShow(50))
        assertTrue(mode.shouldShow(75))
        assertTrue(mode.shouldShow(100))
    }

    @Test
    fun `NEVER mode always returns false`() {
        // Given
        val mode = BatteryThresholdMode.NEVER

        // When/Then - should never show for any battery level
        assertFalse(mode.shouldShow(0))
        assertFalse(mode.shouldShow(10))
        assertFalse(mode.shouldShow(20))
        assertFalse(mode.shouldShow(49))
        assertFalse(mode.shouldShow(50))
        assertFalse(mode.shouldShow(75))
        assertFalse(mode.shouldShow(100))
    }

    @Test
    fun `BELOW_50 mode shows when battery is less than 50 percent`() {
        // Given
        val mode = BatteryThresholdMode.BELOW_50

        // When/Then - should show below 50%
        assertTrue(mode.shouldShow(0))
        assertTrue(mode.shouldShow(10))
        assertTrue(mode.shouldShow(20))
        assertTrue(mode.shouldShow(49))

        // When/Then - should NOT show at or above 50%
        assertFalse(mode.shouldShow(50))
        assertFalse(mode.shouldShow(51))
        assertFalse(mode.shouldShow(75))
        assertFalse(mode.shouldShow(100))
    }

    @Test
    fun `BELOW_20 mode shows when battery is less than 20 percent`() {
        // Given
        val mode = BatteryThresholdMode.BELOW_20

        // When/Then - should show below 20%
        assertTrue(mode.shouldShow(0))
        assertTrue(mode.shouldShow(10))
        assertTrue(mode.shouldShow(19))

        // When/Then - should NOT show at or above 20%
        assertFalse(mode.shouldShow(20))
        assertFalse(mode.shouldShow(21))
        assertFalse(mode.shouldShow(49))
        assertFalse(mode.shouldShow(50))
        assertFalse(mode.shouldShow(75))
        assertFalse(mode.shouldShow(100))
    }

    @Test
    fun `all enum values are covered`() {
        // Given
        val allModes = BatteryThresholdMode.values()

        // Then - verify we have exactly 4 modes
        assertTrue(allModes.contains(BatteryThresholdMode.ALWAYS))
        assertTrue(allModes.contains(BatteryThresholdMode.BELOW_50))
        assertTrue(allModes.contains(BatteryThresholdMode.BELOW_20))
        assertTrue(allModes.contains(BatteryThresholdMode.NEVER))
    }

    @Test
    fun `boundary condition at exactly 50 percent for BELOW_50`() {
        // Given
        val mode = BatteryThresholdMode.BELOW_50

        // When/Then - 50% exactly should NOT show (< not <=)
        assertFalse(mode.shouldShow(50))
    }

    @Test
    fun `boundary condition at exactly 20 percent for BELOW_20`() {
        // Given
        val mode = BatteryThresholdMode.BELOW_20

        // When/Then - 20% exactly should NOT show (< not <=)
        assertFalse(mode.shouldShow(20))
    }
}
