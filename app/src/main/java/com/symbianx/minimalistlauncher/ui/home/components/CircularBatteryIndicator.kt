package com.symbianx.minimalistlauncher.ui.home.components

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.symbianx.minimalistlauncher.domain.model.BatteryThresholdMode

/**
 * Circular battery indicator that displays battery level as a progress ring.
 * Visibility controlled by threshold mode setting.
 * On Pixel 8 Pro, positioned around camera notch area.
 *
 * @param batteryPercentage Current battery level (0-100)
 * @param isCharging Whether the device is currently charging
 * @param thresholdMode When to show the indicator
 * @param modifier Modifier for the indicator
 */
@Composable
fun CircularBatteryIndicator(
    batteryPercentage: Int,
    isCharging: Boolean,
    thresholdMode: BatteryThresholdMode = BatteryThresholdMode.BELOW_50,
    modifier: Modifier = Modifier,
) {
    // Check if indicator should be shown based on threshold mode
    if (!thresholdMode.shouldShow(batteryPercentage)) {
        return
    }

    // Validate battery percentage
    if (batteryPercentage < 0 || batteryPercentage > 100) {
        return
    }

    // Charging animation: subtle pulse effect
    val infiniteTransition = rememberInfiniteTransition(label = "charging")
    val chargingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "chargingPulse",
    )

    val batteryColor =
        when {
            batteryPercentage <= 10 -> Color(0xFFFF0000) // Red at 10% or below
            batteryPercentage <= 20 -> Color(0xFFFFA500) // Orange at 20% or below
            else -> MaterialTheme.colorScheme.primary // Primary color for 21-100%
        }

    val backgroundColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)

    Box(
        modifier = modifier.size(34.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(34.dp)) {
            val strokeWidth = 2.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft =
                Offset(
                    x = (size.width - radius * 2) / 2,
                    y = (size.height - radius * 2) / 2,
                )
            val arcSize = Size(radius * 2, radius * 2)

            // Use actual battery percentage for display
            val sweepAngle = (batteryPercentage / 100f) * 360f

            // Apply pulse alpha when charging, full opacity when not
            val displayAlpha = if (isCharging) chargingAlpha else 1.0f

            // Only draw the battery level arc (no background circle)
            when {
                batteryPercentage <= 0 -> { /* Draw nothing for 0% */ }
                batteryPercentage >= 100 -> {
                    // Draw full circle for 100%
                    drawArc(
                        color = batteryColor.copy(alpha = displayAlpha),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }
                else -> {
                    drawArc(
                        color = batteryColor.copy(alpha = displayAlpha),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }
            }
        }
    }
}

/**
 * Checks if the device is a Pixel 8 Pro.
 * This is a simplified check based on device model.
 */
fun isPixel8Pro(): Boolean {
    return Build.MODEL.contains("Pixel 8 Pro", ignoreCase = true) ||
        Build.MODEL.contains("Pixel 8a", ignoreCase = true) // Also support Pixel 8a
}
