package com.symbianx.minimalistlauncher.domain.usecase

interface GetUsageStatsUseCase {
    suspend fun getHomeScreenStats(): HomeScreenStats

    suspend fun getAppStats(packageName: String): AppStats
}

data class HomeScreenStats(
    val unlockCount: Int,
    val lastUnlockTimeAgo: String?,
)

data class AppStats(
    val launchCount: Int,
    val lastLaunchTimeAgo: String?,
)
