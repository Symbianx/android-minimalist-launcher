package com.symbianx.minimalistlauncher.domain.model

import android.content.Intent
import androidx.compose.runtime.Immutable

/**
 * Represents an installed, launchable application.
 *
 * @property packageName Unique application identifier (e.g., "com.android.chrome")
 * @property label Display name for the app (e.g., "Chrome")
 * @property launchIntent Intent to launch the application
 * @property isSystemApp Whether this is a system app (filtered from search)
 */
@Immutable
data class App(
    val packageName: String,
    val label: String,
    val launchIntent: Intent,
    val isSystemApp: Boolean = false,
)
