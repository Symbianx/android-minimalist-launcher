package com.symbianx.minimalistlauncher.util

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Utility for detecting swipe gestures in Compose UI.
 */
object GestureUtil {
    /**
     * Adds a horizontal swipe gesture detector.
     * @param onSwipeLeft Callback when user swipes from right to left
     * @param onSwipeRight Callback when user swipes from left to right
     * @param threshold Minimum swipe distance in pixels to trigger callback (default 100)
     */
    fun Modifier.swipeGesture(
        onSwipeLeft: () -> Unit = {},
        onSwipeRight: () -> Unit = {},
        threshold: Float = 100f
    ): Modifier = this.pointerInput(Unit) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onDragEnd = {
                if (totalDrag > threshold) {
                    onSwipeRight()
                } else if (totalDrag < -threshold) {
                    onSwipeLeft()
                }
                totalDrag = 0f
            },
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                totalDrag += dragAmount
            }
        )
    }
}
