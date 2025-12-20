package com.symbianx.minimalistlauncher.domain.usecase

import android.content.Intent
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.model.FavoriteApp
import com.symbianx.minimalistlauncher.domain.repository.FavoritesRepository
import com.symbianx.minimalistlauncher.domain.usecase.ManageFavoritesUseCase.AddFavoriteResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ManageFavoritesUseCaseTest {

    private lateinit var useCase: ManageFavoritesUseCase
    private lateinit var mockRepository: MockFavoritesRepository
    private lateinit var testApp: App

    @Before
    fun setup() {
        mockRepository = MockFavoritesRepository()
        useCase = ManageFavoritesUseCaseImpl(mockRepository)
        testApp = App("com.test.app", "Test App", Intent(), false)
    }

    @Test
    fun `addToFavorites returns Success when under limit`() = runTest {
        val result = useCase.addToFavorites(testApp)
        assertEquals(AddFavoriteResult.Success, result)
        assertEquals(1, mockRepository.getFavoriteCount())
    }

    @Test
    fun `addToFavorites returns AlreadyExists when app is already favorite`() = runTest {
        useCase.addToFavorites(testApp)
        val result = useCase.addToFavorites(testApp)
        assertEquals(AddFavoriteResult.AlreadyExists, result)
        assertEquals(1, mockRepository.getFavoriteCount())
    }

    @Test
    fun `addToFavorites returns LimitReached when at max favorites`() = runTest {
        // Add 5 favorites
        repeat(5) { index ->
            val app = App("com.test.app$index", "App $index", Intent(), false)
            useCase.addToFavorites(app)
        }
        
        // Try to add 6th
        val newApp = App("com.test.app6", "App 6", Intent(), false)
        val result = useCase.addToFavorites(newApp)
        
        assertEquals(AddFavoriteResult.LimitReached, result)
        assertEquals(5, mockRepository.getFavoriteCount())
    }

    @Test
    fun `removeFromFavorites removes app successfully`() = runTest {
        useCase.addToFavorites(testApp)
        assertEquals(1, mockRepository.getFavoriteCount())
        
        useCase.removeFromFavorites(testApp.packageName)
        assertEquals(0, mockRepository.getFavoriteCount())
    }

    @Test
    fun `canAddFavorite returns true when under limit`() = runTest {
        assertTrue(useCase.canAddFavorite())
    }

    @Test
    fun `canAddFavorite returns false when at limit`() = runTest {
        // Add 5 favorites
        repeat(5) { index ->
            val app = App("com.test.app$index", "App $index", Intent(), false)
            useCase.addToFavorites(app)
        }
        
        assertFalse(useCase.canAddFavorite())
    }

    @Test
    fun `getFavoriteCount returns correct count`() = runTest {
        assertEquals(0, useCase.getFavoriteCount())
        
        useCase.addToFavorites(testApp)
        assertEquals(1, useCase.getFavoriteCount())
        
        val app2 = App("com.test.app2", "App 2", Intent(), false)
        useCase.addToFavorites(app2)
        assertEquals(2, useCase.getFavoriteCount())
    }

    @Test
    fun `isFavorite returns true for favorite app`() = runTest {
        useCase.addToFavorites(testApp)
        assertTrue(useCase.isFavorite(testApp.packageName))
    }

    @Test
    fun `isFavorite returns false for non-favorite app`() = runTest {
        assertFalse(useCase.isFavorite(testApp.packageName))
    }

    @Test
    fun `observeFavorites emits updates when favorites change`() = runTest {
        val favorites = useCase.observeFavorites()
        
        // Initial state should be empty
        assertEquals(0, favorites.first().size)
        
        // Add favorite
        useCase.addToFavorites(testApp)
        
        // Should now have 1 favorite
        val updatedFavorites = mockRepository.observeFavorites().first()
        assertEquals(1, updatedFavorites.size)
        assertEquals(testApp.packageName, updatedFavorites[0].packageName)
    }

    // Mock repository for testing
    private class MockFavoritesRepository : FavoritesRepository {
        private val favorites = mutableListOf<FavoriteApp>()
        private val favoritesFlow = MutableStateFlow<List<FavoriteApp>>(emptyList())

        override fun observeFavorites(): Flow<List<FavoriteApp>> = favoritesFlow

        override suspend fun addFavorite(app: App): Boolean {
            if (favorites.size >= FavoriteApp.MAX_FAVORITES) return false
            if (favorites.any { it.packageName == app.packageName }) return false
            
            val favorite = FavoriteApp(
                packageName = app.packageName,
                label = app.label,
                addedTimestamp = System.currentTimeMillis(),
                position = favorites.size
            )
            favorites.add(favorite)
            favoritesFlow.value = favorites.toList()
            return true
        }

        override suspend fun removeFavorite(packageName: String) {
            favorites.removeIf { it.packageName == packageName }
            favoritesFlow.value = favorites.toList()
        }

        override suspend fun validateFavorites(installedApps: List<App>) {
            val installedPackages = installedApps.map { it.packageName }.toSet()
            favorites.removeIf { it.packageName !in installedPackages }
            favoritesFlow.value = favorites.toList()
        }

        override suspend fun getFavoriteCount(): Int = favorites.size

        override suspend fun isFavorite(packageName: String): Boolean =
            favorites.any { it.packageName == packageName }
    }
}
