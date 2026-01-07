package com.symbianx.minimalistlauncher.domain.model

/**
 * Primary aggregate representing all launcher configuration preferences.
 *
 * @property autoLaunchEnabled Whether single search result auto-launch is active
 * @property leftQuickAction Configuration for bottom-left quick action button
 * @property rightQuickAction Configuration for bottom-right quick action button
 * @property batteryIndicatorMode When to show battery indicator
 * @property lastModified Timestamp of last settings change (for debugging)
 */
data class LauncherSettings(
    val autoLaunchEnabled: Boolean = true,
    val leftQuickAction: QuickActionConfig = QuickActionConfig.defaultLeft(),
    val rightQuickAction: QuickActionConfig = QuickActionConfig.defaultRight(),
    val batteryIndicatorMode: BatteryThresholdMode = BatteryThresholdMode.BELOW_50,
    val lastModified: Long = System.currentTimeMillis(),
) {
    companion object {
        /**
         * Returns default settings with all values initialized to their defaults.
         */
        fun defaults() = LauncherSettings()
    }
}
