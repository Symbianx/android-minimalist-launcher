package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.system.BatteryDataSource
import com.symbianx.minimalistlauncher.domain.model.DeviceStatus
import com.symbianx.minimalistlauncher.domain.repository.DeviceStatusRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Implementation of [DeviceStatusRepository].
 */
class DeviceStatusRepositoryImpl(
    private val batteryDataSource: BatteryDataSource,
) : DeviceStatusRepository {
    private val timeFlow: Flow<String> =
        flow {
            val timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault())
            while (true) {
                // Emit current time immediately
                emit(timeFormat.format(Date()))
                
                // Calculate delay until next minute change for battery optimization
                val delayUntilNextMinute = calculateDelayUntilNextMinute()
                delay(delayUntilNextMinute)
            }
        }

    private val dateFlow: Flow<String> =
        flow {
            val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            while (true) {
                // Emit current date immediately
                emit(dateFormat.format(Date()))
                
                // Calculate delay until next day change for battery optimization
                val delayUntilNextDay = calculateDelayUntilNextDay()
                delay(delayUntilNextDay)
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

    /**
     * Calculates the delay in milliseconds until the next minute change.
     * This ensures the clock updates exactly when the minute changes for battery optimization.
     */
    internal fun calculateDelayUntilNextMinute(): Long {
        val now = Calendar.getInstance()
        val seconds = now.get(Calendar.SECOND)
        val milliseconds = now.get(Calendar.MILLISECOND)
        
        // Calculate remaining time in current minute
        val remainingSeconds = 60 - seconds
        val remainingMillis = remainingSeconds * 1000L - milliseconds
        
        // Ensure we always have a positive delay (minimum 100ms to avoid timing edge cases)
        return remainingMillis.coerceAtLeast(100L)
    }

    /**
     * Calculates the delay in milliseconds until the next day change (midnight).
     * This ensures the date updates exactly when the day changes for battery optimization.
     */
    internal fun calculateDelayUntilNextDay(): Long {
        val now = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val delayMillis = tomorrow.timeInMillis - now.timeInMillis
        
        // Ensure we always have a positive delay (minimum 1000ms to avoid timing edge cases)
        return delayMillis.coerceAtLeast(1000L)
    }
}
