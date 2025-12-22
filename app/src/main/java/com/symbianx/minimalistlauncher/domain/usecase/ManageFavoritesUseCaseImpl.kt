package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.model.FavoriteApp
import com.symbianx.minimalistlauncher.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of [ManageFavoritesUseCase].
 */
class ManageFavoritesUseCaseImpl(
    private val favoritesRepository: FavoritesRepository,
) : ManageFavoritesUseCase {
    override fun observeFavorites(): Flow<List<FavoriteApp>> = favoritesRepository.observeFavorites()

    override suspend fun addToFavorites(app: App): ManageFavoritesUseCase.AddFavoriteResult {
        // Check if already a favorite
        if (favoritesRepository.isFavorite(app.packageName)) {
            return ManageFavoritesUseCase.AddFavoriteResult.AlreadyExists
        }

        // Check if limit reached
        if (!canAddFavorite()) {
            return ManageFavoritesUseCase.AddFavoriteResult.LimitReached
        }

        // Attempt to add
        return try {
            val success = favoritesRepository.addFavorite(app)
            if (success) {
                ManageFavoritesUseCase.AddFavoriteResult.Success
            } else {
                ManageFavoritesUseCase.AddFavoriteResult.Error("Failed to add favorite")
            }
        } catch (e: Exception) {
            ManageFavoritesUseCase.AddFavoriteResult.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun removeFromFavorites(packageName: String) {
        favoritesRepository.removeFavorite(packageName)
    }

    override suspend fun canAddFavorite(): Boolean = favoritesRepository.getFavoriteCount() < FavoriteApp.MAX_FAVORITES

    override suspend fun getFavoriteCount(): Int = favoritesRepository.getFavoriteCount()

    override suspend fun isFavorite(packageName: String): Boolean = favoritesRepository.isFavorite(packageName)
}
