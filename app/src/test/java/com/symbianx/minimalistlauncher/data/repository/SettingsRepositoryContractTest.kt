package com.symbianx.minimalistlauncher.data.repository

import android.content.Intent
import com.symbianx.minimalistlauncher.data.local.SettingsDataSource
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.model.BatteryThresholdMode
import com.symbianx.minimalistlauncher.domain.model.LauncherSettings
import com.symbianx.minimalistlauncher.domain.model.QuickActionConfig
import com.symbianx.minimalistlauncher.domain.repository.AppRepository
import com.symbianx.minimalistlauncher.domain.repository.SettingsRepository
import com.symbianx.minimalistlauncher.util.SettingsLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Contract tests for SettingsRepository.getSettings() default emission.
 * Verifies that repository always emits settings, starting with defaults.
 */
class SettingsRepositoryContractTest {
    private lateinit var repository: SettingsRepository
    private lateinit var mockDataSource: FakeSettingsDataSource
    private lateinit var mockAppRepository: FakeAppRepository
    private lateinit var mockLogger: SettingsLogger

    @Before
    fun setup() {
        mockDataSource = FakeSettingsDataSource()
        mockAppRepository = FakeAppRepository()
        mockLogger = SettingsLogger()
        repository = SettingsRepositoryImpl(mockDataSource, mockAppRepository, mockLogger)
    }

    @Test
    fun `getSettings emits default settings when data source is empty`() = runTest {
        // Given data source returns defaults
        mockDataSource.settings = LauncherSettings()

        // When getting settings
        val settings = repository.getSettings().first()

        // Then defaults should be emitted
        assertEquals(true, settings.autoLaunchEnabled)
        assertEquals("com.google.android.dialer", settings.leftQuickAction.packageName)
        assertEquals("com.google.android.GoogleCamera", settings.rightQuickAction.packageName)
        assertEquals(BatteryThresholdMode.BELOW_50, settings.batteryIndicatorMode)
    }

    @Test
    fun `getSettings emits immediately without suspension`() = runTest {
        // Given data source is ready
        mockDataSource.settings = LauncherSettings()

        // When getting settings
        val settings = repository.getSettings().first()

        // Then settings should be available immediately
        assertEquals(LauncherSettings(), settings)
    }

    @Test
    fun `getSettings validates installed apps and corrects invalid packages`() = runTest {
        // Given settings with uninstalled app
        mockDataSource.settings = LauncherSettings(
            leftQuickAction = QuickActionConfig("com.uninstalled.app", "Uninstalled", false)
        )
        mockAppRepository.installedApps = listOf(
            App("com.google.android.dialer", "Phone", Intent())
        )

        // When getting settings
        val settings = repository.getSettings().first()

        // Then invalid package should be reverted to default
        assertEquals("com.google.android.dialer", settings.leftQuickAction.packageName)
    }

    @Test
    fun `updateSettings returns success for valid settings`() = runTest {
        // Given valid settings
        val newSettings = LauncherSettings(autoLaunchEnabled = false)
        mockAppRepository.installedApps = listOf(
            App("com.google.android.dialer", "Phone", Intent()),
            App("com.google.android.GoogleCamera", "Camera", Intent())
        )

        // When updating settings
        val result = repository.updateSettings(newSettings)

        // Then result should be success
        assertTrue(result.isSuccess)
        assertEquals(newSettings.autoLaunchEnabled, mockDataSource.settings.autoLaunchEnabled)
    }

    @Test
    fun `resetToDefaults clears data source and returns success`() = runTest {
        // Given settings have been customized
        mockDataSource.settings = LauncherSettings(autoLaunchEnabled = false)

        // When resetting to defaults
        val result = repository.resetToDefaults()

        // Then result should be success and data should be cleared
        assertTrue(result.isSuccess)
        assertTrue(mockDataSource.cleared)
    }

    // Test doubles
    private class FakeSettingsDataSource : SettingsDataSource {
        var settings = LauncherSettings()
        var cleared = false

        override fun readSettings(): Flow<LauncherSettings> = flowOf(settings)

        override suspend fun writeSettings(settings: LauncherSettings) {
            this.settings = settings
        }

        override suspend fun clearSettings() {
            cleared = true
            settings = LauncherSettings()
        }
    }

    private class FakeAppRepository : AppRepository {
        var installedApps = listOf<App>()

        override fun getApps(): Flow<List<App>> = flowOf(installedApps)
    }
}
