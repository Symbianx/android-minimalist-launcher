package com.symbianx.minimalistlauncher.data.system

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.symbianx.minimalistlauncher.domain.model.NowPlayingInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementation of [NowPlayingDataSource] using Pixel's Ambient Music Detection.
 * 
 * This implementation accesses the actual Pixel Now Playing feature which uses
 * ambient music recognition to detect songs playing in the environment.
 * 
 * Content URI: content://com.google.android.as/ambient_music_tracks
 * 
 * This will only work on Pixel devices with "Now Playing" feature enabled.
 * On non-Pixel devices or when the feature is disabled, it will gracefully
 * return unavailable state.
 */
class NowPlayingDataSourceImpl(
    private val context: Context
) : NowPlayingDataSource {

    companion object {
        // Pixel's Ambient Music Detection content provider URI
        private val AMBIENT_MUSIC_URI = Uri.parse("content://com.google.android.as/ambient_music_tracks")
        
        // Column names for the ambient music content provider
        private const val COLUMN_TRACK_NAME = "track_name"
        private const val COLUMN_ARTIST = "artist"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun observeNowPlaying(): Flow<NowPlayingInfo> = callbackFlow {
        fun getCurrentNowPlaying(): NowPlayingInfo {
            try {
                // Check if the Pixel Now Playing content provider is available
                val cursor = context.contentResolver.query(
                    AMBIENT_MUSIC_URI,
                    arrayOf(COLUMN_TRACK_NAME, COLUMN_ARTIST, COLUMN_TIMESTAMP),
                    null,
                    null,
                    "$COLUMN_TIMESTAMP DESC"
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        val trackNameIndex = it.getColumnIndex(COLUMN_TRACK_NAME)
                        val artistIndex = it.getColumnIndex(COLUMN_ARTIST)
                        val timestampIndex = it.getColumnIndex(COLUMN_TIMESTAMP)
                        
                        val trackName = if (trackNameIndex >= 0) {
                            it.getString(trackNameIndex)
                        } else null
                        
                        val artist = if (artistIndex >= 0) {
                            it.getString(artistIndex)
                        } else null
                        
                        val timestamp = if (timestampIndex >= 0) {
                            it.getLong(timestampIndex)
                        } else System.currentTimeMillis()
                        
                        // Only return data if we have at least a track name
                        return if (!trackName.isNullOrBlank()) {
                            NowPlayingInfo(
                                songName = trackName,
                                artistName = artist,
                                timestamp = timestamp,
                                isAvailable = true
                            )
                        } else {
                            // Empty result - no music detected
                            NowPlayingInfo(
                                songName = null,
                                artistName = null,
                                timestamp = System.currentTimeMillis(),
                                isAvailable = true
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                // Permission denied - feature not available or disabled
                return NowPlayingInfo(isAvailable = false)
            } catch (e: IllegalArgumentException) {
                // Content provider not found - not a Pixel device or feature disabled
                return NowPlayingInfo(isAvailable = false)
            } catch (e: Exception) {
                // Other errors - treat as unavailable
                return NowPlayingInfo(isAvailable = false)
            }

            // No data found - feature available but no music detected
            return NowPlayingInfo(
                songName = null,
                artistName = null,
                timestamp = System.currentTimeMillis(),
                isAvailable = true
            )
        }

        // Send initial state
        trySend(getCurrentNowPlaying())

        // Observe changes to ambient music detection
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(getCurrentNowPlaying())
            }
        }

        try {
            context.contentResolver.registerContentObserver(
                AMBIENT_MUSIC_URI,
                true,
                observer
            )
        } catch (e: Exception) {
            // If we can't register the observer, send unavailable state
            trySend(NowPlayingInfo(isAvailable = false))
        }

        awaitClose {
            try {
                context.contentResolver.unregisterContentObserver(observer)
            } catch (e: Exception) {
                // Ignore unregister errors
            }
        }
    }
}
