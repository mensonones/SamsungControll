package com.example.samsungcontroll.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Two roles: a sans for titles/body and a monospace for key legends (VOL, CH, OK,
 * digits), like the printing on a physical remote. The families are the single place
 * to swap in bundled fonts (Space Grotesk + JetBrains Mono) later.
 */
val DisplayFont: FontFamily = FontFamily.SansSerif
val LegendFont: FontFamily = FontFamily.Monospace

object RemoteType {
    val Title = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp)
    val Body = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val Caption = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)
    val Section = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
    val Legend = TextStyle(fontFamily = LegendFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.8.sp)
    val Digit = TextStyle(fontFamily = LegendFont, fontWeight = FontWeight.Medium, fontSize = 26.sp, lineHeight = 32.sp)
}

val Typography = Typography(
    bodyLarge = RemoteType.Body.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = RemoteType.Body,
    bodySmall = RemoteType.Caption,
    titleLarge = RemoteType.Title.copy(fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = RemoteType.Title,
    labelLarge = RemoteType.Section,
    labelMedium = RemoteType.Legend,
    labelSmall = RemoteType.Legend
)
