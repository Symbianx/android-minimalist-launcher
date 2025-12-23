package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.AppLaunchSummary

interface TrackAppLaunchUseCase {
    suspend operator fun invoke(packageName: String): AppLaunchSummary
}
