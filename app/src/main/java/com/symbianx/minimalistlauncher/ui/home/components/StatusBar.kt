package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.symbianx.minimalistlauncher.domain.model.DeviceStatus

/**
 * Status bar displaying time, date, and battery information.
 *
 * @param deviceStatus Current device status
 * @param modifier Modifier for the status bar
 */
@Composable
fun StatusBar(
    deviceStatus: DeviceStatus,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Battery percentage text - only show on non-Pixel 8 Pro devices
        if (!isPixel8Pro()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${deviceStatus.batteryPercentage}%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                
                if (deviceStatus.isCharging) {
                    Text(
                        text = "⚡",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        // Time in the middle
        Text(
            text = deviceStatus.currentTime,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        // Date BELOW time
        Text(
            text = deviceStatus.currentDate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}
