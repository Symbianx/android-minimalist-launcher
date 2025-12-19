package com.symbianx.minimalistlauncher.domain.repository

import com.symbianx.minimalistlauncher.domain.model.NowPlayingInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Now Playing ambient music recognition data.
 */
interface NowPlayingRepository {
    /**
     * Observes Now Playing information updates.
     *
     * @return Flow emitting currently detected song information
     */
    fun observeNowPlaying(): Flow<NowPlayingInfo>
}
