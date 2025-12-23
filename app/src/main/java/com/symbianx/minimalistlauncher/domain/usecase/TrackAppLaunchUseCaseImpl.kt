package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.AppLaunchSummary
import com.symbianx.minimalistlauncher.domain.repository.UsageTrackingRepository

class TrackAppLaunchUseCaseImpl(
    private val repository: UsageTrackingRepository,
) : TrackAppLaunchUseCase {
    override suspend fun invoke(packageName: String): AppLaunchSummary =
        try {
            repository.recordAppLaunch(packageName)
        } catch (e: Exception) {
            AppLaunchSummary(
                packageName = packageName,
                launchCount = 0,
                lastLaunchTimestamp = 0,
            )
        }
}
