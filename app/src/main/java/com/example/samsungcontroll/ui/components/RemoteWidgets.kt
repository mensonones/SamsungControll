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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RemoteButton(
    icon: ImageVector,
    contentDescription: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit,
    size: Dp = 54.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                val scale = if (isPressed) 0.92f else 1f
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                if (enabled) {
                    if (isPressed) color.copy(alpha = 0.8f) else color
                } else Color(0xFF334155)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple()
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (enabled) Color.White else Color(0xFF64748B),
            modifier = Modifier.size(size * 0.56f)
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(58.dp)
            .graphicsLayer {
                val scale = if (isPressed) 0.85f else 1f
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape),
        enabled = enabled,
        interactionSource = interactionSource
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (isPressed) Color.White else getEnabledColor(enabled),
            modifier = Modifier.size(38.dp)
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .heightIn(min = 44.dp)
            .graphicsLayer {
                val scale = if (isPressed) 0.96f else 1f
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (enabled) {
                    if (isPressed) Color(0xFF2D3A4F) else Color(0xFF1F2937)
                } else Color(0xFF111827)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple()
            ) { onClick() }
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(44.dp)
            .graphicsLayer {
                val scale = if (isPressed) 0.95f else 1f
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (enabled) {
                    if (isPressed) bgColor.copy(alpha = 0.8f) else bgColor
                } else Color(0xFF111827)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple()
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (enabled) textColor else Color(0xFF475569),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

fun getEnabledColor(enabled: Boolean): Color {
    return if (enabled) Color.White else Color(0xFF475569)
}
