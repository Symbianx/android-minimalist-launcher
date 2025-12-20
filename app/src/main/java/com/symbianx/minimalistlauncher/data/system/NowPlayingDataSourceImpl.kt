package com.symbianx.minimalistlauncher.data.system

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
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
        private const val TAG = "NowPlayingDataSource"
        
        // Pixel's Ambient Music Detection content provider URI
        private val AMBIENT_MUSIC_URI = Uri.parse("content://com.google.android.as/ambient_music_tracks")
        
        // Column names for the ambient music content provider
        private const val COLUMN_TRACK_NAME = "track_name"
        private const val COLUMN_ARTIST = "artist"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun observeNowPlaying(): Flow<NowPlayingInfo> = callbackFlow {
        Log.d(TAG, "observeNowPlaying: Starting Now Playing observation")
        
        fun getCurrentNowPlaying(): NowPlayingInfo {
            Log.d(TAG, "getCurrentNowPlaying: Querying ambient music content provider")
            Log.d(TAG, "getCurrentNowPlaying: URI = $AMBIENT_MUSIC_URI")
            
            try {
                // Check if the Pixel Now Playing content provider is available
                val cursor = context.contentResolver.query(
                    AMBIENT_MUSIC_URI,
                    arrayOf(COLUMN_TRACK_NAME, COLUMN_ARTIST, COLUMN_TIMESTAMP),
                    null,
                    null,
                    "$COLUMN_TIMESTAMP DESC"
                )
                
                Log.d(TAG, "getCurrentNowPlaying: Query executed, cursor = $cursor")

                cursor?.use {
                    Log.d(TAG, "getCurrentNowPlaying: Cursor count = ${it.count}")
                    
                    if (it.count > 0) {
                        Log.d(TAG, "getCurrentNowPlaying: Available columns = ${it.columnNames.joinToString()}")
                    }
                    
                    if (it.moveToFirst()) {
                        Log.d(TAG, "getCurrentNowPlaying: Moved to first row")
                        
                        val trackNameIndex = it.getColumnIndex(COLUMN_TRACK_NAME)
                        val artistIndex = it.getColumnIndex(COLUMN_ARTIST)
                        val timestampIndex = it.getColumnIndex(COLUMN_TIMESTAMP)
                        
                        Log.d(TAG, "getCurrentNowPlaying: Column indices - track_name: $trackNameIndex, artist: $artistIndex, timestamp: $timestampIndex")
                        
                        val trackName = if (trackNameIndex >= 0) {
                            it.getString(trackNameIndex)
                        } else null
                        
                        val artist = if (artistIndex >= 0) {
                            it.getString(artistIndex)
                        } else null
                        
                        val timestamp = if (timestampIndex >= 0) {
                            it.getLong(timestampIndex)
                        } else System.currentTimeMillis()
                        
                        Log.d(TAG, "getCurrentNowPlaying: Retrieved data - trackName: '$trackName', artist: '$artist', timestamp: $timestamp")
                        
                        // Only return data if we have at least a track name
                        return if (!trackName.isNullOrBlank()) {
                            Log.i(TAG, "getCurrentNowPlaying: ✓ Found track: '$trackName' by '$artist'")
                            NowPlayingInfo(
                                songName = trackName,
                                artistName = artist,
                                timestamp = timestamp,
                                isAvailable = true
                            )
                        } else {
                            // Empty result - no music detected
                            Log.d(TAG, "getCurrentNowPlaying: Track name is empty, no music detected")
                            NowPlayingInfo(
                                songName = null,
                                artistName = null,
                                timestamp = System.currentTimeMillis(),
                                isAvailable = true
                            )
                        }
                    } else {
                        Log.d(TAG, "getCurrentNowPlaying: Cursor is empty (no rows)")
                    }
                } ?: run {
                    Log.w(TAG, "getCurrentNowPlaying: Cursor is null")
                }
            } catch (e: SecurityException) {
                // Permission denied - feature not available or disabled
                Log.e(TAG, "getCurrentNowPlaying: SecurityException - Permission denied", e)
                return NowPlayingInfo(isAvailable = false)
            } catch (e: IllegalArgumentException) {
                // Content provider not found - not a Pixel device or feature disabled
                Log.e(TAG, "getCurrentNowPlaying: IllegalArgumentException - Content provider not found", e)
                return NowPlayingInfo(isAvailable = false)
            } catch (e: Exception) {
                // Other errors - treat as unavailable
                Log.e(TAG, "getCurrentNowPlaying: Unexpected exception", e)
                return NowPlayingInfo(isAvailable = false)
            }

            // No data found - feature available but no music detected
            Log.d(TAG, "getCurrentNowPlaying: No data found, returning available=true with null data")
            return NowPlayingInfo(
                songName = null,
                artistName = null,
                timestamp = System.currentTimeMillis(),
                isAvailable = true
            )
        }

        // Send initial state
        Log.d(TAG, "observeNowPlaying: Sending initial state")
        val initialState = getCurrentNowPlaying()
        Log.d(TAG, "observeNowPlaying: Initial state - isAvailable: ${initialState.isAvailable}, songName: '${initialState.songName}'")
        trySend(initialState)

        // Observe changes to ambient music detection
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                Log.d(TAG, "ContentObserver.onChange: Detected change, fetching new data")
                val newState = getCurrentNowPlaying()
                Log.d(TAG, "ContentObserver.onChange: New state - isAvailable: ${newState.isAvailable}, songName: '${newState.songName}'")
                trySend(newState)
            }
        }

        try {
            Log.d(TAG, "observeNowPlaying: Registering ContentObserver for $AMBIENT_MUSIC_URI")
            context.contentResolver.registerContentObserver(
                AMBIENT_MUSIC_URI,
                true,
                observer
            )
            Log.i(TAG, "observeNowPlaying: ✓ ContentObserver registered successfully")
        } catch (e: Exception) {
            // If we can't register the observer, send unavailable state
            Log.e(TAG, "observeNowPlaying: Failed to register ContentObserver", e)
            trySend(NowPlayingInfo(isAvailable = false))
        }

        awaitClose {
            Log.d(TAG, "observeNowPlaying: Closing flow, unregistering ContentObserver")
            try {
                context.contentResolver.unregisterContentObserver(observer)
                Log.d(TAG, "observeNowPlaying: ContentObserver unregistered")
            } catch (e: Exception) {
                // Ignore unregister errors
                Log.w(TAG, "observeNowPlaying: Error unregistering ContentObserver", e)
            }
        }
    }
}
