package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.App

/**
 * Use case for searching and filtering installed applications.
 */
interface SearchAppsUseCase {
    /**
     * Searches apps by query string using case-insensitive partial matching.
     *
     * @param apps List of all available apps
     * @param query Search query
     * @return List of apps matching the query, sorted alphabetically
     */
    fun execute(
        apps: List<App>,
        query: String,
    ): List<App>
}
