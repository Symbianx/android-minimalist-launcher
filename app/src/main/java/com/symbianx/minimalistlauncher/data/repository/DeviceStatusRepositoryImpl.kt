package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.system.BatteryDataSource
import com.symbianx.minimalistlauncher.domain.model.DeviceStatus
import com.symbianx.minimalistlauncher.domain.repository.DeviceStatusRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
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
            while (true) {
                val timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault())
                emit(timeFormat.format(Date()))
                delay(1_000) // Update every second to ensure clock is always accurate
            }
        }

    private val dateFlow: Flow<String> =
        flow {
            while (true) {
                val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
                emit(dateFormat.format(Date()))
                delay(60_000) // Update every minute - dates don't change frequently
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
