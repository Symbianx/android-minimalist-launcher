package com.symbianx.minimalistlauncher.ui.home.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Detects swipe gestures to trigger different actions:
 * - Right-to-left: Activate search
 * - Bottom-up: Open device search
 * - Top-down: Open notification panel
 *
 * @param onSwipeRightToLeft Callback when right-to-left swipe is detected
 * @param modifier Modifier for the gesture detector
 * @param content Composable content that should detect gestures
 */
@Composable
fun GestureHandler(
    onSwipeRightToLeft: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 50.dp.toPx() } // Reduced from 100dp for faster activation
    
    var dragStartX by remember { mutableStateOf(0f) }
    var dragStartY by remember { mutableStateOf(0f) }
    var totalDragX by remember { mutableStateOf(0f) }
    var totalDragY by remember { mutableStateOf(0f) }

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragStartX = offset.x
                        dragStartY = offset.y
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragEnd = {
                        // Determine which gesture was performed based on predominant direction
                        val absX = abs(totalDragX)
                        val absY = abs(totalDragY)
                        
                        if (absX > absY && absX > swipeThreshold) {
                            // Horizontal swipe
                            if (totalDragX < -swipeThreshold) {
                                // Right to left swipe - activate search
                                onSwipeRightToLeft()
                            }
                        } else if (absY > absX && absY > swipeThreshold) {
                            // Vertical swipe
                            if (totalDragY < -swipeThreshold) {
                                // Bottom to top swipe - open device search
                                openDeviceSearch(context)
                            } else if (totalDragY > swipeThreshold && dragStartY < 100f) {
                                // Top to bottom swipe (starting from top) - open notifications
                                openNotificationPanel(context)
                            }
                        }
                        
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragCancel = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { _, dragAmount ->
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * Opens the device's default search (typically Google search or assistant).
 */
private fun openDeviceSearch(context: Context) {
    try {
        // Try to open Google Search app with search intent
        val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(searchIntent)
    } catch (e: Exception) {
        // Fallback: open app drawer or assistant
        try {
            val intent = Intent(Intent.ACTION_ASSIST).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Silent fail if no search available
        }
    }
}

/**
 * Opens the notification panel.
 */
private fun openNotificationPanel(context: Context) {
    try {
        @Suppress("DEPRECATION")
        val statusBarService = context.getSystemService(Context.STATUS_BAR_SERVICE)
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val expandMethod = statusBarManager.getMethod("expandNotificationsPanel")
        expandMethod.invoke(statusBarService)
    } catch (e: Exception) {
        // Silent fail if unable to open notifications
        // This is expected on some devices/Android versions
    }
}
