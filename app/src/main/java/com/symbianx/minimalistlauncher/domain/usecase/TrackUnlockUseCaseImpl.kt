package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.DailyUnlockSummary
import com.symbianx.minimalistlauncher.domain.repository.UsageTrackingRepository
import java.time.LocalDate

class TrackUnlockUseCaseImpl(
    private val repository: UsageTrackingRepository,
) : TrackUnlockUseCase {
    override suspend fun invoke(): DailyUnlockSummary =
        try {
            repository.recordUnlock()
        } catch (e: Exception) {
            DailyUnlockSummary(
                date = LocalDate.now().toString(),
                unlockCount = 0,
                lastUnlockTimestamp = 0,
            )
        }
}
