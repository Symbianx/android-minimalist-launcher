package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.App
import java.util.Locale

/**
 * Implementation of [SearchAppsUseCase] with case-insensitive partial matching.
 */
class SearchAppsUseCaseImpl : SearchAppsUseCase {
    override fun execute(
        apps: List<App>,
        query: String,
    ): List<App> {
        if (query.isBlank()) {
            return apps.sortedBy { it.label.lowercase(Locale.getDefault()) }
        }

        val normalizedQuery = query.trim().lowercase(Locale.getDefault())

        return apps
            .filter { app ->
                app.label.lowercase(Locale.getDefault()).contains(normalizedQuery)
            }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }
}
