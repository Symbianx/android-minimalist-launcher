package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom camera icon for quick actions.
 */
private val CameraIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Camera",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path {
            // Camera body
            moveTo(9f, 2f)
            lineTo(7.17f, 4f)
            horizontalLineTo(4f)
            curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
            verticalLineTo(18f)
            curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
            horizontalLineTo(20f)
            curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
            verticalLineTo(6f)
            curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
            horizontalLineTo(16.83f)
            lineTo(15f, 2f)
            horizontalLineTo(9f)
            close()
            // Camera lens
            moveTo(12f, 17f)
            curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
            curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
            curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
            curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
            close()
            // Inner lens
            moveTo(12f, 9f)
            curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
            curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
            curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
            curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
            close()
        }
    }.build()

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
                imageVector = CameraIcon,
                contentDescription = "Open camera",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
