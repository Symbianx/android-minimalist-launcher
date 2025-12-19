package com.symbianx.minimalistlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.symbianx.minimalistlauncher.ui.home.HomeScreen
import com.symbianx.minimalistlauncher.ui.theme.MinimalistLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinimalistLauncherTheme {
                HomeScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
