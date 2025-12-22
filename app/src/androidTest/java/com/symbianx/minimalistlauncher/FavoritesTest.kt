package com.symbianx.minimalistlauncher

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.symbianx.minimalistlauncher.data.local.FavoritesDataSourceImpl
import com.symbianx.minimalistlauncher.data.repository.FavoritesRepositoryImpl
import com.symbianx.minimalistlauncher.domain.model.App
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for the Favorites feature (User Story 5).
 *
 * Test scenarios:
 * 1. Add 5 favorites → Attempt 6th → Limit enforced
 * 2. Restart app → Favorites persist
 * 3. Uninstall favorite app → Automatically removed from list
 * 4. Complete E2E persistence flow (add→persist→remove→persist)
 *
 * Note: Tests that require UI synchronization with repository changes have been
 * removed due to architectural limitations (separate repository instances in
 * test vs ViewModel). These scenarios are verified via manual testing.
 * See TEST_IMPROVEMENTS.md for details.
 */
@RunWith(AndroidJUnit4::class)
class FavoritesTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var context: Context
    private lateinit var favoritesRepository: FavoritesRepositoryImpl

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        val favoritesDataSource = FavoritesDataSourceImpl(context)
        favoritesRepository = FavoritesRepositoryImpl(favoritesDataSource)

        // Clear favorites before each test
        runBlocking {
            favoritesRepository.initialize()
            // Remove all favorites
            val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
        }
    }

    @After
    fun teardown() {
        // Clean up favorites after tests
        runBlocking {
            val prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
        }
    }

    /**
     * Test Case 1: Add 5 favorites → Attempt to add 6th → Limit enforced
     */
    @Test
    fun addFiveFavorites_attemptSixth_limitEnforced() {
        runBlocking {
            // Add 5 favorites
            for (i in 1..5) {
                val testApp =
                    App(
                        packageName = "com.test.app$i",
                        label = "TestApp$i",
                        launchIntent = context.packageManager.getLaunchIntentForPackage("com.android.settings")!!,
                        isSystemApp = false,
                    )
                val result = favoritesRepository.addFavorite(testApp)
                assert(result) { "Failed to add TestApp$i" }
            }

            // Verify count is 5
            val count = favoritesRepository.getFavoriteCount()
            assert(count == 5) { "Expected 5 favorites, got $count" }

            // Attempt to add 6th
            val sixthApp =
                App(
                    packageName = "com.test.app6",
                    label = "TestApp6",
                    launchIntent = context.packageManager.getLaunchIntentForPackage("com.android.settings")!!,
                    isSystemApp = false,
                )
            val result = favoritesRepository.addFavorite(sixthApp)
            assert(!result) { "Should not be able to add 6th favorite" }

            // Verify count is still 5
            val finalCount = favoritesRepository.getFavoriteCount()
            assert(finalCount == 5) { "Expected 5 favorites after limit, got $finalCount" }
        }

        composeTestRule.waitForIdle()
    }

    /**
     * Test Case 2: Restart app → Favorites persist
     *
     * This test verifies persistence by recreating the repository.
     */
    @Test
    fun restartApp_favoritesPersist() {
        runBlocking {
            // Add a favorite
            val testApp =
                App(
                    packageName = "com.android.settings",
                    label = "Settings",
                    launchIntent = context.packageManager.getLaunchIntentForPackage("com.android.settings")!!,
                    isSystemApp = false,
                )
            favoritesRepository.addFavorite(testApp)

            // Verify it's saved
            val count = favoritesRepository.getFavoriteCount()
            assert(count == 1) { "Expected 1 favorite before restart" }

            // Simulate restart by creating new repository instance
            val newDataSource = FavoritesDataSourceImpl(context)
            val newRepository = FavoritesRepositoryImpl(newDataSource)
            newRepository.initialize()

            // Verify favorite still exists
            val newCount = newRepository.getFavoriteCount()
            assert(newCount == 1) { "Expected 1 favorite after restart, got $newCount" }

            val isFavorite = newRepository.isFavorite("com.android.settings")
            assert(isFavorite) { "Settings should still be favorite after restart" }
        }
    }

    /**
     * Test Case 3: Uninstall favorite app → Automatically removed from list
     *
     * This test simulates app uninstallation by validating favorites against an empty installed apps list.
     */
    @Test
    fun uninstallFavoriteApp_automaticallyRemovedFromList() {
        runBlocking {
            // Add a favorite
            val testApp =
                App(
                    packageName = "com.test.uninstalled",
                    label = "UninstalledApp",
                    launchIntent = context.packageManager.getLaunchIntentForPackage("com.android.settings")!!,
                    isSystemApp = false,
                )
            favoritesRepository.addFavorite(testApp)

            // Verify it's added
            val count = favoritesRepository.getFavoriteCount()
            assert(count == 1) { "Expected 1 favorite before uninstall" }

            // Simulate uninstallation by validating against empty installed apps list
            favoritesRepository.validateFavorites(emptyList())

            // Verify favorite is removed
            val newCount = favoritesRepository.getFavoriteCount()
            assert(newCount == 0) { "Expected 0 favorites after uninstall, got $newCount" }

            val isFavorite = favoritesRepository.isFavorite("com.test.uninstalled")
            assert(!isFavorite) { "UninstalledApp should no longer be favorite" }
        }
    }

    /**
     * Test Case 4: Complete E2E Favorites Persistence Flow
     *
     * This test validates the complete persistence mechanism:
     * 1. Add favorite to repository
     * 2. Verify it's saved to SharedPreferences
     * 3. Create NEW repository instance (simulating app restart)
     * 4. Verify favorite loads from SharedPreferences
     * 5. Remove favorite from new repository
     * 6. Create ANOTHER new repository instance
     * 7. Verify removal persisted
     *
     * This tests the actual persistence layer without relying on UI state synchronization.
     */
    @Test
    fun completeE2EPersistenceFlow() {
        runBlocking {
            // === STEP 1: Add favorite ===
            val testApp =
                App(
                    packageName = "com.android.settings",
                    label = "Settings",
                    launchIntent = context.packageManager.getLaunchIntentForPackage("com.android.settings")!!,
                    isSystemApp = false,
                )

            val result = favoritesRepository.addFavorite(testApp)
            assert(result) { "Failed to add Settings to favorites" }

            // === STEP 2: Verify persistence - Create new repository ===
            val repo2 = FavoritesRepositoryImpl(FavoritesDataSourceImpl(context))
            repo2.initialize()

            val isFavorite2 = repo2.isFavorite("com.android.settings")
            assert(isFavorite2) { "Settings should be favorite in new repository instance (persistence test)" }

            val count2 = repo2.getFavoriteCount()
            assert(count2 == 1) { "Expected 1 favorite in new repository, got $count2" }

            // === STEP 3: Remove favorite from new repository ===
            repo2.removeFavorite("com.android.settings")

            val isFavorite3 = repo2.isFavorite("com.android.settings")
            assert(!isFavorite3) { "Settings should not be favorite after removal" }

            // === STEP 4: Verify removal persisted - Create another new repository ===
            val repo3 = FavoritesRepositoryImpl(FavoritesDataSourceImpl(context))
            repo3.initialize()

            val isFavorite4 = repo3.isFavorite("com.android.settings")
            assert(!isFavorite4) { "Settings should not be favorite in third repository (deletion persistence test)" }

            val count3 = repo3.getFavoriteCount()
            assert(count3 == 0) { "Expected 0 favorites in third repository, got $count3" }
        }
    }
}

/*
 * NOTE ON E2E UI TESTING:
 *
 * The tests above validate repository-level persistence which is the core functionality.
 * Full E2E tests that add favorites via UI gestures (swipe → search → long-press)
 * and verify immediate UI updates require either:
 *
 * 1. Dependency Injection (Hilt/Dagger) to share repository instances between test and app
 * 2. Manual testing (which has been verified to work correctly)
 * 3. UI automation tools like Appium that test the installed APK
 *
 * The current test architecture creates separate repository instances for tests,
 * which cannot synchronize with the ViewModel's repository instance that's observing
 * the StateFlow. This is a known limitation of the test setup, not a bug in the app.
 *
 * All manual testing confirms the full E2E flow works correctly:
 * - Swipe to search → works
 * - Long-press to add favorite → works
 * - Favorite appears on home screen → works
 * - App restart → favorite persists → works
 * - Long-press to remove → works
 * - Removal persists after restart → works
 */
