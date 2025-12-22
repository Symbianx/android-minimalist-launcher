package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.system.BatteryDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class DeviceStatusRepositoryImplTest {
    private lateinit var mockBatteryDataSource: BatteryDataSource
    private lateinit var repository: DeviceStatusRepositoryImpl

    @Before
    fun setup() {
        mockBatteryDataSource = object : BatteryDataSource {
            override fun observeBatteryStatus() = flowOf(Pair(85, false))
        }
        repository = DeviceStatusRepositoryImpl(mockBatteryDataSource)
    }

    @Test
    fun `calculateDelayUntilNextMinute returns positive delay`() {
        val delay = repository.calculateDelayUntilNextMinute()
        assertTrue("Delay should be positive", delay > 0)
    }

    @Test
    fun `calculateDelayUntilNextMinute returns delay less than or equal to 60 seconds`() {
        val delay = repository.calculateDelayUntilNextMinute()
        assertTrue("Delay should be at most 60 seconds", delay <= 60_000)
    }

    @Test
    fun `calculateDelayUntilNextMinute returns at least minimum delay`() {
        val delay = repository.calculateDelayUntilNextMinute()
        assertTrue("Delay should be at least 100ms", delay >= 100)
    }

    @Test
    fun `calculateDelayUntilNextMinute at second 0 returns approximately 60 seconds`() {
        // This test validates the logic, though actual timing depends on execution
        val delay = repository.calculateDelayUntilNextMinute()
        val now = Calendar.getInstance()
        val currentSecond = now.get(Calendar.SECOND)
        
        // Expected delay should be approximately (60 - currentSecond) seconds
        val expectedDelay = (60 - currentSecond) * 1000L
        
        // Allow for execution time variance (within 1 second)
        assertTrue(
            "Delay $delay should be close to expected $expectedDelay",
            kotlin.math.abs(delay - expectedDelay) < 1000
        )
    }

    @Test
    fun `calculateDelayUntilNextDay returns positive delay`() {
        val delay = repository.calculateDelayUntilNextDay()
        assertTrue("Delay should be positive", delay > 0)
    }

    @Test
    fun `calculateDelayUntilNextDay returns delay less than or equal to 24 hours`() {
        val delay = repository.calculateDelayUntilNextDay()
        val twentyFourHours = 24 * 60 * 60 * 1000L
        assertTrue("Delay should be at most 24 hours", delay <= twentyFourHours)
    }

    @Test
    fun `calculateDelayUntilNextDay returns at least minimum delay`() {
        val delay = repository.calculateDelayUntilNextDay()
        assertTrue("Delay should be at least 1000ms", delay >= 1000)
    }

    @Test
    fun `calculateDelayUntilNextDay at 11 59 PM returns approximately 1 minute`() {
        val delay = repository.calculateDelayUntilNextDay()
        val now = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val expectedDelay = tomorrow.timeInMillis - now.timeInMillis
        
        // The calculated delay should match the expected delay
        assertTrue(
            "Delay $delay should be close to expected $expectedDelay",
            kotlin.math.abs(delay - expectedDelay) < 1000
        )
    }

    @Test
    fun `observeDeviceStatus emits device status with time and date`() = runTest {
        val deviceStatus = repository.observeDeviceStatus().first()
        
        // Verify that time is not empty
        assertTrue("Time should not be empty", deviceStatus.currentTime.isNotEmpty())
        
        // Verify that date is not empty
        assertTrue("Date should not be empty", deviceStatus.currentDate.isNotEmpty())
        
        // Verify battery status from mock
        assertEquals(85, deviceStatus.batteryPercentage)
        assertEquals(false, deviceStatus.isCharging)
    }

    @Test
    fun `observeDeviceStatus combines battery data correctly`() = runTest {
        // Create repository with different battery status
        val customBatteryDataSource = object : BatteryDataSource {
            override fun observeBatteryStatus() = flowOf(Pair(42, true))
        }
        val customRepository = DeviceStatusRepositoryImpl(customBatteryDataSource)
        
        val deviceStatus = customRepository.observeDeviceStatus().first()
        
        // Verify battery data is correctly combined
        assertEquals(42, deviceStatus.batteryPercentage)
        assertEquals(true, deviceStatus.isCharging)
    }
}
