package com.symbianx.minimalistlauncher.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Intent
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.model.SearchState
import com.symbianx.minimalistlauncher.ui.home.components.SearchView
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchViewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun autoLaunch_whenSingleResult_and300msPause_triggersLaunch() {
        composeTestRule.mainClock.autoAdvance = false
        var launched = false
        val singleApp = listOf(
            App(
                packageName = "com.google.android.apps.maps",
                label = "Maps",
                launchIntent = Intent(Intent.ACTION_MAIN).apply { `package` = "com.google.android.apps.maps" },
            ),
        )

        composeTestRule.setContent {
            val state = remember { mutableStateOf(SearchState(isActive = true, query = "Maps", results = singleApp)) }
            SearchView(
                searchState = state.value,
                onQueryChange = { q -> state.value = state.value.copy(query = q) },
                onAppClick = { launched = true },
                autoLaunchEnabled = true,
                autoLaunchDelayMs = 300,
            )
        }

        // Advance virtual time by 300ms to trigger debounce
        composeTestRule.mainClock.advanceTimeBy(300)
        composeTestRule.waitForIdle()
        assertTrue("Expected app to be launched after 300ms pause with single result", launched)
    }

    @Test
    fun autoLaunch_doesNotTrigger_whenMultipleResults() {
        composeTestRule.mainClock.autoAdvance = false
        var launched = false
        val apps = listOf(
            App(
                packageName = "com.android.camera",
                label = "Camera",
                launchIntent = Intent(Intent.ACTION_MAIN).apply { `package` = "com.android.camera" },
            ),
            App(
                packageName = "com.google.android.calendar",
                label = "Calendar",
                launchIntent = Intent(Intent.ACTION_MAIN).apply { `package` = "com.google.android.calendar" },
            ),
        )

        composeTestRule.setContent {
            val state = remember { mutableStateOf(SearchState(isActive = true, query = "ca", results = apps)) }
            SearchView(
                searchState = state.value,
                onQueryChange = { q -> state.value = state.value.copy(query = q) },
                onAppClick = { launched = true },
                autoLaunchEnabled = true,
                autoLaunchDelayMs = 300,
            )
        }

        composeTestRule.mainClock.advanceTimeBy(1000)
        composeTestRule.waitForIdle()
        assertFalse("Expected no launch when multiple results present", launched)
    }

    @Test
    fun autoLaunch_cancels_whenQueryChanges_beforeDelay() {
        composeTestRule.mainClock.autoAdvance = false
        var launched = false
        val singleApp = listOf(
            App(
                packageName = "com.google.android.apps.maps",
                label = "Maps",
                launchIntent = Intent(Intent.ACTION_MAIN).apply { `package` = "com.google.android.apps.maps" },
            ),
        )

        lateinit var stateHolder: androidx.compose.runtime.MutableState<SearchState>

        composeTestRule.setContent {
            stateHolder = remember { mutableStateOf(SearchState(isActive = true, query = "Map", results = singleApp)) }
            SearchView(
                searchState = stateHolder.value,
                onQueryChange = { q -> stateHolder.value = stateHolder.value.copy(query = q) },
                onAppClick = { launched = true },
                autoLaunchEnabled = true,
                autoLaunchDelayMs = 300,
            )
        }

        // Advance time partially, then change query to restart debounce
        composeTestRule.mainClock.advanceTimeBy(150)
        composeTestRule.runOnIdle {
            stateHolder.value = stateHolder.value.copy(query = "Maps!")
        }
        // Advance beyond total 300ms; launch should not have happened due to reset
        composeTestRule.mainClock.advanceTimeBy(200)
        composeTestRule.waitForIdle()
        assertFalse("Expected no launch when query changes before 300ms debounce", launched)
    }
}
