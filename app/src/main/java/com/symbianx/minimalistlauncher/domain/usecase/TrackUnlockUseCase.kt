package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.DailyUnlockSummary

interface TrackUnlockUseCase {
    suspend operator fun invoke(): DailyUnlockSummary
}
