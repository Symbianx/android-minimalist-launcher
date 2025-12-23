package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.DailyUnlockSummary
import com.symbianx.minimalistlauncher.domain.repository.UsageTrackingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TrackUnlockUseCaseTest {
    private lateinit var repository: FakeUsageTrackingRepository
    private lateinit var useCase: TrackUnlockUseCase

    @Before
    fun setup() {
        repository = FakeUsageTrackingRepository()
        useCase = TrackUnlockUseCaseImpl(repository)
    }

    class FakeUsageTrackingRepository : UsageTrackingRepository {
        var shouldThrowException = false
        var unlockCount = 0

        override suspend fun recordUnlock(): DailyUnlockSummary {
            if (shouldThrowException) throw RuntimeException("Database error")
            unlockCount++
            return DailyUnlockSummary(
                date = "2025-12-23",
                unlockCount = unlockCount,
                lastUnlockTimestamp = 123456789L,
            )
        }

        override suspend fun recordAppLaunch(packageName: String) = throw NotImplementedError()

        override suspend fun getDailyUnlockSummary() = throw NotImplementedError()

        override suspend fun getAppLaunchSummary(packageName: String) = throw NotImplementedError()

        override suspend fun clearAllData() = throw NotImplementedError()
    }

    @Test
    fun invoke_returnsUnlockSummary_whenSuccessful() =
        runTest {
            val result = useCase.invoke()

            assertEquals(1, result.unlockCount)
            assertEquals(123456789L, result.lastUnlockTimestamp)
        }

    @Test
    fun invoke_returnsDefaultSummary_whenRepositoryThrowsException() =
        runTest {
            repository.shouldThrowException = true

            val result = useCase.invoke()

            assertEquals(0, result.unlockCount)
            assertEquals(0L, result.lastUnlockTimestamp)
        }

    @Test
    fun invoke_gracefullyDegrades_onAnyException() =
        runTest {
            repository.shouldThrowException = true

            val result = useCase.invoke()

            assertEquals(0, result.unlockCount)
            assertEquals(0L, result.lastUnlockTimestamp)
        }
}
