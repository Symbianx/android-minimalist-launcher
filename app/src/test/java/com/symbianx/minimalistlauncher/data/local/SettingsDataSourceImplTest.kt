package com.symbianx.minimalistlauncher.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.symbianx.minimalistlauncher.domain.model.BatteryThresholdMode
import com.symbianx.minimalistlauncher.domain.model.LauncherSettings
import com.symbianx.minimalistlauncher.domain.model.QuickActionConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Unit tests for SettingsDataSourceImpl write/read round-trip functionality.
 * Tests that settings can be written to DataStore and read back correctly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsDataSourceImplTest {
    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var testContext: Context
    private lateinit var testScope: TestScope
    private lateinit var dataSource: SettingsDataSourceImpl

    @Before
    fun setup() {
        testContext = RuntimeEnvironment.getApplication()
        val testDispatcher = StandardTestDispatcher()
        testScope = TestScope(testDispatcher + Job())

        testDataStore =
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(testDispatcher + Job()),
                produceFile = { testContext.preferencesDataStoreFile("test_settings") },
            )

        dataSource = SettingsDataSourceImpl(testContext, testDataStore)
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun `readSettings returns defaults when no data exists`() =
        testScope.runTest {
            // When reading settings for the first time
            val settings = dataSource.readSettings().first()

            // Then defaults should be returned
            assertEquals(true, settings.autoLaunchEnabled)
            assertEquals("com.google.android.dialer", settings.leftQuickAction.packageName)
            assertEquals("com.google.android.GoogleCamera", settings.rightQuickAction.packageName)
            assertEquals(BatteryThresholdMode.BELOW_50, settings.batteryIndicatorMode)
        }

    @Test
    fun `writeSettings and readSettings round-trip preserves data`() =
        testScope.runTest {
            // Given custom settings
            val customSettings =
                LauncherSettings(
                    autoLaunchEnabled = false,
                    leftQuickAction =
                        QuickActionConfig(
                            packageName = "com.custom.app1",
                            label = "Custom App 1",
                            isDefault = false,
                        ),
                    rightQuickAction =
                        QuickActionConfig(
                            packageName = "com.custom.app2",
                            label = "Custom App 2",
                            isDefault = false,
                        ),
                    batteryIndicatorMode = BatteryThresholdMode.ALWAYS,
                    lastModified = 12345L,
                )

            // When writing and reading back
            dataSource.writeSettings(customSettings)
            val readSettings = dataSource.readSettings().first()

            // Then all fields should match
            assertEquals(customSettings.autoLaunchEnabled, readSettings.autoLaunchEnabled)
            assertEquals(customSettings.leftQuickAction.packageName, readSettings.leftQuickAction.packageName)
            assertEquals(customSettings.leftQuickAction.label, readSettings.leftQuickAction.label)
            assertEquals(customSettings.rightQuickAction.packageName, readSettings.rightQuickAction.packageName)
            assertEquals(customSettings.rightQuickAction.label, readSettings.rightQuickAction.label)
            assertEquals(customSettings.batteryIndicatorMode, readSettings.batteryIndicatorMode)
        }

    @Test
    fun `clearSettings removes all data and returns defaults`() =
        testScope.runTest {
            // Given settings have been written
            val customSettings =
                LauncherSettings(
                    autoLaunchEnabled = false,
                    leftQuickAction = QuickActionConfig("com.test", "Test", false),
                    rightQuickAction = QuickActionConfig("com.test2", "Test2", false),
                    batteryIndicatorMode = BatteryThresholdMode.NEVER,
                    lastModified = 99999L,
                )
            dataSource.writeSettings(customSettings)

            // When clearing settings
            dataSource.clearSettings()
            val settings = dataSource.readSettings().first()

            // Then defaults should be returned
            assertEquals(true, settings.autoLaunchEnabled)
            assertEquals("com.google.android.dialer", settings.leftQuickAction.packageName)
            assertEquals("com.google.android.GoogleCamera", settings.rightQuickAction.packageName)
            assertEquals(BatteryThresholdMode.BELOW_50, settings.batteryIndicatorMode)
        }

    @Test
    fun `writeSettings updates lastModified timestamp`() =
        testScope.runTest {
            // Given initial settings
            val initialSettings = LauncherSettings()
            dataSource.writeSettings(initialSettings)
            val firstRead = dataSource.readSettings().first()

            // When writing again with different timestamp
            Thread.sleep(10) // Ensure time difference
            val updatedSettings = initialSettings.copy(autoLaunchEnabled = false)
            dataSource.writeSettings(updatedSettings)
            val secondRead = dataSource.readSettings().first()

            // Then timestamp should be updated (implementation should set current time)
            // Note: Implementation will override lastModified with current time
            assert(secondRead.lastModified >= firstRead.lastModified)
        }
}
