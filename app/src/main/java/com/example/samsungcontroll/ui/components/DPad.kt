package com.example.samsungcontroll.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.samsungcontroll.ui.haptics.LocalHapticsManager
import com.example.samsungcontroll.ui.theme.RemoteTokens
import com.example.samsungcontroll.ui.theme.RemoteType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot

private enum class PadZone { Up, Down, Left, Right, Ok }

private const val OK_RADIUS_FRACTION = 0.32f
private const val REPEAT_INITIAL_DELAY_MS = 400L
private const val REPEAT_INTERVAL_MS = 120L

data class DPadLabels(
    val pad: String,
    val up: String,
    val down: String,
    val left: String,
    val right: String,
    val ok: String
)

/**
 * Four-way pad: one disc split into up/down/left/right wedges around an OK button.
 * Every wedge is a large touch target (the whole sector, not a small arrow), the touched
 * wedge fills with the signal colour, and the four directions repeat while held.
 */
@Composable
fun DPad(
    enabled: Boolean,
    labels: DPadLabels,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onOk: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticsManager.current
    var pressed by remember { mutableStateOf<PadZone?>(null) }
    val actions = rememberUpdatedState(
        mapOf(
            PadZone.Up to onUp, PadZone.Down to onDown, PadZone.Left to onLeft,
            PadZone.Right to onRight, PadZone.Ok to onOk
        )
    )

    Box(
        modifier = modifier
            .background(RemoteTokens.Surface1, CircleShape)
            .border(1.dp, RemoteTokens.Border, CircleShape)
            .semantics {
                contentDescription = labels.pad
                role = Role.Button
                onClick(labels.ok) { if (enabled) onOk(); enabled }
                customActions = listOf(
                    CustomAccessibilityAction(labels.up) { if (enabled) onUp(); enabled },
                    CustomAccessibilityAction(labels.down) { if (enabled) onDown(); enabled },
                    CustomAccessibilityAction(labels.left) { if (enabled) onLeft(); enabled },
                    CustomAccessibilityAction(labels.right) { if (enabled) onRight(); enabled }
                )
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = { offset ->
                        val zone = zoneAt(offset, size.width.toFloat()) ?: return@detectTapGestures
                        pressed = zone
                        haptics.performKeypress()
                        actions.value.getValue(zone)()
                        coroutineScope {
                            val repeater = if (zone == PadZone.Ok) null else launch {
                                delay(REPEAT_INITIAL_DELAY_MS)
                                while (true) {
                                    actions.value.getValue(zone)()
                                    delay(REPEAT_INTERVAL_MS)
                                }
                            }
                            tryAwaitRelease()
                            repeater?.cancel()
                        }
                        pressed = null
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val okRadius = radius * OK_RADIUS_FRACTION

            // Pressed wedge.
            val wedgeStart = when (pressed) {
                PadZone.Up -> -135f
                PadZone.Right -> -45f
                PadZone.Down -> 45f
                PadZone.Left -> 135f
                else -> null
            }
            if (wedgeStart != null) {
                drawArc(
                    color = RemoteTokens.Accent,
                    startAngle = wedgeStart,
                    sweepAngle = 90f,
                    useCenter = true,
                    size = Size(radius * 2f, radius * 2f)
                )
            }

            // Diagonal dividers between wedges.
            val divider = RemoteTokens.Border
            listOf(45f, 135f, 225f, 315f).forEach { deg ->
                val rad = Math.toRadians(deg.toDouble())
                val dir = Offset(kotlin.math.cos(rad).toFloat(), kotlin.math.sin(rad).toFloat())
                drawLine(divider, center + dir * okRadius, center + dir * radius, strokeWidth = 1.dp.toPx())
            }

            // Chevrons.
            val idle = if (enabled) RemoteTokens.TextPrimary else RemoteTokens.TextDisabled
            fun tint(zone: PadZone) = if (pressed == zone) RemoteTokens.AccentInk else idle
            drawChevron(center + Offset(0f, -radius * 0.66f), 0f, tint(PadZone.Up))
            drawChevron(center + Offset(radius * 0.66f, 0f), 90f, tint(PadZone.Right))
            drawChevron(center + Offset(0f, radius * 0.66f), 180f, tint(PadZone.Down))
            drawChevron(center + Offset(-radius * 0.66f, 0f), 270f, tint(PadZone.Left))

            // OK disc.
            drawCircle(
                color = if (pressed == PadZone.Ok) RemoteTokens.Accent else if (enabled) RemoteTokens.Surface2 else RemoteTokens.SurfaceDisabled,
                radius = okRadius
            )
            drawCircle(
                color = RemoteTokens.BorderStrong,
                radius = okRadius,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Text(
            text = labels.ok,
            style = RemoteType.Legend.copy(fontSize = 16.sp),
            color = when {
                pressed == PadZone.Ok -> RemoteTokens.AccentInk
                enabled -> RemoteTokens.TextPrimary
                else -> RemoteTokens.TextDisabled
            }
        )
    }
}

/** Which wedge (or the OK disc) a touch lands on; null outside the disc. */
private fun zoneAt(offset: Offset, diameter: Float): PadZone? {
    val radius = diameter / 2f
    val dx = offset.x - radius
    val dy = offset.y - radius
    val distance = hypot(dx, dy)
    return when {
        distance > radius -> null
        distance < radius * OK_RADIUS_FRACTION -> PadZone.Ok
        abs(dx) > abs(dy) -> if (dx > 0) PadZone.Right else PadZone.Left
        else -> if (dy > 0) PadZone.Down else PadZone.Up
    }
}

/** A "^" chevron rotated by [rotation] degrees around [at]. */
private fun DrawScope.drawChevron(at: Offset, rotation: Float, color: Color) {
    val arm = size.minDimension * 0.045f
    val rad = Math.toRadians(rotation.toDouble())
    val cos = kotlin.math.cos(rad).toFloat()
    val sin = kotlin.math.sin(rad).toFloat()
    val rotated = Path()
    val pts = listOf(Offset(-arm * 1.6f, arm * 0.8f), Offset(0f, -arm * 0.8f), Offset(arm * 1.6f, arm * 0.8f))
        .map { Offset(at.x + it.x * cos - it.y * sin, at.y + it.x * sin + it.y * cos) }
    rotated.moveTo(pts[0].x, pts[0].y)
    rotated.lineTo(pts[1].x, pts[1].y)
    rotated.lineTo(pts[2].x, pts[2].y)
    drawPath(rotated, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}
