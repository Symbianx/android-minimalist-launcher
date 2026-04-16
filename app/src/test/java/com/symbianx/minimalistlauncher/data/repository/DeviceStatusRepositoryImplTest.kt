package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.system.BatteryDataSource
import com.symbianx.minimalistlauncher.data.system.TimeDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeviceStatusRepositoryImplTest {
    private lateinit var mockBatteryDataSource: BatteryDataSource
    private lateinit var mockTimeDataSource: TimeDataSource
    private lateinit var repository: DeviceStatusRepositoryImpl

    @Before
    fun setup() {
        mockBatteryDataSource =
            object : BatteryDataSource {
                override fun observeBatteryStatus() = flowOf(Pair(85, false))
            }
        mockTimeDataSource =
            object : TimeDataSource {
                override fun observeTimeTicks() = flowOf(Unit)
            }
        repository = DeviceStatusRepositoryImpl(mockBatteryDataSource, mockTimeDataSource)
    }

    @Test
    fun `observeDeviceStatus emits device status with time and date`() =
        runTest {
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
    fun `observeDeviceStatus combines battery data correctly`() =
        runTest {
            // Create repository with different battery status
            val customBatteryDataSource =
                object : BatteryDataSource {
                    override fun observeBatteryStatus() = flowOf(Pair(42, true))
                }
            val customRepository = DeviceStatusRepositoryImpl(customBatteryDataSource, mockTimeDataSource)

            val deviceStatus = customRepository.observeDeviceStatus().first()

            // Verify battery data is correctly combined
            assertEquals(42, deviceStatus.batteryPercentage)
            assertEquals(true, deviceStatus.isCharging)
        }
}
