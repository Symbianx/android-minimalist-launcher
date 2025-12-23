package com.symbianx.minimalistlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.symbianx.minimalistlauncher.ui.home.HomeScreen
import com.symbianx.minimalistlauncher.ui.home.HomeViewModel
import com.symbianx.minimalistlauncher.ui.theme.MinimalistLauncherTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var unlockReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Hide system bars (status bar and navigation bar)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            // Hide status bar
            hide(WindowInsetsCompat.Type.statusBars())
            // Set behavior for when user swipes to reveal system bars
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            MinimalistLauncherTheme {
                val homeViewModel: HomeViewModel = viewModel()
                HomeScreen(modifier = Modifier.fillMaxSize(), viewModel = homeViewModel)

                // Register unlock receiver
                if (unlockReceiver == null) {
                    unlockReceiver =
                        object : BroadcastReceiver() {
                            override fun onReceive(
                                context: Context,
                                intent: Intent,
                            ) {
                                if (intent.action == Intent.ACTION_USER_PRESENT) {
                                    lifecycleScope.launch {
                                        homeViewModel.onUnlockEvent()
                                    }
                                }
                            }
                        }
                    registerReceiver(unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unlockReceiver?.let { unregisterReceiver(it) }
        unlockReceiver = null
    }

    override fun onResume() {
        super.onResume()
        // Force refresh usage stats on resume to handle midnight reset
        val homeViewModel: HomeViewModel? =
            try {
                // Try to get the ViewModel if it exists
                androidx.lifecycle.ViewModelProvider(this).get(HomeViewModel::class.java)
            } catch (e: Exception) {
                null
            }
        homeViewModel?.let { vm ->
            lifecycleScope.launch {
                vm.refreshUsageStats() // Refresh stats without incrementing unlock count
            }
        }
    }
}
