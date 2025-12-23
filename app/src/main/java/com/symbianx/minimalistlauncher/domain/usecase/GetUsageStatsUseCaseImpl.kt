package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.repository.UsageTrackingRepository
import com.symbianx.minimalistlauncher.util.TimeFormatter

class GetUsageStatsUseCaseImpl(
    private val repository: UsageTrackingRepository,
) : GetUsageStatsUseCase {
    override suspend fun getHomeScreenStats(): HomeScreenStats =
        try {
            val summary = repository.getDailyUnlockSummary()
            HomeScreenStats(
                unlockCount = summary.unlockCount,
                lastUnlockTimeAgo = TimeFormatter.formatRelativeTime(summary.lastUnlockTimestamp),
            )
        } catch (e: Exception) {
            HomeScreenStats(
                unlockCount = 0,
                lastUnlockTimeAgo = null,
            )
        }

    override suspend fun getAppStats(packageName: String): AppStats =
        try {
            val summary = repository.getAppLaunchSummary(packageName)
            AppStats(
                launchCount = summary.launchCount,
                lastLaunchTimeAgo = TimeFormatter.formatRelativeTime(summary.lastLaunchTimestamp),
            )
        } catch (e: Exception) {
            AppStats(
                launchCount = 0,
                lastLaunchTimeAgo = null,
            )
        }
}
