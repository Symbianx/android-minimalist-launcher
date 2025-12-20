package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Quick action buttons for phone and camera at the bottom of the screen.
 * Icons are transparent (no background) and positioned in the corners.
 *
 * @param onPhoneClick Callback when phone button is clicked
 * @param onCameraClick Callback when camera button is clicked
 * @param modifier Modifier for the quick actions row
 */
@Composable
fun QuickActionButtons(
    onPhoneClick: () -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Phone button (bottom left corner)
        IconButton(
            onClick = onPhoneClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Open phone dialer",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        // Camera button (bottom right corner)
        IconButton(
            onClick = onCameraClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = "Open camera",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
