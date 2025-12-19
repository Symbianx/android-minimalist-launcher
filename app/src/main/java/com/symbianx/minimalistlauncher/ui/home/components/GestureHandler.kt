package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Detects right-to-left swipe gestures to activate search.
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
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 100.dp.toPx() }
    
    var dragStartX by remember { mutableStateOf(0f) }
    var totalDrag by remember { mutableStateOf(0f) }

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragStartX = offset.x
                        totalDrag = 0f
                    },
                    onDragEnd = {
                        if (totalDrag < -swipeThreshold && abs(totalDrag) > swipeThreshold) {
                            onSwipeRightToLeft()
                        }
                        totalDrag = 0f
                    },
                    onDragCancel = {
                        totalDrag = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    }
                )
            }
    ) {
        content()
    }
}
