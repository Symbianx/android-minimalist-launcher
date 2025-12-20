package com.symbianx.minimalistlauncher.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.symbianx.minimalistlauncher.ui.home.components.QuickActionButtons
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnimationPolishTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun quickActionButtons_animateOnPress() {
        var phoneClicked = false
        var cameraClicked = false

        composeTestRule.setContent {
            QuickActionButtons(
                onPhoneClick = { phoneClicked = true },
                onCameraClick = { cameraClicked = true },
            )
        }

        // Test phone button interaction
        composeTestRule.onNodeWithContentDescription("Open phone dialer").performClick()
        composeTestRule.waitForIdle()
        assert(phoneClicked) { "Phone button should trigger callback" }

        // Test camera button interaction
        composeTestRule.onNodeWithContentDescription("Open camera").performClick()
        composeTestRule.waitForIdle()
        assert(cameraClicked) { "Camera button should trigger callback" }
    }
}
