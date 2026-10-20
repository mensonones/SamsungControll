package com.example.samsungcontroll.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.samsungcontroll.ui.animation.pressScale
import com.example.samsungcontroll.ui.haptics.LocalHapticsManager
import com.example.samsungcontroll.ui.theme.RemoteTokens
import com.example.samsungcontroll.ui.theme.RemoteType

fun getEnabledColor(enabled: Boolean): Color =
    if (enabled) RemoteTokens.TextPrimary else RemoteTokens.TextDisabled

/**
 * Fill and content colours for a key: idle surface, accent while pressed (the single place
 * the signal colour confirms a touch), disabled surface when the TV is not connected.
 */
private fun keyFill(enabled: Boolean, pressed: Boolean): Color = when {
    !enabled -> RemoteTokens.SurfaceDisabled
    pressed -> RemoteTokens.Accent
    else -> RemoteTokens.Surface2
}

private fun keyContent(enabled: Boolean, pressed: Boolean): Color = when {
    !enabled -> RemoteTokens.TextDisabled
    pressed -> RemoteTokens.AccentInk
    else -> RemoteTokens.TextPrimary
}

@OptIn(ExperimentalFoundationApi::class)
/** Text key printed in the legend (monospace) face, like BACK / HOME / HDMI 1 on a physical remote. */
@Composable
fun RemoteKey(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = RemoteType.Legend,
    minHeight: Dp = RemoteTokens.MinTouch,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val haptics = LocalHapticsManager.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Box(
        modifier = modifier
            .heightIn(min = minHeight)
            .pressScale(pressed)
            .clip(RoundedCornerShape(RemoteTokens.RadiusKey))
            .background(keyFill(enabled, pressed))
            .border(
                BorderStroke(1.dp, if (enabled) RemoteTokens.Border else Color.Transparent),
                RoundedCornerShape(RemoteTokens.RadiusKey)
            )
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        interactionSource = interaction,
                        indication = LocalIndication.current,
                        enabled = enabled,
                        onClick = { haptics.performKeypress(); onClick() },
                        onLongClick = { haptics.performToggle(); onLongClick() }
                    )
                } else {
                    Modifier.repeatingClickable(interaction, enabled) {
                        haptics.performKeypress()
                        onClick()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = textStyle,
            color = keyContent(enabled, pressed),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/** Icon key. [onRepeat] != null makes it fire repeatedly while held (volume, channel, transport). */
@Composable
fun RemoteIconKey(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onRepeat: () -> Unit = {},
    onClick: () -> Unit
) {
    val haptics = LocalHapticsManager.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Box(
        modifier = modifier
            .heightIn(min = RemoteTokens.MinTouch)
            .pressScale(pressed)
            .clip(RoundedCornerShape(RemoteTokens.RadiusKey))
            .background(keyFill(enabled, pressed))
            .border(
                BorderStroke(1.dp, if (enabled) RemoteTokens.Border else Color.Transparent),
                RoundedCornerShape(RemoteTokens.RadiusKey)
            )
            .repeatingClickable(interaction, enabled, onRepeat) {
                haptics.performKeypress()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = keyContent(enabled, pressed),
            modifier = Modifier.size(24.dp)
        )
    }
}

/** Round power button: outlined while the TV is on, filled red when pressing it would wake the TV. */
@Composable
fun PowerKey(
    contentDescription: String,
    icon: ImageVector,
    tvIsOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val haptics = LocalHapticsManager.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val filled = !tvIsOn || pressed

    Box(
        modifier = modifier
            .size(size)
            .pressScale(pressed)
            .clip(CircleShape)
            .background(if (filled) RemoteTokens.Power else Color.Transparent)
            .border(BorderStroke(1.5.dp, RemoteTokens.Power), CircleShape)
            .repeatingClickable(interaction, true) {
                haptics.performClick()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (filled) RemoteTokens.Background else RemoteTokens.Power,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
