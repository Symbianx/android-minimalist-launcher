package com.symbianx.minimalistlauncher.domain.model

import androidx.compose.runtime.Immutable

/**
 * Combined state for the launcher home screen.
 *
 * @property searchState Current search state
 * @property deviceStatus Current device status
 */
@Immutable
data class LauncherState(
    val searchState: SearchState = SearchState(),
    val deviceStatus: DeviceStatus = DeviceStatus(currentTime = "", currentDate = "", batteryPercentage = 0),
)
