package com.symbianx.minimalistlauncher.domain.model

data class DailyUnlockSummary(
    val date: String,
    val unlockCount: Int,
    val lastUnlockTimestamp: Long,
)
