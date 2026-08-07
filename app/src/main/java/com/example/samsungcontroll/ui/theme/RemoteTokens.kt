package com.example.samsungcontroll.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Semantic design tokens for the remote UI. Centralizes the surface/elevation
 * ramp, text contrast levels, the single accent, and the radius scale so every
 * screen and widget reads from one system instead of scattered hex literals.
 */
object RemoteTokens {
    // Surface / elevation ramp (dark theme). Each step is visibly distinct from
    // the one below so buttons read as raised even when disabled.
    val Background = Color(0xFF0B1020)
    val Surface1 = Color(0xFF141B2C)   // cards
    val Surface2 = Color(0xFF212C42)   // idle button fill (clearly above the card)
    val Surface3 = Color(0xFF2E3B57)   // pressed / hover
    val SurfaceDisabled = Color(0xFF19212F) // disabled fill — still separated from bg

    // Text contrast levels.
    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFF9AA7BD)
    val TextDisabled = Color(0xFF8792A6) // legible grey, not the old near-invisible slate

    // Hairline borders.
    val Border = Color(0x14FFFFFF)       // ~8% white
    val BorderStrong = Color(0x24FFFFFF) // ~14% white

    // Single accent for selection / focus. Red is reserved for power only.
    val Accent = Color(0xFF38BDF8)
    val Power = Color(0xFFEF4444)

    // Radius scale.
    val RadiusKey = 18.dp
    val RadiusButton = 14.dp
    val RadiusCard = 28.dp
}
