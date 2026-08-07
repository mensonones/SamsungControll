package com.example.samsungcontroll

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.samsungcontroll.ui.screens.RemoteControlScreen
import com.example.samsungcontroll.ui.theme.SamsungControllTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RemoteViewModel by viewModel()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        setContent {
            SamsungControllTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B1020)) {
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
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val isVolumeKey = keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (isVolumeKey && viewModel.connectionState == ConnectionState.CONNECTED) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                viewModel.sendKey(
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) "KEY_VOLUP" else "KEY_VOLDOWN"
                )
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
