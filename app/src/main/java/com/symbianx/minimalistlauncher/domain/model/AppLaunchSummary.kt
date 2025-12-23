package com.symbianx.minimalistlauncher.domain.model

data class AppLaunchSummary(
    val packageName: String,
    val launchCount: Int,
    val lastLaunchTimestamp: Long,
)
