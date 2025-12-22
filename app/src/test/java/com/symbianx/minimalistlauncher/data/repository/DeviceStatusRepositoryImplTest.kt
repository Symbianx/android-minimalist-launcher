package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.system.BatteryDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class DeviceStatusRepositoryImplTest {

    private lateinit var repository: DeviceStatusRepositoryImpl
    private lateinit var mockBatteryDataSource: BatteryDataSource

    @Before
    fun setup() {
        // Create a mock battery data source that returns a simple flow
        mockBatteryDataSource = object : BatteryDataSource {
            override fun observeBatteryStatus(): Flow<Pair<Int, Boolean>> {
                return flowOf(Pair(100, false))
            }
        }
        
        repository = DeviceStatusRepositoryImpl(mockBatteryDataSource)
    }

    // Tests for calculateDelayToNextMinute

    @Test
    fun `calculateDelayToNextMinute at minute boundary returns 0`() {
        // Exactly at 10:30:00.000
        val time = LocalDateTime.of(2024, 12, 22, 10, 30, 0, 0)
        val delay = repository.calculateDelayToNextMinute(time)
        assertEquals(0L, delay)
    }

    @Test
    fun `calculateDelayToNextMinute at second boundary returns correct milliseconds`() {
        // At 10:30:45.000 (exactly 45 seconds into the minute)
        val time = LocalDateTime.of(2024, 12, 22, 10, 30, 45, 0)
        val delay = repository.calculateDelayToNextMinute(time)
        // Should return 15 seconds = 15,000 milliseconds
        assertEquals(15_000L, delay)
    }

    @Test
    fun `calculateDelayToNextMinute with nanoseconds returns correct milliseconds`() {
        // At 10:30:45.500000000 (45.5 seconds into the minute)
        val time = LocalDateTime.of(2024, 12, 22, 10, 30, 45, 500_000_000)
        val delay = repository.calculateDelayToNextMinute(time)
        // Should return 14.5 seconds = 14,500 milliseconds
        assertEquals(14_500L, delay)
    }

    @Test
    fun `calculateDelayToNextMinute with 1 second left returns correct milliseconds`() {
        // At 10:30:59.000 (exactly 59 seconds into the minute)
        val time = LocalDateTime.of(2024, 12, 22, 10, 30, 59, 0)
        val delay = repository.calculateDelayToNextMinute(time)
        // Should return 1 second = 1,000 milliseconds
        assertEquals(1_000L, delay)
    }

    @Test
    fun `calculateDelayToNextMinute with 1 second and nanoseconds left returns correct milliseconds`() {
        // At 10:30:59.250000000 (59.25 seconds into the minute)
        val time = LocalDateTime.of(2024, 12, 22, 10, 30, 59, 250_000_000)
        val delay = repository.calculateDelayToNextMinute(time)
        // Should return 0.75 seconds = 750 milliseconds
        assertEquals(750L, delay)
    }

    @Test
    fun `calculateDelayToNextMinute at start of minute with 1 nano returns correct milliseconds`() {
        // At 10:30:00.000000001 (1 nanosecond into the minute)
        val time = LocalDateTime.of(2024, 12, 22, 10, 30, 0, 1)
        val delay = repository.calculateDelayToNextMinute(time)
        // Should return nearly 60 seconds minus 1 millisecond
        // (60 - 1) * 1000 + (1_000_000_000 - 1) / 1_000_000 = 59_000 + 999 = 59_999
        assertEquals(59_999L, delay)
    }

    // Tests for calculateDelayToNextDay

    @Test
    fun `calculateDelayToNextDay at midnight returns 0`() {
        // Exactly at 00:00:00.000
        val time = LocalDateTime.of(2024, 12, 22, 0, 0, 0, 0)
        val delay = repository.calculateDelayToNextDay(time)
        assertEquals(0L, delay)
    }

    @Test
    fun `calculateDelayToNextDay at noon on second boundary returns correct milliseconds`() {
        // At 12:00:00.000 (noon)
        val time = LocalDateTime.of(2024, 12, 22, 12, 0, 0, 0)
        val delay = repository.calculateDelayToNextDay(time)
        // Should return 12 hours = 43,200,000 milliseconds
        assertEquals(43_200_000L, delay)
    }

    @Test
    fun `calculateDelayToNextDay at 23h59m59s returns correct milliseconds`() {
        // At 23:59:59.000 (1 second before midnight)
        val time = LocalDateTime.of(2024, 12, 22, 23, 59, 59, 0)
        val delay = repository.calculateDelayToNextDay(time)
        // Should return 1 second = 1,000 milliseconds
        assertEquals(1_000L, delay)
    }

    @Test
    fun `calculateDelayToNextDay with nanoseconds returns correct milliseconds`() {
        // At 23:59:59.500000000 (0.5 seconds before midnight)
        val time = LocalDateTime.of(2024, 12, 22, 23, 59, 59, 500_000_000)
        val delay = repository.calculateDelayToNextDay(time)
        // Should return 0.5 seconds = 500 milliseconds
        assertEquals(500L, delay)
    }

    @Test
    fun `calculateDelayToNextDay at start of day returns correct milliseconds`() {
        // At 00:00:01.000 (1 second after midnight)
        val time = LocalDateTime.of(2024, 12, 22, 0, 0, 1, 0)
        val delay = repository.calculateDelayToNextDay(time)
        // Should return 24 hours - 1 second = 86,399,000 milliseconds
        assertEquals(86_399_000L, delay)
    }

    @Test
    fun `calculateDelayToNextDay at arbitrary time returns correct milliseconds`() {
        // At 14:30:45.250000000
        val time = LocalDateTime.of(2024, 12, 22, 14, 30, 45, 250_000_000)
        val delay = repository.calculateDelayToNextDay(time)
        
        // Calculate expected delay manually:
        // Seconds in day: 14*3600 + 30*60 + 45 = 52245
        // Seconds until midnight: 86400 - 52245 = 34155
        // With 250ms nanoseconds: (34155 - 1) * 1000 + (1_000_000_000 - 250_000_000) / 1_000_000
        // = 34154 * 1000 + 750 = 34_154_750
        assertEquals(34_154_750L, delay)
    }

    @Test
    fun `calculateDelayToNextDay near midnight with nanoseconds returns correct milliseconds`() {
        // At 23:59:58.999999999 (1 nanosecond before 23:59:59)
        val time = LocalDateTime.of(2024, 12, 22, 23, 59, 58, 999_999_999)
        val delay = repository.calculateDelayToNextDay(time)
        // Seconds in day: 23*3600 + 59*60 + 58 = 86398
        // Seconds until midnight: 86400 - 86398 = 2
        // With nanoseconds: (2 - 1) * 1000 + (1_000_000_000 - 999_999_999) / 1_000_000
        // = 1000 + 0 = 1000 (the nanoseconds round to 0 milliseconds)
        assertEquals(1_000L, delay)
    }

    @Test
    fun `calculateDelayToNextMinute consistency check multiple times`() {
        // Test that delays are consistent for the same time
        val time = LocalDateTime.of(2024, 12, 22, 10, 30, 30, 500_000_000)
        val delay1 = repository.calculateDelayToNextMinute(time)
        val delay2 = repository.calculateDelayToNextMinute(time)
        assertEquals(delay1, delay2)
        // Expected: 29.5 seconds = 29,500 milliseconds
        assertEquals(29_500L, delay1)
    }

    @Test
    fun `calculateDelayToNextDay consistency check multiple times`() {
        // Test that delays are consistent for the same time
        val time = LocalDateTime.of(2024, 12, 22, 15, 45, 30, 750_000_000)
        val delay1 = repository.calculateDelayToNextDay(time)
        val delay2 = repository.calculateDelayToNextDay(time)
        assertEquals(delay1, delay2)
    }
}
