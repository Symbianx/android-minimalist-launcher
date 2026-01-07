package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.LauncherSettings
import com.symbianx.minimalistlauncher.domain.repository.SettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for SaveSettingsUseCase.
 *
 * Tests settings update functionality including auto-launch toggle and error handling.
 */
class SaveSettingsUseCaseTest {
    private lateinit var repository: SettingsRepository
    private lateinit var useCase: SaveSettingsUseCase

    @Before
    fun setup() {
        repository = mock()
        useCase = SaveSettingsUseCase(repository)
    }

    @Test
    fun `invoke calls repository updateSettings with provided settings`() =
        runTest {
            // Given
            val settings = LauncherSettings(autoLaunchEnabled = false)
            whenever(repository.updateSettings(settings)).thenReturn(Result.success(Unit))

            // When
            val result = useCase(settings)

            // Then
            verify(repository).updateSettings(settings)
            assertTrue(result.isSuccess)
        }

    @Test
    fun `invoke returns success when repository succeeds`() =
        runTest {
            // Given
            val settings = LauncherSettings.defaults()
            whenever(repository.updateSettings(settings)).thenReturn(Result.success(Unit))

            // When
            val result = useCase(settings)

            // Then
            assertTrue(result.isSuccess)
        }

    @Test
    fun `invoke returns failure when repository fails`() =
        runTest {
            // Given
            val settings = LauncherSettings.defaults()
            val error = Exception("Database error")
            whenever(repository.updateSettings(settings)).thenReturn(Result.failure(error))

            // When
            val result = useCase(settings)

            // Then
            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }

    @Test
    fun `invoke correctly updates auto-launch setting`() =
        runTest {
            // Given
            val settingsWithAutoLaunchOff = LauncherSettings(autoLaunchEnabled = false)
            whenever(repository.updateSettings(settingsWithAutoLaunchOff)).thenReturn(Result.success(Unit))

            // When
            val result = useCase(settingsWithAutoLaunchOff)

            // Then
            verify(repository).updateSettings(settingsWithAutoLaunchOff)
            assertTrue(result.isSuccess)
        }
}
