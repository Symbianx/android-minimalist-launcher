package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.local.FavoritesDataSource
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.model.FavoriteApp
import com.symbianx.minimalistlauncher.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Implementation of [FavoritesRepository].
 */
class FavoritesRepositoryImpl(
    private val favoritesDataSource: FavoritesDataSource
) : FavoritesRepository {

    private val _favorites = MutableStateFlow<List<FavoriteApp>>(emptyList())

    init {
        // Load favorites on initialization (should be called from a coroutine)
        // This will be triggered when the repository is created
    }

    /**
     * Initializes the repository by loading favorites from storage.
     * Should be called from a coroutine scope.
     */
    suspend fun initialize() {
        val loadedFavorites = favoritesDataSource.loadFavorites()
        _favorites.value = loadedFavorites
    }

    override fun observeFavorites(): Flow<List<FavoriteApp>> {
        return _favorites.asStateFlow()
    }

    override suspend fun addFavorite(app: App): Boolean {
        val currentFavorites = _favorites.value

        // Check if already exists
        if (currentFavorites.any { it.packageName == app.packageName }) {
            return false
        }

        // Check limit
        if (currentFavorites.size >= FavoriteApp.MAX_FAVORITES) {
            return false
        }

        // Create new favorite with next available position
        val newFavorite = FavoriteApp(
            packageName = app.packageName,
            label = app.label,
            addedTimestamp = System.currentTimeMillis(),
            position = currentFavorites.size
        )

        // Update list
        val updatedFavorites = currentFavorites + newFavorite
        _favorites.value = updatedFavorites
        favoritesDataSource.saveFavorites(updatedFavorites)

        return true
    }

    override suspend fun removeFavorite(packageName: String) {
        val currentFavorites = _favorites.value
        val updatedFavorites = currentFavorites.filter { it.packageName != packageName }

        // Recompute positions to maintain contiguous 0..N range
        val reindexed = updatedFavorites.mapIndexed { index, favorite ->
            favorite.copy(position = index)
        }

        _favorites.value = reindexed
        favoritesDataSource.saveFavorites(reindexed)
    }

    override suspend fun validateFavorites(installedApps: List<App>) {
        val installedPackages = installedApps.map { it.packageName }.toSet()
        val currentFavorites = _favorites.value

        // Remove favorites for uninstalled apps
        val validFavorites = currentFavorites.filter { it.packageName in installedPackages }

        // Recompute positions if any were removed
        if (validFavorites.size != currentFavorites.size) {
            val reindexed = validFavorites.mapIndexed { index, favorite ->
                favorite.copy(position = index)
            }
            _favorites.value = reindexed
            favoritesDataSource.saveFavorites(reindexed)
        }
    }

    override suspend fun getFavoriteCount(): Int {
        return _favorites.value.size
    }

    override suspend fun isFavorite(packageName: String): Boolean {
        return _favorites.value.any { it.packageName == packageName }
    }
}
