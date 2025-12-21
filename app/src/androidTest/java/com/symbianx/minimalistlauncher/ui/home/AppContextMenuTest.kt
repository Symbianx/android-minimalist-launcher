package com.symbianx.minimalistlauncher.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Intent
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.ui.home.components.AppContextMenu
import com.symbianx.minimalistlauncher.ui.home.components.AppListItem
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppContextMenuTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun longPress_onAppItem_opensContextMenu() {
        val app = App(
            packageName = "com.google.android.apps.maps",
            label = "Maps",
            launchIntent = Intent(Intent.ACTION_MAIN).apply { `package` = "com.google.android.apps.maps" },
        )

        composeTestRule.setContent {
            val showMenu = remember { mutableStateOf(false) }
            AppListItem(
                app = app,
                onClick = { },
                onLongClick = { showMenu.value = true },
            )
            if (showMenu.value) {
                AppContextMenu(
                    app = app,
                    isFavorite = false,
                    onDismiss = { showMenu.value = false },
                    onAddToFavorites = { },
                    onRemoveFromFavorites = { },
                    onOpenAppInfo = { },
                )
            }
        }

        // Perform long press on the app item
        composeTestRule.onNodeWithText("Maps").performTouchInput { longClick() }

        // Verify that context menu shows the expected option
        composeTestRule.onNodeWithText("Add to Favorites").assertExists()
    }

    @Test
    fun addToFavorites_viaContextMenu_triggersCallback() {
        val app = App(
            packageName = "com.android.camera",
            label = "Camera",
            launchIntent = Intent(Intent.ACTION_MAIN).apply { `package` = "com.android.camera" },
        )
        var added = false

        composeTestRule.setContent {
            val showMenu = remember { mutableStateOf(true) }
            if (showMenu.value) {
                AppContextMenu(
                    app = app,
                    isFavorite = false,
                    onDismiss = { showMenu.value = false },
                    onAddToFavorites = { added = true },
                    onRemoveFromFavorites = { },
                    onOpenAppInfo = { },
                )
            }
        }

        composeTestRule.onNodeWithText("Add to Favorites").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Expected add to favorites callback to be triggered", added)
    }

    @Test
    fun removeFromFavorites_viaContextMenu_triggersCallback() {
        val app = App(
            packageName = "com.google.android.calendar",
            label = "Calendar",
            launchIntent = Intent(Intent.ACTION_MAIN).apply { `package` = "com.google.android.calendar" },
        )
        var removed = false

        composeTestRule.setContent {
            val showMenu = remember { mutableStateOf(true) }
            if (showMenu.value) {
                AppContextMenu(
                    app = app,
                    isFavorite = true,
                    onDismiss = { showMenu.value = false },
                    onAddToFavorites = { },
                    onRemoveFromFavorites = { removed = true },
                    onOpenAppInfo = { },
                )
            }
        }

        composeTestRule.onNodeWithText("Remove from Favorites").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Expected remove from favorites callback to be triggered", removed)
    }

    @Test
    fun goToAppInfo_viaContextMenu_triggersCallback() {
        val app = App(
            packageName = "com.google.android.apps.maps",
            label = "Maps",
            launchIntent = Intent(Intent.ACTION_MAIN).apply { `package` = "com.google.android.apps.maps" },
        )
        var openedInfo = false

        composeTestRule.setContent {
            AppContextMenu(
                app = app,
                isFavorite = false,
                onDismiss = { },
                onAddToFavorites = { },
                onRemoveFromFavorites = { },
                onOpenAppInfo = { openedInfo = true },
            )
        }

        composeTestRule.onNodeWithText("Go to App Info").performClick()
        composeTestRule.waitForIdle()
        assertTrue("Expected go to app info callback to be triggered", openedInfo)
    }
}
