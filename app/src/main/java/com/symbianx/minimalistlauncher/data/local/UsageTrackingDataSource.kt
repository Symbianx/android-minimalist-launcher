package com.symbianx.minimalistlauncher.data.local

interface UsageTrackingDataSource {
    fun getUsageData(): UsageData

    fun saveUsageData(data: UsageData)

    fun clearUsageData()

    fun incrementUnlockCount()

    fun updateLastUnlockTimestamp(timestamp: Long)

    fun incrementAppLaunchCount(packageName: String)

    fun updateAppLastLaunchTimestamp(
        packageName: String,
        timestamp: Long,
    )
}
