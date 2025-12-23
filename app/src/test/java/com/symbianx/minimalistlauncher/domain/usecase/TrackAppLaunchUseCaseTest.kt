package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.AppLaunchSummary
import com.symbianx.minimalistlauncher.domain.repository.UsageTrackingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TrackAppLaunchUseCaseTest {
    private lateinit var repository: FakeUsageTrackingRepository
    private lateinit var useCase: TrackAppLaunchUseCase

    @Before
    fun setup() {
        repository = FakeUsageTrackingRepository()
        useCase = TrackAppLaunchUseCaseImpl(repository)
    }

    class FakeUsageTrackingRepository : UsageTrackingRepository {
        var shouldThrowException = false
        val launchCounts = mutableMapOf<String, Int>()

        override suspend fun recordUnlock() = throw NotImplementedError()

        override suspend fun recordAppLaunch(packageName: String): AppLaunchSummary {
            if (shouldThrowException) throw RuntimeException("Database error")
            val count = (launchCounts[packageName] ?: 0) + 1
            launchCounts[packageName] = count
            return AppLaunchSummary(
                packageName = packageName,
                launchCount = count,
                lastLaunchTimestamp = 123456789L,
            )
        }

        override suspend fun getDailyUnlockSummary() = throw NotImplementedError()

        override suspend fun getAppLaunchSummary(packageName: String) = throw NotImplementedError()

        override suspend fun clearAllData() = throw NotImplementedError()
    }

    @Test
    fun invoke_returnsAppLaunchSummary_whenSuccessful() =
        runTest {
            val result = useCase.invoke("com.example.app")

            assertEquals("com.example.app", result.packageName)
            assertEquals(1, result.launchCount)
            assertEquals(123456789L, result.lastLaunchTimestamp)
        }

    @Test
    fun invoke_returnsDefaultSummary_whenRepositoryThrowsException() =
        runTest {
            repository.shouldThrowException = true

            val result = useCase.invoke("com.example.app")

            assertEquals("com.example.app", result.packageName)
            assertEquals(0, result.launchCount)
            assertEquals(0L, result.lastLaunchTimestamp)
        }

    @Test
    fun invoke_gracefullyDegrades_onAnyException() =
        runTest {
            repository.shouldThrowException = true

            val result = useCase.invoke("com.example.app")

            assertEquals("com.example.app", result.packageName)
            assertEquals(0, result.launchCount)
            assertEquals(0L, result.lastLaunchTimestamp)
        }
}
