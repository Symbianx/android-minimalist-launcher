package com.symbianx.minimalistlauncher.domain.model

import androidx.compose.runtime.Immutable

/**
 * Combined state for the launcher home screen.
 *
 * @property searchState Current search state
 * @property deviceStatus Current device status
 * @property nowPlayingInfo Current now playing information
 */
@Immutable
data class LauncherState(
    val searchState: SearchState = SearchState(),
    val deviceStatus: DeviceStatus = DeviceStatus(currentTime = "", currentDate = "", batteryPercentage = 0),
    val nowPlayingInfo: NowPlayingInfo = NowPlayingInfo()
)
