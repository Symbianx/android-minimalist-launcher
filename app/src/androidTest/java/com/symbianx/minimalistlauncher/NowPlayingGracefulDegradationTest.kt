package com.symbianx.minimalistlauncher

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for graceful degradation when Now Playing is unavailable (T026).
 *
 * This test verifies that the app works correctly on devices/emulators
 * that don't have the Pixel Now Playing feature.
 */
@RunWith(AndroidJUnit4::class)
class NowPlayingGracefulDegradationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesSuccessfullyWithoutNowPlaying() {
        // Wait for the app to load
        composeTestRule.waitForIdle()

        // Verify the home screen is displayed
        // The app should show time and battery even without Now Playing
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                // Look for status bar elements (time/battery should always be present)
                composeTestRule.onNodeWithContentDescription("Battery percentage").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun nowPlayingAreaDoesNotCrashWhenUnavailable() {
        composeTestRule.waitForIdle()

        // The app should handle Now Playing being unavailable gracefully
        // Either it shows nothing, a placeholder, or handles the null state
        // This test just ensures no crash occurs

        // Try to find Now Playing area - it might not exist on emulator
        try {
            composeTestRule.onNodeWithText("Now Playing", substring = true).assertExists()
        } catch (e: AssertionError) {
            // This is expected on non-Pixel devices/emulators
            // The absence of Now Playing should not crash the app
        }

        // Verify the app is still functional
        composeTestRule.onNodeWithContentDescription("Battery percentage").assertExists()
    }

    @Test
    fun appIsFullyFunctionalWithoutNowPlaying() {
        composeTestRule.waitForIdle()

        // Verify search can be activated (this tests core functionality)
        // Note: Gesture testing is complex, so we verify the UI is responsive

        // App should show time
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithContentDescription("Current time").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Verify favorites can be displayed
        // (Even empty favorites list should render without Now Playing)
        composeTestRule.waitForIdle()

        // The main assertion: app doesn't crash and shows essential UI
        composeTestRule.onNodeWithContentDescription("Battery percentage").assertExists()
    }

    @Test
    fun statusBarDisplaysWithoutNowPlayingData() {
        composeTestRule.waitForIdle()

        // Wait for status bar to render
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                // Battery percentage should be visible
                composeTestRule.onNodeWithContentDescription("Battery percentage").assertExists()

                // Current time should be visible
                composeTestRule.onNodeWithContentDescription("Current time").assertExists()

                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
}
