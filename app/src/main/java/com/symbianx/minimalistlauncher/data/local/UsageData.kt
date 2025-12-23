package com.symbianx.minimalistlauncher.data.local

import kotlinx.serialization.Serializable

@Serializable
data class UsageData(
    val currentDate: String,
    val unlockCount: Int = 0,
    val lastUnlockTimestamp: Long = 0,
    val appLaunches: Map<String, AppUsageData> = emptyMap(),
)

@Serializable
data class AppUsageData(
    val launchCount: Int,
    val lastLaunchTimestamp: Long,
)
