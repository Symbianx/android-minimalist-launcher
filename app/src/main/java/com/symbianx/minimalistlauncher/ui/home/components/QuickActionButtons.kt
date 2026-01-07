package com.symbianx.minimalistlauncher.ui.home.components

import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.symbianx.minimalistlauncher.domain.model.QuickActionConfig

/**
 * Quick action buttons for customizable apps at the bottom of the screen.
 * Icons are transparent (no background) and positioned in the corners.
 *
 * @param leftAction Configuration for the left quick action button
 * @param rightAction Configuration for the right quick action button
 * @param onLeftClick Callback when left button is clicked
 * @param onRightClick Callback when right button is clicked
 * @param modifier Modifier for the quick actions row
 */
@Composable
fun QuickActionButtons(
    leftAction: QuickActionConfig,
    rightAction: QuickActionConfig,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val iconColor = MaterialTheme.colorScheme.onBackground

    // Load left action icon
    val leftIconData =
        remember(leftAction.packageName) {
            loadAppIcon(packageManager, leftAction.packageName)
        }

    // Load right action icon
    val rightIconData =
        remember(rightAction.packageName) {
            loadAppIcon(packageManager, rightAction.packageName)
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Left button (bottom left corner)
        val leftInteractionSource = remember { MutableInteractionSource() }
        val isLeftPressed by leftInteractionSource.collectIsPressedAsState()
        val leftScale by animateFloatAsState(
            targetValue = if (isLeftPressed) 0.85f else 1f,
            label = "leftScale",
        )
        IconButton(
            onClick = onLeftClick,
            modifier =
                Modifier
                    .size(64.dp)
                    .scale(leftScale),
            interactionSource = leftInteractionSource,
        ) {
            leftIconData?.let { (appIcon, isMonochrome) ->
                Image(
                    bitmap = appIcon,
                    contentDescription = "Open ${leftAction.label}",
                    colorFilter = if (isMonochrome) ColorFilter.tint(iconColor) else null,
                    modifier = Modifier.size(56.dp),
                )
            }
        }

        // Right button (bottom right corner)
        val rightInteractionSource = remember { MutableInteractionSource() }
        val isRightPressed by rightInteractionSource.collectIsPressedAsState()
        val rightScale by animateFloatAsState(
            targetValue = if (isRightPressed) 0.85f else 1f,
            label = "rightScale",
        )
        IconButton(
            onClick = onRightClick,
            modifier =
                Modifier
                    .size(64.dp)
                    .scale(rightScale),
            interactionSource = rightInteractionSource,
        ) {
            rightIconData?.let { (appIcon, isMonochrome) ->
                Image(
                    bitmap = appIcon,
                    contentDescription = "Open ${rightAction.label}",
                    colorFilter = if (isMonochrome) ColorFilter.tint(iconColor) else null,
                    modifier = Modifier.size(56.dp),
                )
            }
        }
    }
}

/**
 * Loads app icon from PackageManager, preferring monochrome for minimalist style.
 * Returns Pair<ImageBitmap, Boolean> where Boolean indicates if it's monochrome.
 */
private fun loadAppIcon(
    packageManager: PackageManager,
    packageName: String,
): Pair<androidx.compose.ui.graphics.ImageBitmap, Boolean>? =
    try {
        val drawable = packageManager.getApplicationIcon(packageName)

        // Try to extract monochrome icon from adaptive icon (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            drawable is AdaptiveIconDrawable
        ) {
            val monochromeDrawable = drawable.monochrome
            if (monochromeDrawable != null) {
                // Return monochrome icon with true flag
                Pair(monochromeDrawable.toBitmap(96, 96).asImageBitmap(), true)
            } else {
                // Fallback to full icon if no monochrome available
                Pair(drawable.toBitmap(96, 96).asImageBitmap(), false)
            }
        } else if (drawable is BitmapDrawable) {
            Pair(drawable.bitmap.asImageBitmap(), false)
        } else {
            Pair(drawable.toBitmap(96, 96).asImageBitmap(), false)
        }
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
