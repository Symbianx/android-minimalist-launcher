package com.symbianx.minimalistlauncher.domain.model

import androidx.compose.runtime.Immutable

/**
 * Represents currently detected ambient music from Pixel's Now Playing feature.
 *
 * @property songName Song title (null if no song detected)
 * @property artistName Artist name (null if unavailable)
 * @property timestamp Detection timestamp in epoch milliseconds
 * @property isAvailable Whether Now Playing feature is available/enabled
 */
@Immutable
data class NowPlayingInfo(
    val songName: String? = null,
    val artistName: String? = null,
    val timestamp: Long = 0L,
    val isAvailable: Boolean = true
)
