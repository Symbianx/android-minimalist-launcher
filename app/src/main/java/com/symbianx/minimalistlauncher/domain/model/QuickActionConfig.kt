package com.symbianx.minimalistlauncher.domain.model

/**
 * Configuration for a single quick action button (left or right).
 *
 * @property packageName Android package name of the target app
 * @property label Display name of the app (for UI preview)
 * @property isDefault Whether this is the default app (not user-customized)
 */
data class QuickActionConfig(
    val packageName: String,
    val label: String,
    val isDefault: Boolean = true,
) {
    companion object {
        /**
         * Default configuration for left quick action button (Phone dialer).
         * Falls back to known package names if device default is unavailable.
         */
        fun defaultLeft() =
            QuickActionConfig(
                packageName = "com.google.android.dialer",
                label = "Phone",
                isDefault = true,
            )

        /**
         * Default configuration for right quick action button (Camera).
         * Falls back to known package names if device default is unavailable.
         */
        fun defaultRight() =
            QuickActionConfig(
                packageName = "com.google.android.GoogleCamera",
                label = "Camera",
                isDefault = true,
            )
    }
}
