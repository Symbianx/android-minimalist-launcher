package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.symbianx.minimalistlauncher.domain.model.BatteryThresholdMode
import com.symbianx.minimalistlauncher.domain.model.DeviceStatus

/**
 * Status bar displaying time, date, and battery information.
 *
 * @param deviceStatus Current device status
 * @param batteryIndicatorMode When to show battery indicator
 * @param modifier Modifier for the status bar
 * @param onClockTap Callback invoked when time/date area is tapped
 */
@Composable
fun StatusBar(
    deviceStatus: DeviceStatus,
    modifier: Modifier = Modifier,
    batteryIndicatorMode: BatteryThresholdMode = BatteryThresholdMode.BELOW_50,
    onClockTap: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Battery percentage text - only show on non-Pixel 8 Pro devices
        // Always reserve space to prevent layout shift, but make invisible when not shown
        if (!isPixel8Pro()) {
            val shouldShow = batteryIndicatorMode.shouldShow(deviceStatus.batteryPercentage)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(if (shouldShow) 1f else 0f),
            ) {
                Text(
                    text = "${deviceStatus.batteryPercentage}%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier =
                        Modifier.semantics {
                            contentDescription = if (shouldShow) "Battery percentage" else ""
                        },
                )

                if (deviceStatus.isCharging) {
                    Text(
                        text = "⚡",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        // Time in the middle
        val clockInteractionSource = remember { MutableInteractionSource() }
        val isClockPressed by clockInteractionSource.collectIsPressedAsState()
        val clockScale by animateFloatAsState(
            targetValue = if (isClockPressed) 0.95f else 1f,
            label = "clockScale",
        )
        Column(
            modifier =
                Modifier
                    .scale(clockScale)
                    .clickable(
                        interactionSource = clockInteractionSource,
                        indication = null,
                        onClick = onClockTap,
                    ).padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = deviceStatus.currentTime,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier =
                    Modifier.semantics {
                        contentDescription = "Current time"
                    },
            )

            // Date BELOW time
            Text(
                text = deviceStatus.currentDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier =
                    Modifier.semantics {
                        contentDescription = "Current date"
                    },
            )
        }
    }
}
