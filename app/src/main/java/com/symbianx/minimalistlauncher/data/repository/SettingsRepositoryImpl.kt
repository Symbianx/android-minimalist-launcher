package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.local.SettingsDataSource
import com.symbianx.minimalistlauncher.domain.model.LauncherSettings
import com.symbianx.minimalistlauncher.domain.model.QuickActionConfig
import com.symbianx.minimalistlauncher.domain.repository.AppRepository
import com.symbianx.minimalistlauncher.domain.repository.SettingsRepository
import com.symbianx.minimalistlauncher.util.SettingsLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Implementation of SettingsRepository with validation logic.
 * Validates settings on load to ensure quick action apps are still installed.
 *
 * @param dataSource Data source for settings persistence
 * @param appRepository Repository for app information
 * @param logger Logger for settings changes
 */
class SettingsRepositoryImpl(
    private val dataSource: SettingsDataSource,
    private val appRepository: AppRepository,
    private val logger: SettingsLogger
) : SettingsRepository {

    override fun getSettings(): Flow<LauncherSettings> {
        return dataSource.readSettings().map { settings ->
            validateAndCorrect(settings)
        }
    }

    override suspend fun updateSettings(settings: LauncherSettings): Result<Unit> {
        return try {
            val validated = validateAndCorrect(settings)
            dataSource.writeSettings(validated)
            logger.log("Settings updated: autoLaunch=${validated.autoLaunchEnabled}, battery=${validated.batteryIndicatorMode}")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.log("Failed to update settings: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun resetToDefaults(): Result<Unit> {
        return try {
            dataSource.clearSettings()
            logger.log("Settings reset to defaults")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.log("Failed to reset settings: ${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun validateAndCorrect(settings: LauncherSettings): LauncherSettings {
        val installedApps = appRepository.getApps().first()
        val installedPackages = installedApps.map { it.packageName }.toSet()

        val leftValid = settings.leftQuickAction.packageName in installedPackages
        val rightValid = settings.rightQuickAction.packageName in installedPackages

        if (!leftValid || !rightValid) {
            logger.log("Quick action app(s) uninstalled, reverting to defaults")
        }

        return settings.copy(
            leftQuickAction = if (leftValid) settings.leftQuickAction else QuickActionConfig.defaultLeft(),
            rightQuickAction = if (rightValid) settings.rightQuickAction else QuickActionConfig.defaultRight()
        )
    }
}
