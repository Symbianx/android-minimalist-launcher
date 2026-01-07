package com.symbianx.minimalistlauncher.domain.repository

import com.symbianx.minimalistlauncher.domain.model.AppLaunchSummary
import com.symbianx.minimalistlauncher.domain.model.DailyUnlockSummary

interface UsageTrackingRepository {
    suspend fun recordUnlock(): DailyUnlockSummary

    suspend fun recordAppLaunch(packageName: String): AppLaunchSummary

    suspend fun getDailyUnlockSummary(): DailyUnlockSummary

    suspend fun getAppLaunchSummary(packageName: String): AppLaunchSummary

    suspend fun clearAllData()
}
