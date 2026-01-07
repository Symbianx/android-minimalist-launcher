package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.LauncherSettings
import com.symbianx.minimalistlauncher.domain.repository.SettingsRepository

/**
 * Use case for saving updated launcher settings.
 *
 * Encapsulates the business logic for persisting settings changes,
 * delegating to the repository for actual persistence.
 *
 * @property repository The settings repository for persistence operations
 */
class SaveSettingsUseCase(
    private val repository: SettingsRepository,
) {
    /**
     * Saves the provided settings.
     *
     * @param settings The settings to save
     * @return Result.success(Unit) if save succeeded, Result.failure(Exception) if failed
     */
    suspend operator fun invoke(settings: LauncherSettings): Result<Unit> = repository.updateSettings(settings)
}
