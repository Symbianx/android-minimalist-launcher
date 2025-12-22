package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.model.FavoriteApp
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing favorite apps with business logic.
 */
interface ManageFavoritesUseCase {
    /**
     * Observes the list of favorite apps.
     *
     * @return Flow emitting the current list of favorites
     */
    fun observeFavorites(): Flow<List<FavoriteApp>>

    /**
     * Adds an app to favorites with validation.
     *
     * @param app The app to add to favorites
     * @return Result indicating success or failure reason
     */
    suspend fun addToFavorites(app: App): AddFavoriteResult

    /**
     * Removes an app from favorites.
     *
     * @param packageName The package name of the app to remove
     */
    suspend fun removeFromFavorites(packageName: String)

    /**
     * Checks if a new favorite can be added.
     *
     * @return True if under the 5-favorite limit
     */
    suspend fun canAddFavorite(): Boolean

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

    /**
     * Result of adding a favorite app.
     */
    sealed class AddFavoriteResult {
        /** Successfully added to favorites */
        object Success : AddFavoriteResult()

        /** Already exists in favorites */
        object AlreadyExists : AddFavoriteResult()

        /** Limit of 5 favorites reached */
        object LimitReached : AddFavoriteResult()

        /** Generic failure */
        data class Error(
            val message: String,
        ) : AddFavoriteResult()
    }
}
