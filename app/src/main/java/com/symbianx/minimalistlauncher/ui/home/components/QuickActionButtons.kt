package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Quick action buttons for phone and camera at the bottom of the screen.
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
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Phone button (bottom left)
        FilledIconButton(
            onClick = onPhoneClick,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Open phone dialer",
                modifier = Modifier.size(32.dp)
            )
        }

        // Camera button (bottom right) - using text emoji since icon not available
        FilledIconButton(
            onClick = onCameraClick,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text(
                text = "📷",
                fontSize = 32.sp
            )
        }
    }
}
