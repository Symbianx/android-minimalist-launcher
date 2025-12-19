package com.symbianx.minimalistlauncher.ui.home.components

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Circular battery indicator that displays battery level as a progress ring.
 * On Pixel 8 Pro, positioned around camera notch area.
 * On other devices, falls back to standard display.
 *
 * @param batteryPercentage Current battery level (0-100)
 * @param isCharging Whether the device is currently charging
 * @param modifier Modifier for the indicator
 */
@Composable
fun CircularBatteryIndicator(
    batteryPercentage: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val isPixel8Pro = isPixel8Pro()
    
    if (!isPixel8Pro) {
        // Fallback: Don't display circular indicator on non-Pixel 8 Pro devices
        return
    }

    val batteryColor = when {
        isCharging -> MaterialTheme.colorScheme.tertiary
        batteryPercentage > 50 -> MaterialTheme.colorScheme.primary
        batteryPercentage > 20 -> Color(0xFFFFA500) // Orange
        else -> MaterialTheme.colorScheme.error
    }

    val backgroundColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)

    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(48.dp)) {
            val strokeWidth = 4.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = Offset(
                x = (size.width - radius * 2) / 2,
                y = (size.height - radius * 2) / 2
            )
            val arcSize = Size(radius * 2, radius * 2)

            // Background circle
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Battery level arc
            val sweepAngle = (batteryPercentage / 100f) * 360f
            drawArc(
                color = batteryColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Checks if the device is a Pixel 8 Pro.
 * This is a simplified check based on device model.
 */
private fun isPixel8Pro(): Boolean {
    return Build.MODEL.contains("Pixel 8 Pro", ignoreCase = true) ||
           Build.MODEL.contains("Pixel 8a", ignoreCase = true) // Also support Pixel 8a
}
