package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.symbianx.minimalistlauncher.domain.model.DeviceStatus
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatusBarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clockTap_invokesCallback() {
        var clockTapped = false
        val deviceStatus = DeviceStatus(
            currentTime = "10:30",
            currentDate = "Mon, Jan 1",
            batteryPercentage = 85,
            isCharging = false,
        )

        composeTestRule.setContent {
            StatusBar(
                deviceStatus = deviceStatus,
                onClockTap = { clockTapped = true },
            )
        }

        // Tap on the time
        composeTestRule.onNodeWithText("10:30").performClick()
        composeTestRule.waitForIdle()

        assertTrue("Expected clock tap callback to be invoked", clockTapped)
    }

    @Test
    fun dateTap_invokesCallback() {
        var clockTapped = false
        val deviceStatus = DeviceStatus(
            currentTime = "10:30",
            currentDate = "Mon, Jan 1",
            batteryPercentage = 85,
            isCharging = false,
        )

        composeTestRule.setContent {
            StatusBar(
                deviceStatus = deviceStatus,
                onClockTap = { clockTapped = true },
            )
        }

        // Tap on the date
        composeTestRule.onNodeWithText("Mon, Jan 1").performClick()
        composeTestRule.waitForIdle()

        assertTrue("Expected clock tap callback to be invoked when date is tapped", clockTapped)
    }
}
