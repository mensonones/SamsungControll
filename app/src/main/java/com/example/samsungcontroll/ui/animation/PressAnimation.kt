package com.example.samsungcontroll.ui.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Reusable modifier providing smooth spring-physics scale animation when pressed (SOLID: Single Responsibility Principle).
 */
@Composable
fun Modifier.pressScale(
    isPressed: Boolean,
    pressedScale: Float = 0.92f
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressScaleAnimation"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
