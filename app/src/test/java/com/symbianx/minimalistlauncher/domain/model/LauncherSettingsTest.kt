package com.symbianx.minimalistlauncher.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for LauncherSettings data model.
 *
 * Tests default values, immutability, and the defaults() factory method.
 */
class LauncherSettingsTest {
    @Test
    fun `defaults() returns settings with expected default values`() {
        // When
        val settings = LauncherSettings.defaults()

        // Then
        assertEquals(true, settings.autoLaunchEnabled)
        assertEquals("com.google.android.dialer", settings.leftQuickAction.packageName)
        assertEquals("Phone", settings.leftQuickAction.label)
        assertTrue(settings.leftQuickAction.isDefault)
        assertEquals("com.google.android.GoogleCamera", settings.rightQuickAction.packageName)
        assertEquals("Camera", settings.rightQuickAction.label)
        assertTrue(settings.rightQuickAction.isDefault)
        assertEquals(BatteryThresholdMode.BELOW_50, settings.batteryIndicatorMode)
        assertTrue(settings.lastModified > 0)
    }

    @Test
    fun `constructor with no args returns same as defaults()`() {
        // When
        val settings1 = LauncherSettings()
        val settings2 = LauncherSettings.defaults()

        // Then - should have identical field values (except timestamp)
        assertEquals(settings1.autoLaunchEnabled, settings2.autoLaunchEnabled)
        assertEquals(settings1.leftQuickAction.packageName, settings2.leftQuickAction.packageName)
        assertEquals(settings1.rightQuickAction.packageName, settings2.rightQuickAction.packageName)
        assertEquals(settings1.batteryIndicatorMode, settings2.batteryIndicatorMode)
    }

    @Test
    fun `copy() creates new instance with modified field`() {
        // Given
        val original = LauncherSettings.defaults()

        // When
        val modified = original.copy(autoLaunchEnabled = false)

        // Then
        assertEquals(false, modified.autoLaunchEnabled)
        assertEquals(original.leftQuickAction, modified.leftQuickAction)
        assertEquals(original.rightQuickAction, modified.rightQuickAction)
        assertEquals(original.batteryIndicatorMode, modified.batteryIndicatorMode)
    }

    @Test
    fun `lastModified is updated when set explicitly`() {
        // Given
        val timestamp = 1234567890L

        // When
        val settings = LauncherSettings(lastModified = timestamp)

        // Then
        assertEquals(timestamp, settings.lastModified)
    }

    @Test
    fun `data class equality works correctly`() {
        // Given
        val leftConfig = QuickActionConfig("com.example.app1", "App1", false)
        val rightConfig = QuickActionConfig("com.example.app2", "App2", false)
        val settings1 =
            LauncherSettings(
                autoLaunchEnabled = false,
                leftQuickAction = leftConfig,
                rightQuickAction = rightConfig,
                batteryIndicatorMode = BatteryThresholdMode.ALWAYS,
                lastModified = 1000L,
            )
        val settings2 =
            LauncherSettings(
                autoLaunchEnabled = false,
                leftQuickAction = leftConfig,
                rightQuickAction = rightConfig,
                batteryIndicatorMode = BatteryThresholdMode.ALWAYS,
                lastModified = 1000L,
            )

        // When/Then
        assertEquals(settings1, settings2)
        assertEquals(settings1.hashCode(), settings2.hashCode())
    }
}
