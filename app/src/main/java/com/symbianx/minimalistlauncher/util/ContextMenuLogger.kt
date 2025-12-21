package com.symbianx.minimalistlauncher.util

import android.util.Log

/**
 * Utility for logging context menu events for debugging and analytics.
 */
object ContextMenuLogger {
    private const val TAG = "ContextMenu"

    fun logContextMenuOpened(appLabel: String) {
        Log.d(TAG, "Context menu opened for app: $appLabel")
    }

    fun logContextMenuDismissed() {
        Log.d(TAG, "Context menu dismissed")
    }

    fun logAddToFavorites(appLabel: String) {
        Log.d(TAG, "Add to favorites selected for app: $appLabel")
    }

    fun logRemoveFromFavorites(appLabel: String) {
        Log.d(TAG, "Remove from favorites selected for app: $appLabel")
    }

    fun logOpenAppInfo(appLabel: String) {
        Log.d(TAG, "Open app info selected for app: $appLabel")
    }

    fun logContextMenuEvent(event: String) {
        Log.d(TAG, "Context menu event: $event")
    }
}
