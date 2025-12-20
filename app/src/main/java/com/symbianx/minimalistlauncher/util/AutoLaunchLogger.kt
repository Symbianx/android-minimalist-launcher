package com.symbianx.minimalistlauncher.util

import android.util.Log

/**
 * Utility for logging auto-launch events for debugging and analytics.
 */
object AutoLaunchLogger {
    private const val TAG = "AutoLaunch"

    fun logEvent(event: String) {
        Log.d(TAG, event)
    }
}
