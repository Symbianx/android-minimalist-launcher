package com.symbianx.minimalistlauncher.data.local

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class UsageTrackingDataSourceImplTest {
    private lateinit var dataSource: UsageTrackingDataSourceImpl
    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        prefs = context.getSharedPreferences("usage_data", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        dataSource = UsageTrackingDataSourceImpl(context)
    }

    @Test
    fun getUsageData_returnsDefaultData_whenNoDataExists() {
        val result = dataSource.getUsageData()
        assertEquals(0, result.unlockCount)
        assertEquals(0L, result.lastUnlockTimestamp)
        assertTrue(result.appLaunches.isEmpty())
    }

    @Test
    fun incrementUnlockCount_incrementsCount_andUpdatesTimestamp() {
        dataSource.incrementUnlockCount()
        val result = dataSource.getUsageData()
        assertEquals(1, result.unlockCount)
        assertTrue(result.lastUnlockTimestamp > 0)
    }

    @Test
    fun incrementUnlockCount_incrementsMultipleTimes() {
        dataSource.incrementUnlockCount()
        dataSource.incrementUnlockCount()
        dataSource.incrementUnlockCount()
        val result = dataSource.getUsageData()
        assertEquals(3, result.unlockCount)
    }

    @Test
    fun updateLastUnlockTimestamp_updatesTimestamp() {
        val testTimestamp = 123456789L
        dataSource.updateLastUnlockTimestamp(testTimestamp)
        val result = dataSource.getUsageData()
        assertEquals(testTimestamp, result.lastUnlockTimestamp)
    }

    @Test
    fun incrementAppLaunchCount_createsNewEntry_whenAppNotTracked() {
        dataSource.incrementAppLaunchCount("com.example.app")
        val result = dataSource.getUsageData()
        assertEquals(1, result.appLaunches["com.example.app"]?.launchCount)
        assertTrue(result.appLaunches["com.example.app"]?.lastLaunchTimestamp!! > 0)
    }

    @Test
    fun incrementAppLaunchCount_incrementsExistingEntry() {
        dataSource.incrementAppLaunchCount("com.example.app")
        dataSource.incrementAppLaunchCount("com.example.app")
        dataSource.incrementAppLaunchCount("com.example.app")
        val result = dataSource.getUsageData()
        assertEquals(3, result.appLaunches["com.example.app"]?.launchCount)
    }

    @Test
    fun incrementAppLaunchCount_tracksMultipleApps() {
        dataSource.incrementAppLaunchCount("com.example.app1")
        dataSource.incrementAppLaunchCount("com.example.app2")
        val result = dataSource.getUsageData()
        assertEquals(1, result.appLaunches["com.example.app1"]?.launchCount)
        assertEquals(1, result.appLaunches["com.example.app2"]?.launchCount)
    }

    @Test
    fun updateAppLastLaunchTimestamp_updatesTimestamp() {
        val testTimestamp = 987654321L
        dataSource.incrementAppLaunchCount("com.example.app")
        dataSource.updateAppLastLaunchTimestamp("com.example.app", testTimestamp)
        val result = dataSource.getUsageData()
        assertEquals(testTimestamp, result.appLaunches["com.example.app"]?.lastLaunchTimestamp)
    }

    @Test
    fun saveUsageData_persistsData() {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
        val testData =
            UsageData(
                currentDate = today,
                unlockCount = 5,
                lastUnlockTimestamp = 123456789L,
                appLaunches =
                    mapOf(
                        "com.example.app" to AppUsageData(3, 987654321L),
                    ),
            )
        dataSource.saveUsageData(testData)
        val result = dataSource.getUsageData()
        assertEquals(5, result.unlockCount)
        assertEquals(123456789L, result.lastUnlockTimestamp)
        assertEquals(3, result.appLaunches["com.example.app"]?.launchCount)
    }

    @Test
    fun clearUsageData_removesAllData() {
        dataSource.incrementUnlockCount()
        dataSource.incrementAppLaunchCount("com.example.app")
        dataSource.clearUsageData()
        val result = dataSource.getUsageData()
        assertEquals(0, result.unlockCount)
        assertTrue(result.appLaunches.isEmpty())
    }

    @Test
    fun getUsageData_handlesCorruptedData_gracefully() {
        prefs.edit().putString("usage_data", "invalid json").commit()
        val result = dataSource.getUsageData()
        assertEquals(0, result.unlockCount)
        assertTrue(result.appLaunches.isEmpty())
    }

    @Test
    fun dataSource_persistsAcrossInstances() {
        dataSource.incrementUnlockCount()
        val newDataSource = UsageTrackingDataSourceImpl(context)
        val result = newDataSource.getUsageData()
        assertEquals(1, result.unlockCount)
    }
}
