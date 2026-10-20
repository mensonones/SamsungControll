package com.example.samsungcontroll.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design tokens for the remote UI ("Graphite & Signal", see design.md).
 *
 * Near-neutral graphite surfaces, one signal-green accent used only for state
 * (connected, focus, pressed key), and red reserved for power/danger. Every screen
 * and widget reads from here; there are no raw hex literals in UI code.
 * Values are OKLCH-derived (hue 250 neutrals, accent oklch(0.86 0.18 125)).
 */
object RemoteTokens {
    // Surface ramp. Each step is visibly distinct from the one below.
    val Background = Color(0xFF0C1014)       // oklch(0.17 0.01 250)
    val Surface1 = Color(0xFF15191D)         // panels, pad
    val Surface2 = Color(0xFF20252A)         // idle key fill
    val Surface3 = Color(0xFF2E343A)         // hovered / raised
    val SurfaceDisabled = Color(0xFF1A1D21)

    // Text. Primary and secondary both clear 4.5:1 on every surface above.
    val TextPrimary = Color(0xFFEFF2F5)
    val TextSecondary = Color(0xFFA5ACB2)
    val TextDisabled = Color(0xFF7C8186)

    // Hairlines.
    val Border = Color(0x1AFFFFFF)           // ~10% white
    val BorderStrong = Color(0x2EFFFFFF)     // ~18% white

    // Signal accent: state only (<= ~5% of the screen).
    val Accent = Color(0xFFB6E551)           // oklch(0.86 0.18 125)
    val AccentInk = Color(0xFF111A00)        // text/icons on top of Accent
    val AccentMuted = Color(0x33B6E551)      // 20% accent, pressed wedge / selected tile

    // Power and destructive actions only.
    val Power = Color(0xFFFC5855)            // oklch(0.68 0.20 25)
    val Warn = Color(0xFFF5AE39)             // waiting-for-permission

    // Radius scale: two radii plus circles for pad/power.
    val RadiusKey = 12.dp
    val RadiusSurface = 20.dp
    @Deprecated("use RadiusKey") val RadiusButton = RadiusKey
    @Deprecated("use RadiusSurface") val RadiusCard = RadiusSurface

    // 4 dp spacing scale.
    val Space1 = 4.dp
    val Space2 = 8.dp
    val Space3 = 12.dp
    val Space4 = 16.dp
    val Space6 = 24.dp
    val Space8 = 32.dp

    val MinTouch = 48.dp
}
