package com.symbianx.minimalistlauncher.domain.model

/**
 * Defines when the battery indicator should be visible on the home screen.
 */
enum class BatteryThresholdMode {
    /**
     * Indicator always visible regardless of battery level.
     */
    ALWAYS,

    /**
     * Indicator visible when battery < 50%.
     */
    BELOW_50,

    /**
     * Indicator visible when battery < 20% (low battery warning).
     */
    BELOW_20,

    /**
     * Indicator never displayed.
     */
    NEVER,
    ;

    /**
     * Determines if the battery indicator should be shown based on current battery percentage.
     *
     * @param batteryPercent Current battery percentage (0-100)
     * @return true if indicator should be visible, false otherwise
     */
    fun shouldShow(batteryPercent: Int): Boolean =
        when (this) {
            ALWAYS -> true
            BELOW_50 -> batteryPercent < 50
            BELOW_20 -> batteryPercent < 20
            NEVER -> false
        }
}
