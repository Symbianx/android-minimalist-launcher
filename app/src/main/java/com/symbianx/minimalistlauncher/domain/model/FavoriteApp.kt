package com.symbianx.minimalistlauncher.domain.model

import androidx.compose.runtime.Immutable

/**
 * Represents a user-selected favorite app displayed on the home screen.
 *
 * @property packageName Unique identifier for the app (e.g., "com.android.chrome")
 * @property label Display name of the app (cached for persistence)
 * @property addedTimestamp Unix timestamp in milliseconds when favorite was added
 * @property position Display order (0-4, top to bottom)
 */
@Immutable
data class FavoriteApp(
    val packageName: String,
    val label: String,
    val addedTimestamp: Long,
    val position: Int,
) {
    companion object {
        /**
         * Maximum number of favorites allowed
         */
        const val MAX_FAVORITES = 5
    }

    init {
        require(packageName.isNotBlank()) { "Package name cannot be blank" }
        require(label.isNotBlank()) { "Label cannot be blank" }
        require(addedTimestamp > 0) { "Added timestamp must be positive" }
        require(position in 0 until MAX_FAVORITES) { "Position must be in range 0..${MAX_FAVORITES - 1}" }
    }
}
