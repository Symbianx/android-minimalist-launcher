package com.symbianx.minimalistlauncher.data.system

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.symbianx.minimalistlauncher.domain.model.NowPlayingInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementation of [NowPlayingDataSource] using MediaStore ContentProvider.
 * 
 * Note: This is a simplified implementation. The actual Pixel Now Playing data
 * may require accessing a proprietary ContentProvider that's not publicly documented.
 * This implementation will work as a placeholder and can be enhanced with
 * Pixel-specific APIs when available.
 */
class NowPlayingDataSourceImpl(
    private val context: Context
) : NowPlayingDataSource {

    override fun observeNowPlaying(): Flow<NowPlayingInfo> = callbackFlow {
        fun getCurrentNowPlaying(): NowPlayingInfo {
            // Attempt to query MediaStore for currently playing media
            // Note: This may not capture ambient recognition on Pixel devices
            // A more sophisticated implementation would access the
            // com.google.intelligence.sense content provider if available
            
            try {
                val cursor = context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST
                    ),
                    null,
                    null,
                    "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
                )

                cursor?.use {
                    if (it.moveToFirst()) {
                        val titleIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                        val artistIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                        
                        val title = if (titleIndex >= 0) it.getString(titleIndex) else null
                        val artist = if (artistIndex >= 0) it.getString(artistIndex) else null
                        
                        return NowPlayingInfo(
                            songName = title,
                            artistName = artist,
                            timestamp = System.currentTimeMillis(),
                            isAvailable = true
                        )
                    }
                }
            } catch (e: Exception) {
                // If we can't access the data, return unavailable
                return NowPlayingInfo(isAvailable = false)
            }

            // No music detected
            return NowPlayingInfo(
                songName = null,
                artistName = null,
                timestamp = System.currentTimeMillis(),
                isAvailable = true
            )
        }

        // Send initial state
        trySend(getCurrentNowPlaying())

        // Observe changes to media store
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                trySend(getCurrentNowPlaying())
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )

        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }
}
