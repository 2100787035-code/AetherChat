package com.aetherchat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aetherchat.app.navigation.AetherChatNavHost
import com.aetherchat.core.ui.theme.AetherChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AetherChatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AetherChatNavHost()
                }
            }
        }
    }
}
