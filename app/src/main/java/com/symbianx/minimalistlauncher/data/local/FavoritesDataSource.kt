package com.symbianx.minimalistlauncher.data.local

import com.symbianx.minimalistlauncher.domain.model.FavoriteApp

/**
 * Data source interface for persisting favorite apps.
 */
interface FavoritesDataSource {
    /**
     * Saves the list of favorites to persistent storage.
     *
     * @param favorites List of favorite apps to save
     */
    suspend fun saveFavorites(favorites: List<FavoriteApp>)

    /**
     * Loads the list of favorites from persistent storage.
     *
     * @return List of favorite apps, empty if none exist
     */
    suspend fun loadFavorites(): List<FavoriteApp>

    /**
     * Clears all favorites from persistent storage.
     */
    suspend fun clearFavorites()
}
