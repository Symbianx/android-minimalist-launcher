package com.symbianx.minimalistlauncher.data.system

import com.symbianx.minimalistlauncher.domain.model.NowPlayingInfo
import kotlinx.coroutines.flow.Flow

/**
 * Data source for Pixel's Now Playing ambient music recognition.
 */
interface NowPlayingDataSource {
    /**
     * Observes Now Playing song detection updates.
     *
     * @return Flow emitting currently playing song information
     */
    fun observeNowPlaying(): Flow<NowPlayingInfo>
}
