package com.example.samsungcontroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.samsungcontroll.ui.screens.RemoteControlScreen
import com.example.samsungcontroll.ui.theme.SamsungControllTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SamsungControllTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B1020)) {
                    val viewModel: RemoteViewModel = koinViewModel()
                    RemoteControlScreen(viewModel)
                }
            }
        }
    }
}
