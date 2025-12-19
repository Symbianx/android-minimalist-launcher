package com.symbianx.minimalistlauncher.domain.repository

import com.symbianx.minimalistlauncher.domain.model.DeviceStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository for device status information (time and battery).
 */
interface DeviceStatusRepository {
    /**
     * Observes device status updates.
     *
     * @return Flow emitting current device status
     */
    fun observeDeviceStatus(): Flow<DeviceStatus>
}
