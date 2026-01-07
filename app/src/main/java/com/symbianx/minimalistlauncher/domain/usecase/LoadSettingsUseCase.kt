package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.LauncherSettings
import com.symbianx.minimalistlauncher.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for loading launcher settings.
 * Delegates to SettingsRepository to retrieve current settings.
 *
 * @param repository Settings repository
 */
class LoadSettingsUseCase(
    private val repository: SettingsRepository
) {
    /**
     * Loads launcher settings as a Flow.
     *
     * @return Flow emitting current launcher settings
     */
    operator fun invoke(): Flow<LauncherSettings> = repository.getSettings()
}
