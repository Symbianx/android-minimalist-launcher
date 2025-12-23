package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.AppLaunchSummary
import com.symbianx.minimalistlauncher.domain.model.DailyUnlockSummary
import com.symbianx.minimalistlauncher.domain.repository.UsageTrackingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetUsageStatsUseCaseTest {
    private lateinit var repository: FakeUsageTrackingRepository
    private lateinit var useCase: GetUsageStatsUseCase

    @Before
    fun setup() {
        repository = FakeUsageTrackingRepository()
        useCase = GetUsageStatsUseCaseImpl(repository)
    }

    class FakeUsageTrackingRepository : UsageTrackingRepository {
        var shouldThrowException = false
        var unlockSummary = DailyUnlockSummary("2025-12-23", 0, 0L)
        val appSummaries = mutableMapOf<String, AppLaunchSummary>()

        override suspend fun recordUnlock() =
            throw NotImplementedError()

        override suspend fun recordAppLaunch(packageName: String) =
            throw NotImplementedError()

        override suspend fun getDailyUnlockSummary(): DailyUnlockSummary {
            if (shouldThrowException) throw RuntimeException("Database error")
            return unlockSummary
        }

        override suspend fun getAppLaunchSummary(packageName: String): AppLaunchSummary {
            if (shouldThrowException) throw RuntimeException("Database error")
            return appSummaries[packageName] ?: AppLaunchSummary(packageName, 0, 0L)
        }

        override suspend fun clearAllData() =
            throw NotImplementedError()
    }

    @Test
    fun getHomeScreenStats_returnsStats_whenSuccessful() =
        runTest {
            val now = System.currentTimeMillis()
            val fiveMinutesAgo = now - (5 * 60 * 1000)
            repository.unlockSummary = DailyUnlockSummary(
                date = "2025-12-23",
                unlockCount = 7,
                lastUnlockTimestamp = fiveMinutesAgo
            )

            val result = useCase.getHomeScreenStats()

            assertEquals(7, result.unlockCount)
            assertEquals("5m ago", result.lastUnlockTimeAgo)
        }

    @Test
    fun getHomeScreenStats_returnsNull_whenNoUnlocks() =
        runTest {
            repository.unlockSummary = DailyUnlockSummary(
                date = "2025-12-23",
                unlockCount = 0,
                lastUnlockTimestamp = 0L
            )

            val result = useCase.getHomeScreenStats()

            assertEquals(0, result.unlockCount)
            assertNull(result.lastUnlockTimeAgo)
        }

    @Test
    fun getHomeScreenStats_returnsDefaultStats_whenRepositoryThrowsException() =
        runTest {
            repository.shouldThrowException = true

            val result = useCase.getHomeScreenStats()

            assertEquals(0, result.unlockCount)
            assertNull(result.lastUnlockTimeAgo)
        }

    @Test
    fun getAppStats_returnsStats_whenSuccessful() =
        runTest {
            val now = System.currentTimeMillis()
            val fifteenMinutesAgo = now - (15 * 60 * 1000)
            repository.appSummaries["com.example.app"] = AppLaunchSummary(
                packageName = "com.example.app",
                launchCount = 3,
                lastLaunchTimestamp = fifteenMinutesAgo
            )

            val result = useCase.getAppStats("com.example.app")

            assertEquals(3, result.launchCount)
            assertEquals("15m ago", result.lastLaunchTimeAgo)
        }

    @Test
    fun getAppStats_returnsNull_whenNeverLaunched() =
        runTest {
            val result = useCase.getAppStats("com.example.app")

            assertEquals(0, result.launchCount)
            assertNull(result.lastLaunchTimeAgo)
        }

    @Test
    fun getAppStats_returnsDefaultStats_whenRepositoryThrowsException() =
        runTest {
            repository.shouldThrowException = true

            val result = useCase.getAppStats("com.example.app")

            assertEquals(0, result.launchCount)
            assertNull(result.lastLaunchTimeAgo)
        }

    @Test
    fun getAppStats_gracefullyDegrades_onAnyException() =
        runTest {
            repository.shouldThrowException = true

            val result = useCase.getAppStats("com.example.app")

            assertEquals(0, result.launchCount)
            assertNull(result.lastLaunchTimeAgo)
        }
}
