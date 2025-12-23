package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.local.AppUsageData
import com.symbianx.minimalistlauncher.data.local.UsageData
import com.symbianx.minimalistlauncher.data.local.UsageTrackingDataSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UsageTrackingRepositoryImplTest {
    private lateinit var dataSource: FakeUsageTrackingDataSource
    private lateinit var repository: UsageTrackingRepositoryImpl

    @Before
    fun setup() {
        dataSource = FakeUsageTrackingDataSource()
        repository = UsageTrackingRepositoryImpl(dataSource)
    }

    class FakeUsageTrackingDataSource : UsageTrackingDataSource {
        private var _usageData =
            UsageData(
                currentDate = "2025-12-23",
                unlockCount = 0,
                lastUnlockTimestamp = 0L,
                appLaunches = emptyMap(),
            )
        var saveCallCount = 0

        override fun getUsageData(): UsageData = _usageData

        override fun incrementUnlockCount() {
            _usageData =
                _usageData.copy(
                    unlockCount = _usageData.unlockCount + 1,
                    lastUnlockTimestamp = System.currentTimeMillis(),
                )
        }

        override fun updateLastUnlockTimestamp(timestamp: Long) {
            _usageData = _usageData.copy(lastUnlockTimestamp = timestamp)
        }

        override fun incrementAppLaunchCount(packageName: String) {
            val appData = _usageData.appLaunches[packageName] ?: AppUsageData(0, 0)
            val updatedAppData =
                appData.copy(
                    launchCount = appData.launchCount + 1,
                    lastLaunchTimestamp = System.currentTimeMillis(),
                )
            _usageData =
                _usageData.copy(
                    appLaunches = _usageData.appLaunches + (packageName to updatedAppData),
                )
        }

        override fun updateAppLastLaunchTimestamp(
            packageName: String,
            timestamp: Long,
        ) {
            val appData = _usageData.appLaunches[packageName] ?: AppUsageData(0, 0)
            _usageData =
                _usageData.copy(
                    appLaunches = _usageData.appLaunches + (packageName to appData.copy(lastLaunchTimestamp = timestamp)),
                )
        }

        override fun saveUsageData(data: UsageData) {
            _usageData = data
            saveCallCount++
        }

        override fun clearUsageData() {
            _usageData =
                UsageData(
                    currentDate = "2025-12-23",
                    unlockCount = 0,
                    lastUnlockTimestamp = 0L,
                    appLaunches = emptyMap(),
                )
        }

        fun setUsageData(data: UsageData) {
            _usageData = data
        }
    }

    @Test
    fun recordUnlock_incrementsCount_andReturnsUpdatedSummary() =
        runTest {
            dataSource.setUsageData(
                UsageData(
                    currentDate = "2025-12-23",
                    unlockCount = 5,
                    lastUnlockTimestamp = 0L,
                    appLaunches = emptyMap(),
                ),
            )

            val result = repository.recordUnlock()

            assertEquals("2025-12-23", result.date)
            assertEquals(6, result.unlockCount)
            assertEquals(1, dataSource.saveCallCount)
        }

    @Test
    fun recordAppLaunch_createsNewEntry_whenAppNotTracked() =
        runTest {
            val result = repository.recordAppLaunch("com.example.app")

            assertEquals("com.example.app", result.packageName)
            assertEquals(1, result.launchCount)
        }

    @Test
    fun recordAppLaunch_incrementsExistingEntry() =
        runTest {
            dataSource.setUsageData(
                UsageData(
                    currentDate = "2025-12-23",
                    unlockCount = 0,
                    lastUnlockTimestamp = 0L,
                    appLaunches =
                        mapOf(
                            "com.example.app" to AppUsageData(3, 123456789L),
                        ),
                ),
            )

            val result = repository.recordAppLaunch("com.example.app")

            assertEquals(4, result.launchCount)
        }

    @Test(expected = IllegalArgumentException::class)
    fun recordAppLaunch_throwsException_whenPackageNameEmpty() =
        runTest {
            repository.recordAppLaunch("")
        }

    @Test
    fun getDailyUnlockSummary_returnsCurrentData() =
        runTest {
            dataSource.setUsageData(
                UsageData(
                    currentDate = "2025-12-23",
                    unlockCount = 7,
                    lastUnlockTimestamp = 987654321L,
                    appLaunches = emptyMap(),
                ),
            )

            val result = repository.getDailyUnlockSummary()

            assertEquals("2025-12-23", result.date)
            assertEquals(7, result.unlockCount)
            assertEquals(987654321L, result.lastUnlockTimestamp)
        }

    @Test
    fun getAppLaunchSummary_returnsAppData_whenExists() =
        runTest {
            dataSource.setUsageData(
                UsageData(
                    currentDate = "2025-12-23",
                    unlockCount = 0,
                    lastUnlockTimestamp = 0L,
                    appLaunches =
                        mapOf(
                            "com.example.app" to AppUsageData(5, 123456789L),
                        ),
                ),
            )

            val result = repository.getAppLaunchSummary("com.example.app")

            assertEquals("com.example.app", result.packageName)
            assertEquals(5, result.launchCount)
            assertEquals(123456789L, result.lastLaunchTimestamp)
        }

    @Test
    fun getAppLaunchSummary_returnsZeroData_whenAppNotTracked() =
        runTest {
            val result = repository.getAppLaunchSummary("com.example.app")

            assertEquals("com.example.app", result.packageName)
            assertEquals(0, result.launchCount)
            assertEquals(0L, result.lastLaunchTimestamp)
        }

    @Test
    fun clearAllData_delegatesToDataSource() =
        runTest {
            dataSource.setUsageData(
                UsageData(
                    currentDate = "2025-12-23",
                    unlockCount = 5,
                    lastUnlockTimestamp = 123456789L,
                    appLaunches = mapOf("com.example.app" to AppUsageData(3, 987654321L)),
                ),
            )

            repository.clearAllData()

            val result = dataSource.getUsageData()
            assertEquals(0, result.unlockCount)
            assertEquals(0L, result.lastUnlockTimestamp)
            assertEquals(0, result.appLaunches.size)
        }
}
