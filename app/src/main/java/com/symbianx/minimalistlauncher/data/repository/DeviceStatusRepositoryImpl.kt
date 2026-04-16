package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.system.BatteryDataSource
import com.symbianx.minimalistlauncher.data.system.TimeDataSource
import com.symbianx.minimalistlauncher.domain.model.DeviceStatus
import com.symbianx.minimalistlauncher.domain.repository.DeviceStatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implementation of [DeviceStatusRepository].
 *
 * Uses system time tick broadcasts (ACTION_TIME_TICK, ACTION_TIME_CHANGED,
 * ACTION_TIMEZONE_CHANGED) to drive clock updates instead of manual delay
 * calculations, ensuring the display always reflects the current timezone.
 */
class DeviceStatusRepositoryImpl(
    private val batteryDataSource: BatteryDataSource,
    private val timeDataSource: TimeDataSource,
) : DeviceStatusRepository {
    override fun observeDeviceStatus(): Flow<DeviceStatus> =
        combine(
            timeDataSource.observeTimeTicks(),
            batteryDataSource.observeBatteryStatus(),
        ) { _, (batteryPercentage, isCharging) ->
            val now = Date()
            val timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            DeviceStatus(
                currentTime = timeFormat.format(now),
                currentDate = dateFormat.format(now),
                batteryPercentage = batteryPercentage,
                isCharging = isCharging,
            )
        }
}
