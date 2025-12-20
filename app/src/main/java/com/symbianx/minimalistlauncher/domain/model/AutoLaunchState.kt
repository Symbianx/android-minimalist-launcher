package com.symbianx.minimalistlauncher.domain.model

/**
 * Represents the state for auto-launch logic in the search UI.
 */
data class AutoLaunchState(
    val isEligible: Boolean = false,
    val debounceTimer: Long = 0L,
    val feedbackGiven: Boolean = false
)

/**
 * User settings related to auto-launch feature.
 */
data class UserSettings(
    val autoLaunchEnabled: Boolean = true
)
