package com.example.samsungcontroll.ui.animation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

private const val PRESS_DURATION_MS = 90

/**
 * Subtle scale-down while pressed. A short eased tween (no spring, no overshoot): UI state
 * changes should settle, not bounce. Collapses to <=150 ms, so it stays calm under
 * reduced-motion settings too.
 */
@Composable
fun Modifier.pressScale(
    isPressed: Boolean,
    pressedScale: Float = 0.96f
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = tween(durationMillis = PRESS_DURATION_MS),
        label = "pressScale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
