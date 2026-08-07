package com.example.samsungcontroll.ui.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * Interface defining tactile feedback operations (SOLID: Dependency Inversion & Interface Segregation).
 */
interface HapticsManager {
    fun performClick()
    fun performKeypress()
    fun performToggle()
}

/**
 * Tactile feedback backed primarily by the platform view haptics
 * ([View.performHapticFeedback]), which the OS renders as a crisp, tuned tick and
 * is perceptible on devices where short raw [Vibrator] one-shots are not. The raw
 * vibrator is kept only as a fallback for callers without a [View].
 */
class AndroidHapticsManager(
    private val view: View?,
    private val context: Context? = null
) : HapticsManager {

    override fun performClick() =
        perform(HapticFeedbackConstants.VIRTUAL_KEY, durationMs = 30L, amplitude = VibrationEffect.DEFAULT_AMPLITUDE)

    override fun performKeypress() =
        perform(HapticFeedbackConstants.VIRTUAL_KEY, durationMs = 15L, amplitude = 100)

    override fun performToggle() =
        perform(HapticFeedbackConstants.LONG_PRESS, durationMs = 50L, amplitude = VibrationEffect.DEFAULT_AMPLITUDE)

    private fun perform(feedbackConstant: Int, durationMs: Long, amplitude: Int) {
        val performed = view?.performHapticFeedback(
            feedbackConstant,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        ) ?: false
        if (!performed) {
            vibrate(durationMs, amplitude)
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
    val view = LocalView.current
    val context = LocalContext.current
    return remember(view, context) {
        AndroidHapticsManager(view = view, context = context)
    }
}
