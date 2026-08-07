package com.example.samsungcontroll.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import com.example.samsungcontroll.ui.animation.pressScale
import com.example.samsungcontroll.ui.haptics.LocalHapticsManager
import com.example.samsungcontroll.ui.theme.RemoteTokens

@Composable
fun RemoteButton(
    icon: ImageVector,
    contentDescription: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    size: Dp = 54.dp
) {
    val haptics = LocalHapticsManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(size)
            .pressScale(isPressed = isPressed, pressedScale = 0.92f)
            .clip(CircleShape)
            .background(
                if (enabled) {
                    if (isPressed) color.copy(alpha = 0.8f) else color
                } else RemoteTokens.SurfaceDisabled
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple()
            ) {
                haptics.performClick()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (enabled) Color.White else RemoteTokens.TextDisabled,
            modifier = Modifier.size(size * 0.54f)
        )
    }
}

@Composable
fun RemoteIconButton(
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val haptics = LocalHapticsManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    IconButton(
        onClick = {
            haptics.performClick()
            onClick()
        },
        modifier = modifier
            .size(54.dp)
            .pressScale(isPressed = isPressed, pressedScale = 0.85f)
            .clip(CircleShape),
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (isPressed) Color.White else getEnabledColor(enabled),
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
fun RemoteSmallButton(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val haptics = LocalHapticsManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .pressScale(isPressed = isPressed, pressedScale = 0.96f)
            .clip(RoundedCornerShape(RemoteTokens.RadiusButton))
            .background(
                if (enabled) {
                    if (isPressed) RemoteTokens.Surface3 else RemoteTokens.Surface2
                } else RemoteTokens.SurfaceDisabled
            )
            .border(
                BorderStroke(1.dp, if (enabled) RemoteTokens.Border else Color.Transparent),
                RoundedCornerShape(RemoteTokens.RadiusButton)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple()
            ) {
                haptics.performKeypress()
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = getEnabledColor(enabled),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

@Composable
fun AppLaunchButton(
    label: String,
    bgColor: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    val haptics = LocalHapticsManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(46.dp)
            .pressScale(isPressed = isPressed, pressedScale = 0.95f)
            .clip(RoundedCornerShape(RemoteTokens.RadiusButton))
            .background(
                if (enabled) {
                    if (isPressed) RemoteTokens.Surface3 else RemoteTokens.Surface2
                } else RemoteTokens.SurfaceDisabled
            )
            .border(
                BorderStroke(1.dp, if (enabled) RemoteTokens.Border else Color.Transparent),
                RoundedCornerShape(RemoteTokens.RadiusButton)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple()
            ) {
                haptics.performClick()
                onClick()
            }
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (enabled) bgColor else bgColor.copy(alpha = 0.35f))
            )
            Text(
                label,
                color = getEnabledColor(enabled),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TvColorButton(
    color: Color,
    label: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptics = LocalHapticsManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(36.dp)
            .pressScale(isPressed = isPressed, pressedScale = 0.90f)
            .clip(CircleShape)
            .background(if (enabled) color.copy(alpha = if (isPressed) 0.75f else 0.95f) else Color(0xFF1F2937))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple()
            ) {
                haptics.performClick()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

fun getEnabledColor(enabled: Boolean): Color {
    return if (enabled) RemoteTokens.TextPrimary else RemoteTokens.TextDisabled
}
