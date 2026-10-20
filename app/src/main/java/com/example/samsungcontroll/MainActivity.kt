package com.example.samsungcontroll

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.samsungcontroll.ui.screens.RemoteControlScreen
import com.example.samsungcontroll.ui.theme.SamsungControllTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RemoteViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SamsungControllTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = com.example.samsungcontroll.ui.theme.RemoteTokens.Background) {
                    RemoteControlScreen(viewModel)
                }
            }
        }
    }

    /**
     * Route the phone's physical volume buttons to the TV while a connection is
     * active. When disconnected we fall back to the default behavior so the
     * phone's own volume still works.
     */
    // Lint false positive: ComponentActivity.dispatchKeyEvent is a normal public override.
    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val isVolumeKey = keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (isVolumeKey && viewModel.volumeKeysEnabled &&
            viewModel.connectionState == ConnectionState.CONNECTED
        ) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                viewModel.sendKey(
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) "KEY_VOLUP" else "KEY_VOLDOWN"
                )
                if (viewModel.hapticsEnabled) performVolumeHaptic()
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * Crisp tactile tick for a volume key press, rendered by the platform haptic
     * engine. FLAG_IGNORE_VIEW_SETTING ensures it fires regardless of the
     * decor view's haptic-enabled attribute.
     */
    private fun performVolumeHaptic() {
        window.decorView.performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )
    }
}
