package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.system.NowPlayingDataSource
import com.symbianx.minimalistlauncher.domain.model.NowPlayingInfo
import com.symbianx.minimalistlauncher.domain.repository.NowPlayingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of [NowPlayingRepository].
 */
class NowPlayingRepositoryImpl(
    private val nowPlayingDataSource: NowPlayingDataSource
) : NowPlayingRepository {

    override fun observeNowPlaying(): Flow<NowPlayingInfo> = 
        nowPlayingDataSource.observeNowPlaying()
}
