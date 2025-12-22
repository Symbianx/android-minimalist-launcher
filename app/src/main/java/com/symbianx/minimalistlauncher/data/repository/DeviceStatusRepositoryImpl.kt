package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.system.BatteryDataSource
import com.symbianx.minimalistlauncher.domain.model.DeviceStatus
import com.symbianx.minimalistlauncher.domain.repository.DeviceStatusRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Implementation of [DeviceStatusRepository].
 */
class DeviceStatusRepositoryImpl(
    private val batteryDataSource: BatteryDataSource,
) : DeviceStatusRepository {
    private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd", Locale.getDefault())

    /**
     * Calculates milliseconds until the next minute boundary for battery-efficient updates.
     */
    private fun calculateDelayToNextMinute(now: LocalDateTime): Long {
        // If we're exactly at a minute boundary, no delay needed
        if (now.second == 0 && now.nano == 0) {
            return 0
        }
        
        val secondsUntilNextMinute = 60 - now.second
        val nanosUntilNextMinute = 1_000_000_000 - now.nano
        return (secondsUntilNextMinute - 1) * 1_000L + nanosUntilNextMinute / 1_000_000L
    }

    private val timeFlow: Flow<String> =
        flow {
            // Emit immediately for fresh display when UI becomes visible
            val now = LocalDateTime.now()
            emit(now.format(timeFormatter))
            
            // Calculate delay to next minute boundary for battery efficiency
            val delayMillis = calculateDelayToNextMinute(now)
            if (delayMillis > 0) {
                delay(delayMillis)
            }
            
            // Then update every minute, aligned to minute changes
            while (true) {
                emit(LocalDateTime.now().format(timeFormatter))
                delay(60_000) // Update every minute
            }
        }

    private val dateFlow: Flow<String> =
        flow {
            // Emit immediately for fresh display when UI becomes visible
            val now = LocalDateTime.now()
            emit(now.format(dateFormatter))
            
            // Calculate delay to next minute boundary for consistency
            val delayMillis = calculateDelayToNextMinute(now)
            if (delayMillis > 0) {
                delay(delayMillis)
            }
            
            // Then update every minute, aligned to minute changes
            while (true) {
                emit(LocalDateTime.now().format(dateFormatter))
                delay(60_000) // Update every minute
            }
        }

    override fun observeDeviceStatus(): Flow<DeviceStatus> =
        combine(
            timeFlow,
            dateFlow,
            batteryDataSource.observeBatteryStatus(),
        ) { time, date, (batteryPercentage, isCharging) ->
            DeviceStatus(
                currentTime = time,
                currentDate = date,
                batteryPercentage = batteryPercentage,
                isCharging = isCharging,
            )
        }
}
