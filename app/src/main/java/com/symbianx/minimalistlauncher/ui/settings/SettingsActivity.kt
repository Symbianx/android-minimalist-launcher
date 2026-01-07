package com.symbianx.minimalistlauncher.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.symbianx.minimalistlauncher.ui.settings.components.SettingsScreen
import com.symbianx.minimalistlauncher.ui.theme.MinimalistLauncherTheme

/**
 * Activity for displaying and managing launcher settings.
 */
class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        setContent {
            MinimalistLauncherTheme {
                SettingsScreen(
                    onBack = { finish() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
