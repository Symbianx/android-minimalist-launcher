package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.symbianx.minimalistlauncher.domain.model.DeviceStatus
import com.symbianx.minimalistlauncher.domain.model.NowPlayingInfo

/**
 * Status bar displaying time, battery, and now playing information.
 *
 * @param deviceStatus Current device status
 * @param nowPlayingInfo Current now playing information
 * @param onNowPlayingClick Callback when now playing info is clicked
 * @param modifier Modifier for the status bar
 */
@Composable
fun StatusBar(
    deviceStatus: DeviceStatus,
    nowPlayingInfo: NowPlayingInfo? = null,
    onNowPlayingClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Battery percentage text
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

        // Now Playing info
        nowPlayingInfo?.let { info ->
            if (info.isAvailable && info.songName != null) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple()
                        ) { onNowPlayingClick() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = info.songName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    
                    info.artistName?.let { artist ->
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
