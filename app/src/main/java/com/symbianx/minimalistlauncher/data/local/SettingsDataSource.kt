package com.symbianx.minimalistlauncher.data.local

import com.symbianx.minimalistlauncher.domain.model.LauncherSettings
import kotlinx.coroutines.flow.Flow

/**
 * Low-level persistence layer that handles DataStore read/write operations for settings.
 *
 * This interface abstracts the DataStore implementation details from the repository layer.
 */
interface SettingsDataSource {
    /**
     * Reads settings from DataStore and maps to domain model.
     *
     * Behavior:
     * - Maps DataStore Preferences to LauncherSettings
     * - Returns defaults for missing keys
     * - Handles corrupted data gracefully
     * - Does NOT validate app installations (repository's responsibility)
     *
     * @return Flow of LauncherSettings emitting settings from DataStore
     */
    fun readSettings(): Flow<LauncherSettings>

    /**
     * Writes settings to DataStore atomically.
     *
     * Behavior:
     * - Serializes settings to DataStore keys
     * - Uses transaction for atomicity
     * - Overwrites all keys (not incremental)
     *
     * @param settings Settings to persist
     * @throws IOException if write fails
     */
    suspend fun writeSettings(settings: LauncherSettings)

    /**
     * Removes all settings from DataStore.
     *
     * Behavior:
     * - Clears all settings keys
     * - Used by resetToDefaults()
     */
    suspend fun clearSettings()
}
