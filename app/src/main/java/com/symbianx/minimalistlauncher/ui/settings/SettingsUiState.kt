package com.symbianx.minimalistlauncher.ui.settings

import com.symbianx.minimalistlauncher.domain.model.LauncherSettings

/**
 * UI state for the Settings screen.
 */
sealed interface SettingsUiState {
    /**
     * Loading state while settings are being retrieved.
     */
    data object Loading : SettingsUiState

    /**
     * Loaded state with current settings.
     *
     * @property settings Current launcher settings
     */
    data class Loaded(
        val settings: LauncherSettings,
    ) : SettingsUiState

    /**
     * Error state when settings cannot be loaded.
     *
     * @property message Error message to display
     */
    data class Error(
        val message: String,
    ) : SettingsUiState
}
