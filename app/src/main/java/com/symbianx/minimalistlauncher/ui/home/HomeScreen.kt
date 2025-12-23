package com.symbianx.minimalistlauncher.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.symbianx.minimalistlauncher.ui.home.components.AppContextMenu
import com.symbianx.minimalistlauncher.ui.home.components.AppLaunchOverlay
import com.symbianx.minimalistlauncher.ui.home.components.CircularBatteryIndicator
import com.symbianx.minimalistlauncher.ui.home.components.FavoritesList
import com.symbianx.minimalistlauncher.ui.home.components.GestureHandler
import com.symbianx.minimalistlauncher.ui.home.components.QuickActionButtons
import com.symbianx.minimalistlauncher.ui.home.components.SearchView
import com.symbianx.minimalistlauncher.ui.home.components.StatusBar
import com.symbianx.minimalistlauncher.ui.home.components.UnlockCountDisplay
import com.symbianx.minimalistlauncher.ui.home.components.isPixel8Pro

/**
 * Main home screen composable for the minimalist launcher.
 *
 * @param viewModel ViewModel managing the home screen state
 * @param modifier Modifier for the home screen
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val searchState by viewModel.searchState.collectAsState()
    val deviceStatus by viewModel.deviceStatus.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val contextMenuApp by viewModel.contextMenuApp.collectAsState()
    val unlockCount by viewModel.unlockCountToday.collectAsState()
    val lastUnlockTimeAgo by viewModel.lastUnlockTimeAgo.collectAsState()
    val appLaunchOverlayState by viewModel.appLaunchOverlayState.collectAsState()

    // Handle back button press when search is active
    BackHandler(enabled = searchState.isActive) {
        viewModel.deactivateSearch()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        GestureHandler(
            onSwipeRightToLeft = { viewModel.activateSearch() },
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                if (!searchState.isActive) {
                    if (isPixel8Pro()) {
                        // Circular battery indicator at absolute top center (around notch)
                        CircularBatteryIndicator(
                            batteryPercentage = deviceStatus.batteryPercentage,
                            isCharging = deviceStatus.isCharging,
                            modifier =
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = 8.dp),
                        )
                    }
                    // Unlock count display at top-left
                    UnlockCountDisplay(
                        unlockCount = unlockCount,
                        lastUnlockTimeAgo = lastUnlockTimeAgo,
                        modifier = Modifier.align(Alignment.TopStart),
                    )

                    // Status bar below circular indicator
                    StatusBar(
                        deviceStatus = deviceStatus,
                        onClockTap = { viewModel.openClockApp() },
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 80.dp),
                    )

                    // Favorites list below status bar
                    FavoritesList(
                        favorites = favorites,
                        onFavoriteClick = { viewModel.launchFavorite(it) },
                        onFavoriteLongPress = { viewModel.removeFromFavorites(it) },
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(top = 200.dp), // Adjust based on status bar height
                    )

                    // Quick action buttons at bottom
                    QuickActionButtons(
                        onPhoneClick = { viewModel.openPhoneDialer() },
                        onCameraClick = { viewModel.openCamera() },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }

        SearchView(
            searchState = searchState,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onAppClick = { viewModel.launchApp(it) },
            onAppLongPress = { viewModel.showContextMenu(it) },
            onSwipeBack = { viewModel.deactivateSearch() },
            autoLaunchEnabled = viewModel.autoLaunchEnabled,
            autoLaunchDelayMs = viewModel.autoLaunchDelayMs,
            modifier = Modifier.fillMaxSize(),
        )

        // Context menu
        contextMenuApp?.let { app ->
            AppContextMenu(
                app = app,
                isFavorite = viewModel.isAppFavorite(app),
                onDismiss = { viewModel.hideContextMenu() },
                onAddToFavorites = { viewModel.addAppToFavorites(app) },
                onRemoveFromFavorites = { viewModel.removeAppFromFavorites(app) },
                onOpenAppInfo = { viewModel.openAppInfo(app) },
            )
        }

        // App launch overlay
        appLaunchOverlayState?.let { state ->
            AppLaunchOverlay(
                appName = state.appName,
                launchCount = state.launchCount,
                lastLaunchTimeAgo = state.lastLaunchTimeAgo,
                visible = state.visible,
            )
        }
    }
}
