package com.example.samsungcontroll.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val REPEAT_INITIAL_DELAY_MS = 400L
private const val REPEAT_INTERVAL_MS = 120L

/**
 * Click that fires immediately on press and, while the finger stays down, keeps
 * firing [onRepeat] (after an initial delay) like holding a physical remote key.
 * Used for volume, channel and D-Pad keys.
 */
fun Modifier.repeatingClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    onRepeat: () -> Unit = {},
    onClick: () -> Unit
): Modifier = composed {
    val currentClick = rememberUpdatedState(onClick)
    val currentRepeat = rememberUpdatedState(onRepeat)

    this
        .semantics(mergeDescendants = true) {
            role = Role.Button
            onClick { if (enabled) currentClick.value(); enabled }
        }
        .indication(interactionSource, LocalIndication.current)
        .pointerInput(enabled, interactionSource) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = { offset ->
                    val press = PressInteraction.Press(offset)
                    interactionSource.emit(press)
                    currentClick.value()
                    coroutineScope {
                        val repeater = launch {
                            delay(REPEAT_INITIAL_DELAY_MS)
                            while (true) {
                                currentRepeat.value()
                                delay(REPEAT_INTERVAL_MS)
                            }
                        }
                        val released = tryAwaitRelease()
                        repeater.cancel()
                        interactionSource.emit(
                            if (released) PressInteraction.Release(press) else PressInteraction.Cancel(press)
                        )
                    }
                }
            )
        }
}
