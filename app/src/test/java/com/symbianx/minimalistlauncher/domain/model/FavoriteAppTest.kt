package com.symbianx.minimalistlauncher.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteAppTest {

    @Test
    fun `FavoriteApp creation with valid data succeeds`() {
        val favorite = FavoriteApp(
            packageName = "com.test.app",
            label = "Test App",
            addedTimestamp = System.currentTimeMillis(),
            position = 0
        )
        
        assertEquals("com.test.app", favorite.packageName)
        assertEquals("Test App", favorite.label)
        assertEquals(0, favorite.position)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `FavoriteApp with blank package name throws exception`() {
        FavoriteApp(
            packageName = "",
            label = "Test App",
            addedTimestamp = System.currentTimeMillis(),
            position = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `FavoriteApp with blank label throws exception`() {
        FavoriteApp(
            packageName = "com.test.app",
            label = "",
            addedTimestamp = System.currentTimeMillis(),
            position = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `FavoriteApp with negative timestamp throws exception`() {
        FavoriteApp(
            packageName = "com.test.app",
            label = "Test App",
            addedTimestamp = -1,
            position = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `FavoriteApp with zero timestamp throws exception`() {
        FavoriteApp(
            packageName = "com.test.app",
            label = "Test App",
            addedTimestamp = 0,
            position = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `FavoriteApp with negative position throws exception`() {
        FavoriteApp(
            packageName = "com.test.app",
            label = "Test App",
            addedTimestamp = System.currentTimeMillis(),
            position = -1
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `FavoriteApp with position greater than MAX throws exception`() {
        FavoriteApp(
            packageName = "com.test.app",
            label = "Test App",
            addedTimestamp = System.currentTimeMillis(),
            position = FavoriteApp.MAX_FAVORITES
        )
    }

    @Test
    fun `MAX_FAVORITES constant is 5`() {
        assertEquals(5, FavoriteApp.MAX_FAVORITES)
    }

    @Test
    fun `FavoriteApp with position at max boundary succeeds`() {
        val favorite = FavoriteApp(
            packageName = "com.test.app",
            label = "Test App",
            addedTimestamp = System.currentTimeMillis(),
            position = FavoriteApp.MAX_FAVORITES - 1
        )
        
        assertEquals(4, favorite.position)
    }
}
