package com.symbianx.minimalistlauncher.util

import android.util.Log

/**
 * Utility for logging navigation and quick access events for debugging and analytics.
 */
object NavigationLogger {
    private const val TAG = "Navigation"

    fun logSwipeBack() {
        Log.d(TAG, "Swipe back gesture detected: returning to home")
    }

    fun logClockQuickAccess() {
        Log.d(TAG, "Clock quick access: launching clock/alarm app")
    }

    fun logNavigationEvent(event: String) {
        Log.d(TAG, "Navigation event: $event")
    }
}
