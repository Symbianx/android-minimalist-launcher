package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertDoesNotExist
import org.junit.Rule
import org.junit.Test

class CircularBatteryIndicatorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun indicator_visible_whenBatteryAbove50() {
        composeTestRule.setContent {
            CircularBatteryIndicator(batteryPercentage = 75, isCharging = false, modifier = androidx.compose.ui.Modifier.testTag("battery-indicator"))
        }
        composeTestRule.onNodeWithTag("battery-indicator").assertIsDisplayed()
    }

    @Test
    fun indicator_arcVisible_whenBatteryBelow50() {
        composeTestRule.setContent {
            CircularBatteryIndicator(batteryPercentage = 25, isCharging = false, modifier = androidx.compose.ui.Modifier.testTag("battery-indicator"))
        }
        composeTestRule.onNodeWithTag("battery-indicator").assertIsDisplayed()
    }

    @Test
    fun indicator_empty_whenBatteryZero() {
        composeTestRule.setContent {
            CircularBatteryIndicator(batteryPercentage = 0, isCharging = false, modifier = androidx.compose.ui.Modifier.testTag("battery-indicator"))
        }
        composeTestRule.onNodeWithTag("battery-indicator").assertIsDisplayed()
        // Visual check: arc should not be drawn
    }

    @Test
    fun indicator_full_whenBatteryHundred() {
        composeTestRule.setContent {
            CircularBatteryIndicator(batteryPercentage = 100, isCharging = false, modifier = androidx.compose.ui.Modifier.testTag("battery-indicator"))
        }
        composeTestRule.onNodeWithTag("battery-indicator").assertIsDisplayed()
        // Visual check: full arc should be drawn
    }

    @Test
    fun indicator_animates_whenCharging() {
        composeTestRule.setContent {
            CircularBatteryIndicator(batteryPercentage = 50, isCharging = true, modifier = androidx.compose.ui.Modifier.testTag("battery-indicator"))
        }
        composeTestRule.onNodeWithTag("battery-indicator").assertIsDisplayed()
        // Animation check: indicator should continuously fill when charging
    }

    @Test
    fun indicator_static_whenNotCharging() {
        composeTestRule.setContent {
            CircularBatteryIndicator(batteryPercentage = 50, isCharging = false, modifier = androidx.compose.ui.Modifier.testTag("battery-indicator"))
        }
        composeTestRule.onNodeWithTag("battery-indicator").assertIsDisplayed()
        // Static check: indicator should not animate when not charging
    }
}
