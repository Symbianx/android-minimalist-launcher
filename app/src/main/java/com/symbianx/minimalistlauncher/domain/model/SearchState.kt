package com.symbianx.minimalistlauncher.domain.model

import androidx.compose.runtime.Immutable

/**
 * Represents the current state of the app search functionality.
 *
 * @property isActive Whether search mode is currently active
 * @property query Current search query text
 * @property results List of apps matching the search query
 */
@Immutable
data class SearchState(
    val isActive: Boolean = false,
    val query: String = "",
    val results: List<App> = emptyList(),
)
