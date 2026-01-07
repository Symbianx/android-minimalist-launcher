package com.symbianx.minimalistlauncher.domain.repository

import com.symbianx.minimalistlauncher.domain.model.LauncherSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for launcher settings persistence and retrieval.
 *
 * Provides access to launcher settings with reactive updates, validation, and persistence guarantees.
 */
interface SettingsRepository {
    /**
     * Returns a Flow that emits the current settings and all future updates.
     *
     * Behavior:
     * - Emits current settings immediately on collection
     * - Emits updated settings whenever changed via updateSettings()
     * - Always emits valid settings (defaults if none exist)
     * - Validates quick action app installations on each emission
     * - Never emits null or throws exceptions
     * - Safe to collect multiple times
     *
     * @return Flow of LauncherSettings that emits current and future settings
     */
    fun getSettings(): Flow<LauncherSettings>

    /**
     * Persists updated settings atomically.
     *
     * Behavior:
     * - Validates settings before saving (checks installed apps)
     * - Writes atomically to DataStore (all-or-nothing)
     * - Updates lastModified timestamp automatically
     * - Triggers Flow emission to all observers
     * - Fails fast if validation fails
     *
     * @param settings Complete settings object with all fields
     * @return Result.success(Unit) if save succeeded, Result.failure(Exception) if failed
     */
    suspend fun updateSettings(settings: LauncherSettings): Result<Unit>

    /**
     * Resets all settings to their default values.
     *
     * Behavior:
     * - Clears all DataStore entries
     * - Next read returns default values
     * - Triggers Flow emission with defaults
     * - Logs reset event
     *
     * @return Result.success(Unit) if reset succeeded, Result.failure(Exception) if failed
     */
    suspend fun resetToDefaults(): Result<Unit>
}
