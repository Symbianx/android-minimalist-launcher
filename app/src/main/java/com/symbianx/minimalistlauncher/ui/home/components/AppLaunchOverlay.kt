package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AppLaunchOverlay(
    appName: String,
    launchCount: Int,
    lastLaunchTimeAgo: String?,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            progress = 0f
            // Animate progress from 0 to 1 over 800ms
            val steps = 80
            val delayPerStep = 10L
            repeat(steps) {
                delay(delayPerStep)
                progress = (it + 1) / steps.toFloat()
            }
        } else {
            progress = 0f
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "progress",
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                        ).padding(horizontal = 32.dp, vertical = 24.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatLaunchCount(launchCount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lastLaunchTimeAgo != null) "last opened $lastLaunchTimeAgo" else "first time today",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
}

private fun formatLaunchCount(count: Int): String {
    val suffix =
        when (count % 10) {
            1 -> if (count == 11) "th" else "st"
            2 -> if (count == 12) "th" else "nd"
            3 -> if (count == 13) "th" else "rd"
            else -> "th"
        }
    return "${count}$suffix time today"
}
