package com.example.samsungcontroll.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Interface defining tactile feedback operations (SOLID: Dependency Inversion & Interface Segregation).
 */
interface HapticsManager {
    fun performClick()
    fun performKeypress()
    fun performToggle()
}

/**
 * Android system Vibrator + Compose [HapticFeedback] implementation of [HapticsManager].
 */
class AndroidHapticsManager(
    private val context: Context?,
    private val hapticFeedback: HapticFeedback?
) : HapticsManager {

    override fun performClick() {
        if (!vibrate(30L, VibrationEffect.DEFAULT_AMPLITUDE)) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    override fun performKeypress() {
        if (!vibrate(15L, 100)) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    override fun performToggle() {
        if (!vibrate(50L, VibrationEffect.DEFAULT_AMPLITUDE)) {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int): Boolean {
        return try {
            val vibrator = getVibrator(context) ?: return false
            if (!vibrator.hasVibrator()) return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255))
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun getVibrator(context: Context?): Vibrator? {
        if (context == null) return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

/**
 * No-Op implementation of [HapticsManager] for Previews and Unit Tests (SOLID: Liskov Substitution).
 */
class NoOpHapticsManager : HapticsManager {
    override fun performClick() {}
    override fun performKeypress() {}
    override fun performToggle() {}
}

/**
 * CompositionLocal providing access to the current [HapticsManager].
 */
val LocalHapticsManager: ProvidableCompositionLocal<HapticsManager> = staticCompositionLocalOf {
    NoOpHapticsManager()
}

/**
 * Remembers an [HapticsManager] instance bound to current Compose environment.
 */
@Composable
fun rememberHapticsManager(): HapticsManager {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    return remember(context, hapticFeedback) {
        AndroidHapticsManager(context, hapticFeedback)
    }
}
