package com.symbianx.minimalistlauncher.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Utility for creating smooth animations in Compose.
 */
object AnimationUtil {
    /**
     * Default spring animation spec for smooth, natural motion
     */
    fun <T> defaultSpring(): AnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Default tween animation spec for controlled motion
     */
    fun <T> defaultTween(durationMs: Int = 300): AnimationSpec<T> = tween(
        durationMillis = durationMs
    )

    /**
     * Create a float animatable with default value
     */
    @Composable
    fun rememberFloatAnimatable(initialValue: Float = 0f): Animatable<Float, *> {
        return remember { Animatable(initialValue) }
    }
}
