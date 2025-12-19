package com.symbianx.minimalistlauncher.domain.repository

import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.model.FavoriteApp
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing favorite apps.
 */
interface FavoritesRepository {
    /**
     * Observes the list of favorite apps.
     *
     * @return Flow emitting the current list of favorites, validated against installed apps
     */
    fun observeFavorites(): Flow<List<FavoriteApp>>

    /**
     * Adds an app to favorites.
     *
     * @param app The app to add to favorites
     * @return True if added successfully, false if limit reached or already exists
     */
    suspend fun addFavorite(app: App): Boolean

    /**
     * Removes an app from favorites by package name.
     *
     * @param packageName The package name of the app to remove
     */
    suspend fun removeFavorite(packageName: String)

    /**
     * Validates favorites against installed apps and removes uninstalled apps.
     *
     * @param installedApps List of currently installed apps
     */
    suspend fun validateFavorites(installedApps: List<App>)

    /**
     * Gets the current count of favorites.
     *
     * @return Number of favorite apps
     */
    suspend fun getFavoriteCount(): Int

    /**
     * Checks if an app is already a favorite.
     *
     * @param packageName The package name to check
     * @return True if the app is a favorite
     */
    suspend fun isFavorite(packageName: String): Boolean
}
