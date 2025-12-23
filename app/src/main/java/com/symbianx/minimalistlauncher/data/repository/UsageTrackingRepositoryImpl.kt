package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.local.AppUsageData
import com.symbianx.minimalistlauncher.data.local.UsageTrackingDataSource
import com.symbianx.minimalistlauncher.domain.model.AppLaunchSummary
import com.symbianx.minimalistlauncher.domain.model.DailyUnlockSummary
import com.symbianx.minimalistlauncher.domain.repository.UsageTrackingRepository

class UsageTrackingRepositoryImpl(
    private val dataSource: UsageTrackingDataSource,
) : UsageTrackingRepository {
    override suspend fun recordUnlock(): DailyUnlockSummary {
        val current = dataSource.getUsageData()
        val updated =
            current.copy(
                unlockCount = current.unlockCount + 1,
                lastUnlockTimestamp = System.currentTimeMillis(),
            )
        dataSource.saveUsageData(updated)
        return DailyUnlockSummary(
            date = updated.currentDate,
            unlockCount = updated.unlockCount,
            lastUnlockTimestamp = updated.lastUnlockTimestamp,
        )
    }

    override suspend fun recordAppLaunch(packageName: String): AppLaunchSummary {
        require(packageName.isNotEmpty()) { "packageName cannot be empty" }
        val current = dataSource.getUsageData()
        val appData = current.appLaunches[packageName]
        val updatedAppData =
            AppUsageData(
                launchCount = (appData?.launchCount ?: 0) + 1,
                lastLaunchTimestamp = System.currentTimeMillis(),
            )
        val updated =
            current.copy(
                appLaunches = current.appLaunches + (packageName to updatedAppData),
            )
        dataSource.saveUsageData(updated)
        return AppLaunchSummary(
            packageName = packageName,
            launchCount = updatedAppData.launchCount,
            lastLaunchTimestamp = updatedAppData.lastLaunchTimestamp,
        )
    }

    override suspend fun getDailyUnlockSummary(): DailyUnlockSummary {
        val current = dataSource.getUsageData()
        return DailyUnlockSummary(
            date = current.currentDate,
            unlockCount = current.unlockCount,
            lastUnlockTimestamp = current.lastUnlockTimestamp,
        )
    }

    override suspend fun getAppLaunchSummary(packageName: String): AppLaunchSummary {
        val current = dataSource.getUsageData()
        val appData = current.appLaunches[packageName]
        return AppLaunchSummary(
            packageName = packageName,
            launchCount = appData?.launchCount ?: 0,
            lastLaunchTimestamp = appData?.lastLaunchTimestamp ?: 0,
        )
    }

    override suspend fun clearAllData() {
        dataSource.clearUsageData()
    }
}
