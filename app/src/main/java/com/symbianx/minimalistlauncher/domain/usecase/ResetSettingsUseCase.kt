package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.repository.SettingsRepository

/**
 * Use case for resetting all settings to default values.
 *
 * Encapsulates the business logic for clearing all custom settings
 * and restoring defaults.
 *
 * @property repository The settings repository for reset operations
 */
class ResetSettingsUseCase(
    private val repository: SettingsRepository,
) {
    /**
     * Resets all settings to their default values.
     *
     * @return Result.success(Unit) if reset succeeded, Result.failure(Exception) if failed
     */
    suspend operator fun invoke(): Result<Unit> = repository.resetToDefaults()
}
