package com.symbianx.minimalistlauncher.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.symbianx.minimalistlauncher.domain.model.BatteryThresholdMode
import com.symbianx.minimalistlauncher.domain.model.LauncherSettings
import com.symbianx.minimalistlauncher.domain.model.QuickActionConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore extension for settings preferences.
 */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "launcher_settings",
)

/**
 * Implementation of SettingsDataSource using DataStore Preferences.
 * Handles persistence of launcher settings to local storage.
 *
 * @param context Application context for DataStore access
 * @param dataStore Optional DataStore instance for testing
 */
class SettingsDataSourceImpl(
    private val context: Context,
    private val dataStore: DataStore<Preferences> = context.settingsDataStore,
) : SettingsDataSource {
    private object Keys {
        val AUTO_LAUNCH_ENABLED = booleanPreferencesKey("auto_launch_enabled")
        val LEFT_QUICK_ACTION_PACKAGE = stringPreferencesKey("left_quick_action_package")
        val LEFT_QUICK_ACTION_LABEL = stringPreferencesKey("left_quick_action_label")
        val RIGHT_QUICK_ACTION_PACKAGE = stringPreferencesKey("right_quick_action_package")
        val RIGHT_QUICK_ACTION_LABEL = stringPreferencesKey("right_quick_action_label")
        val BATTERY_THRESHOLD_MODE = stringPreferencesKey("battery_threshold_mode")
        val CLOCK_APP_PACKAGE = stringPreferencesKey("clock_app_package")
        val CLOCK_APP_LABEL = stringPreferencesKey("clock_app_label")
        val LAST_MODIFIED = longPreferencesKey("last_modified")
    }

    override fun readSettings(): Flow<LauncherSettings> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                LauncherSettings(
                    autoLaunchEnabled = preferences[Keys.AUTO_LAUNCH_ENABLED] ?: true,
                    leftQuickAction =
                        QuickActionConfig(
                            packageName =
                                preferences[Keys.LEFT_QUICK_ACTION_PACKAGE]
                                    ?: "com.google.android.dialer",
                            label = preferences[Keys.LEFT_QUICK_ACTION_LABEL] ?: "Phone",
                            isDefault = preferences[Keys.LEFT_QUICK_ACTION_PACKAGE] == null,
                        ),
                    rightQuickAction =
                        QuickActionConfig(
                            packageName =
                                preferences[Keys.RIGHT_QUICK_ACTION_PACKAGE]
                                    ?: "com.google.android.GoogleCamera",
                            label = preferences[Keys.RIGHT_QUICK_ACTION_LABEL] ?: "Camera",
                            isDefault = preferences[Keys.RIGHT_QUICK_ACTION_PACKAGE] == null,
                        ),
                    batteryIndicatorMode =
                        preferences[Keys.BATTERY_THRESHOLD_MODE]?.let {
                            try {
                                BatteryThresholdMode.valueOf(it)
                            } catch (e: IllegalArgumentException) {
                                BatteryThresholdMode.BELOW_50
                            }
                        } ?: BatteryThresholdMode.BELOW_50,
                    clockAppPackage = preferences[Keys.CLOCK_APP_PACKAGE],
                    clockAppLabel = preferences[Keys.CLOCK_APP_LABEL],
                    lastModified = preferences[Keys.LAST_MODIFIED] ?: System.currentTimeMillis(),
                )
            }

    override suspend fun writeSettings(settings: LauncherSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.AUTO_LAUNCH_ENABLED] = settings.autoLaunchEnabled
            preferences[Keys.LEFT_QUICK_ACTION_PACKAGE] = settings.leftQuickAction.packageName
            preferences[Keys.LEFT_QUICK_ACTION_LABEL] = settings.leftQuickAction.label
            preferences[Keys.RIGHT_QUICK_ACTION_PACKAGE] = settings.rightQuickAction.packageName
            preferences[Keys.RIGHT_QUICK_ACTION_LABEL] = settings.rightQuickAction.label
            preferences[Keys.BATTERY_THRESHOLD_MODE] = settings.batteryIndicatorMode.name
            if (settings.clockAppPackage != null) {
                preferences[Keys.CLOCK_APP_PACKAGE] = settings.clockAppPackage
            } else {
                preferences.remove(Keys.CLOCK_APP_PACKAGE)
            }
            if (settings.clockAppLabel != null) {
                preferences[Keys.CLOCK_APP_LABEL] = settings.clockAppLabel
            } else {
                preferences.remove(Keys.CLOCK_APP_LABEL)
            }
            preferences[Keys.LAST_MODIFIED] = System.currentTimeMillis()
        }
    }

    override suspend fun clearSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
