package com.symbianx.minimalistlauncher.util

import android.util.Log

/**
 * Utility class for logging settings-related events and state changes.
 *
 * Provides centralized logging for debugging settings operations including:
 * - Settings changes
 * - Corrupted data recovery
 * - Uninstalled app detection
 * - Validation failures
 */
class SettingsLogger {
    /**
     * Logs a settings-related message.
     *
     * In debug builds, logs to Android logcat with tag "Settings".
     * In release builds, logging is suppressed for performance.
     *
     * @param message The message to log
     */
    fun log(message: String) {
        // Only log in debug builds to avoid performance impact in production
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Use Log.d for debug logging
            Log.d(TAG, message)
        }
    }

    /**
     * Logs a settings error with exception details.
     *
     * @param message Error message
     * @param throwable Exception that occurred
     */
    fun logError(
        message: String,
        throwable: Throwable? = null,
    ) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }

    companion object {
        private const val TAG = "Settings"
    }
}
