package com.symbianx.minimalistlauncher.ui.home

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.symbianx.minimalistlauncher.data.local.FavoritesDataSourceImpl
import com.symbianx.minimalistlauncher.data.repository.AppRepositoryImpl
import com.symbianx.minimalistlauncher.data.repository.DeviceStatusRepositoryImpl
import com.symbianx.minimalistlauncher.data.repository.FavoritesRepositoryImpl
import com.symbianx.minimalistlauncher.data.system.AppListDataSourceImpl
import com.symbianx.minimalistlauncher.data.system.BatteryDataSourceImpl
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.model.DeviceStatus
import com.symbianx.minimalistlauncher.domain.model.FavoriteApp
import com.symbianx.minimalistlauncher.domain.model.SearchState
import com.symbianx.minimalistlauncher.domain.usecase.LaunchAppUseCaseImpl
import com.symbianx.minimalistlauncher.domain.usecase.ManageFavoritesUseCase
import com.symbianx.minimalistlauncher.domain.usecase.ManageFavoritesUseCaseImpl
import com.symbianx.minimalistlauncher.domain.usecase.SearchAppsUseCaseImpl
import com.symbianx.minimalistlauncher.util.NavigationLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the home screen, managing app search and launch functionality.
 */
class HomeViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val appRepository =
        AppRepositoryImpl(
            AppListDataSourceImpl(application.applicationContext),
        )

    private val deviceStatusRepository =
        DeviceStatusRepositoryImpl(
            BatteryDataSourceImpl(application.applicationContext),
        )

    private val favoritesRepository =
        FavoritesRepositoryImpl(
            FavoritesDataSourceImpl(application.applicationContext),
        ).also {
            // Initialize favorites repository
            viewModelScope.launch {
                it.initialize()
            }
        }

    private val searchAppsUseCase = SearchAppsUseCaseImpl()
    private val launchAppUseCase = LaunchAppUseCaseImpl(application.applicationContext)
    private val manageFavoritesUseCase: ManageFavoritesUseCase = ManageFavoritesUseCaseImpl(favoritesRepository)

    // Auto-Launch configuration (future: make configurable via settings)
    val autoLaunchEnabled: Boolean = true
    val autoLaunchDelayMs: Long = 300L

    private val searchQuery = MutableStateFlow("")
    private val isSearchActive = MutableStateFlow(false)
    private val _contextMenuApp = MutableStateFlow<App?>(null)

    val contextMenuApp: StateFlow<App?> = _contextMenuApp

    private val allApps: StateFlow<List<App>> =
        appRepository
            .getApps()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    val searchState: StateFlow<SearchState> =
        combine(
            isSearchActive,
            searchQuery,
            allApps,
        ) { isActive, query, apps ->
            SearchState(
                isActive = isActive,
                query = query,
                results =
                    if (isActive) {
                        searchAppsUseCase.execute(apps, query)
                    } else {
                        emptyList()
                    },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchState(),
        )

    val deviceStatus: StateFlow<DeviceStatus> =
        deviceStatusRepository
            .observeDeviceStatus()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DeviceStatus(currentTime = "", currentDate = "", batteryPercentage = 0),
            )

    val favorites: StateFlow<List<FavoriteApp>> =
        manageFavoritesUseCase
            .observeFavorites()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    // Validate favorites when app list changes
    init {
        viewModelScope.launch {
            allApps.collect { apps ->
                favoritesRepository.validateFavorites(apps)
            }
        }
    }

    /**
     * Activates search mode.
     */
    fun activateSearch() {
        isSearchActive.value = true
    }

    /**
     * Deactivates search mode and clears query.
     */
    fun deactivateSearch() {
        isSearchActive.value = false
        searchQuery.value = ""
    }

    /**
     * Updates the search query.
     */
    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    /**
     * Launches an app and deactivates search.
     */
    fun launchApp(app: App) {
        viewModelScope.launch {
            if (launchAppUseCase.execute(app)) {
                deactivateSearch()
            }
        }
    }

    /**
     * Launches a favorite app.
     */
    fun launchFavorite(favorite: FavoriteApp) {
        viewModelScope.launch {
            // Find the app in the all apps list
            val app = allApps.value.find { it.packageName == favorite.packageName }
            if (app != null) {
                launchAppUseCase.execute(app)
            }
        }
    }

    /**
     * Adds an app to favorites via long-press.
     */
    fun addToFavorites(app: App) {
        viewModelScope.launch {
            when (val result = manageFavoritesUseCase.addToFavorites(app)) {
                is ManageFavoritesUseCase.AddFavoriteResult.Success -> {
                    Toast
                        .makeText(
                            getApplication(),
                            "Added to favorites",
                            Toast.LENGTH_SHORT,
                        ).show()
                }

                is ManageFavoritesUseCase.AddFavoriteResult.AlreadyExists -> {
                    Toast
                        .makeText(
                            getApplication(),
                            "Already in favorites",
                            Toast.LENGTH_SHORT,
                        ).show()
                }

                is ManageFavoritesUseCase.AddFavoriteResult.LimitReached -> {
                    Toast
                        .makeText(
                            getApplication(),
                            "Maximum 5 favorites allowed",
                            Toast.LENGTH_SHORT,
                        ).show()
                }

                is ManageFavoritesUseCase.AddFavoriteResult.Error -> {
                    Toast
                        .makeText(
                            getApplication(),
                            "Failed to add favorite",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }
    }

    /**
     * Removes an app from favorites via long-press.
     */
    fun removeFromFavorites(favorite: FavoriteApp) {
        viewModelScope.launch {
            manageFavoritesUseCase.removeFromFavorites(favorite.packageName)
            Toast
                .makeText(
                    getApplication(),
                    "Removed from favorites",
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    /**
     * Shows the context menu for a specific app.
     */
    fun showContextMenu(app: App) {
        _contextMenuApp.value = app
    }

    /**
     * Hides the context menu.
     */
    fun hideContextMenu() {
        _contextMenuApp.value = null
    }

    /**
     * Checks if an app is in favorites.
     */
    fun isAppFavorite(app: App): Boolean = favorites.value.any { it.packageName == app.packageName }

    /**
     * Adds an app to favorites from context menu.
     */
    fun addAppToFavorites(app: App) {
        viewModelScope.launch {
            when (val result = manageFavoritesUseCase.addToFavorites(app)) {
                is ManageFavoritesUseCase.AddFavoriteResult.Success -> {
                    Toast
                        .makeText(
                            getApplication(),
                            "Added to favorites",
                            Toast.LENGTH_SHORT,
                        ).show()
                }

                is ManageFavoritesUseCase.AddFavoriteResult.AlreadyExists -> {
                    Toast
                        .makeText(
                            getApplication(),
                            "Already in favorites",
                            Toast.LENGTH_SHORT,
                        ).show()
                }

                is ManageFavoritesUseCase.AddFavoriteResult.LimitReached -> {
                    Toast
                        .makeText(
                            getApplication(),
                            "Maximum 5 favorites allowed",
                            Toast.LENGTH_SHORT,
                        ).show()
                }

                is ManageFavoritesUseCase.AddFavoriteResult.Error -> {
                    Toast
                        .makeText(
                            getApplication(),
                            "Failed to add favorite",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }
    }

    /**
     * Removes an app from favorites from context menu.
     */
    fun removeAppFromFavorites(app: App) {
        viewModelScope.launch {
            manageFavoritesUseCase.removeFromFavorites(app.packageName)
            Toast
                .makeText(
                    getApplication(),
                    "Removed from favorites",
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    /**
     * Opens the phone dialer app.
     */
    fun openPhoneDialer() {
        try {
            val intent =
                Intent(Intent.ACTION_DIAL).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            Toast
                .makeText(
                    getApplication(),
                    "Phone app not available",
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    /**
     * Opens the camera app.
     */
    fun openCamera() {
        try {
            val intent =
                Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            // Try alternative method - launch camera via package name
            try {
                val cameraIntent =
                    getApplication<Application>()
                        .packageManager
                        .getLaunchIntentForPackage("com.google.android.GoogleCamera")
                        ?: getApplication<Application>()
                            .packageManager
                            .getLaunchIntentForPackage("com.android.camera2")

                if (cameraIntent != null) {
                    cameraIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    getApplication<Application>().startActivity(cameraIntent)
                } else {
                    Toast
                        .makeText(
                            getApplication(),
                            "Camera app not available",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            } catch (ex: Exception) {
                Toast
                    .makeText(
                        getApplication(),
                        "Camera app not available",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    /**
     * Opens the system clock/alarm app.
     */
    fun openClockApp() {
        NavigationLogger.logClockQuickAccess()
        try {
            val intent =
                Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            // Fallback: try to launch the clock app directly
            try {
                val clockIntent =
                    getApplication<Application>()
                        .packageManager
                        .getLaunchIntentForPackage("com.google.android.deskclock")
                        ?: getApplication<Application>()
                            .packageManager
                            .getLaunchIntentForPackage("com.android.deskclock")

                if (clockIntent != null) {
                    clockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    getApplication<Application>().startActivity(clockIntent)
                } else {
                    Toast
                        .makeText(
                            getApplication(),
                            "Clock app not available",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            } catch (ex: Exception) {
                Toast
                    .makeText(
                        getApplication(),
                        "Clock app not available",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    /**
     * Opens the Android system App Info screen for a given app.
     */
    fun openAppInfo(app: App) {
        try {
            val intent = createAppInfoIntent(app)
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            Toast
                .makeText(
                    getApplication(),
                    "Unable to open app info",
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    companion object {
        fun createAppInfoIntent(app: App): Intent =
            Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", app.packageName, null),
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }
}
