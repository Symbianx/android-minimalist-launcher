package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.BatteryThresholdMode
import com.symbianx.minimalistlauncher.domain.model.LauncherSettings
import com.symbianx.minimalistlauncher.domain.model.QuickActionConfig
import com.symbianx.minimalistlauncher.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LoadSettingsUseCase.
 * Verifies that the use case correctly delegates to the repository.
 */
class LoadSettingsUseCaseTest {
    private lateinit var useCase: LoadSettingsUseCase
    private lateinit var mockRepository: FakeSettingsRepository

    @Before
    fun setup() {
        mockRepository = FakeSettingsRepository()
        useCase = LoadSettingsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns settings from repository`() =
        runTest {
            // Given repository has settings
            val expectedSettings =
                LauncherSettings(
                    autoLaunchEnabled = false,
                    leftQuickAction = QuickActionConfig("com.test", "Test", false),
                    rightQuickAction = QuickActionConfig("com.test2", "Test2", false),
                    batteryIndicatorMode = BatteryThresholdMode.ALWAYS,
                    lastModified = 12345L,
                )
            mockRepository.settings = expectedSettings

            // When invoking use case
            val settings = useCase().first()

            // Then settings should match repository settings
            assertEquals(expectedSettings, settings)
        }

    @Test
    fun `invoke returns Flow that emits immediately`() =
        runTest {
            // Given repository has default settings
            mockRepository.settings = LauncherSettings()

            // When invoking use case
            val flow = useCase()

            // Then flow should emit immediately
            val settings = flow.first()
            assertEquals(LauncherSettings(), settings)
        }

    @Test
    fun `invoke creates new Flow on each call`() =
        runTest {
            // Given initial settings
            mockRepository.settings = LauncherSettings(autoLaunchEnabled = true)

            // When invoking use case twice
            val firstSettings = useCase().first()

            // And settings change
            mockRepository.settings = LauncherSettings(autoLaunchEnabled = false)

            // Then second call should get updated settings
            val secondSettings = useCase().first()

            assertEquals(true, firstSettings.autoLaunchEnabled)
            assertEquals(false, secondSettings.autoLaunchEnabled)
        }

    @Test
    fun `invoke handles all battery threshold modes`() =
        runTest {
            // Test each battery threshold mode
            val modes = BatteryThresholdMode.values()

            for (mode in modes) {
                mockRepository.settings = LauncherSettings(batteryIndicatorMode = mode)
                val settings = useCase().first()
                assertEquals(mode, settings.batteryIndicatorMode)
            }
        }

    // Test double
    private class FakeSettingsRepository : SettingsRepository {
        var settings = LauncherSettings()

        override fun getSettings(): Flow<LauncherSettings> = flowOf(settings)

        override suspend fun updateSettings(settings: LauncherSettings): Result<Unit> {
            this.settings = settings
            return Result.success(Unit)
        }

        override suspend fun resetToDefaults(): Result<Unit> {
            settings = LauncherSettings()
            return Result.success(Unit)
        }
    }
}
