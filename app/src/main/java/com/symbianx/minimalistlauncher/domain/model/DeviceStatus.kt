package com.symbianx.minimalistlauncher.domain.model

import androidx.compose.runtime.Immutable

/**
 * Represents current device status for home screen display.
 *
 * @property currentTime Formatted time string (e.g., "2:45 PM" or "14:45")
 * @property currentDate Formatted date string (e.g., "Thu, Dec 19")
 * @property batteryPercentage Battery level 0-100
 * @property isCharging Whether the device is currently charging
 */
@Immutable
data class DeviceStatus(
    val currentTime: String,
    val currentDate: String,
    val batteryPercentage: Int,
    val isCharging: Boolean = false
)
