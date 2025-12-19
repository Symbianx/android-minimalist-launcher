package com.symbianx.minimalistlauncher.data.local

import android.content.Context
import android.content.SharedPreferences
import com.symbianx.minimalistlauncher.domain.model.FavoriteApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of [FavoritesDataSource] using SharedPreferences and JSON serialization.
 */
class FavoritesDataSourceImpl(
    context: Context
) : FavoritesDataSource {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun saveFavorites(favorites: List<FavoriteApp>) = withContext(Dispatchers.IO) {
        val serializableList = favorites.map { it.toSerializable() }
        val jsonString = json.encodeToString(serializableList)
        prefs.edit().putString(KEY_FAVORITES_LIST, jsonString).apply()
    }

    override suspend fun loadFavorites(): List<FavoriteApp> = withContext(Dispatchers.IO) {
        val jsonString = prefs.getString(KEY_FAVORITES_LIST, null) ?: return@withContext emptyList()
        try {
            val serializableList = json.decodeFromString<List<SerializableFavoriteApp>>(jsonString)
            serializableList.map { it.toDomain() }
        } catch (e: Exception) {
            // If deserialization fails, return empty list and clear corrupted data
            clearFavorites()
            emptyList()
        }
    }

    override suspend fun clearFavorites() = withContext(Dispatchers.IO) {
        prefs.edit().remove(KEY_FAVORITES_LIST).apply()
    }

    companion object {
        private const val PREFS_NAME = "favorites"
        private const val KEY_FAVORITES_LIST = "favorites_list"
    }

    /**
     * Serializable version of FavoriteApp for JSON persistence.
     */
    @Serializable
    private data class SerializableFavoriteApp(
        val packageName: String,
        val label: String,
        val addedTimestamp: Long,
        val position: Int
    )

    private fun FavoriteApp.toSerializable() = SerializableFavoriteApp(
        packageName = packageName,
        label = label,
        addedTimestamp = addedTimestamp,
        position = position
    )

    private fun SerializableFavoriteApp.toDomain() = FavoriteApp(
        packageName = packageName,
        label = label,
        addedTimestamp = addedTimestamp,
        position = position
    )
}
