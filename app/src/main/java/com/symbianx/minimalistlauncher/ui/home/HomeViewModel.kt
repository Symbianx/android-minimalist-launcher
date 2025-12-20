package com.symbianx.minimalistlauncher.ui.home

import android.app.Application
import android.content.Intent
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the home screen, managing app search and launch functionality.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepositoryImpl(
        AppListDataSourceImpl(application.applicationContext)
    )
    
    private val deviceStatusRepository = DeviceStatusRepositoryImpl(
        BatteryDataSourceImpl(application.applicationContext)
    )
    
    private val favoritesRepository = FavoritesRepositoryImpl(
        FavoritesDataSourceImpl(application.applicationContext)
    ).also {
        // Initialize favorites repository
        viewModelScope.launch {
            it.initialize()
        }
    }
    
    private val searchAppsUseCase = SearchAppsUseCaseImpl()
    private val launchAppUseCase = LaunchAppUseCaseImpl(application.applicationContext)
    private val manageFavoritesUseCase: ManageFavoritesUseCase = ManageFavoritesUseCaseImpl(favoritesRepository)

    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    
    private val allApps: StateFlow<List<App>> = appRepository.getApps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val searchState: StateFlow<SearchState> = combine(
        _isSearchActive,
        _searchQuery,
        allApps
    ) { isActive, query, apps ->
        SearchState(
            isActive = isActive,
            query = query,
            results = if (isActive && query.isNotBlank()) {
                searchAppsUseCase.execute(apps, query)
            } else {
                emptyList()
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchState()
    )

    val deviceStatus: StateFlow<DeviceStatus> = deviceStatusRepository.observeDeviceStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DeviceStatus(currentTime = "", currentDate = "", batteryPercentage = 0)
        )

    val favorites: StateFlow<List<FavoriteApp>> = manageFavoritesUseCase.observeFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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
        _isSearchActive.value = true
    }

    /**
     * Deactivates search mode and clears query.
     */
    fun deactivateSearch() {
        _isSearchActive.value = false
        _searchQuery.value = ""
    }

    /**
     * Updates the search query.
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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
                    Toast.makeText(
                        getApplication(),
                        "Added to favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is ManageFavoritesUseCase.AddFavoriteResult.AlreadyExists -> {
                    Toast.makeText(
                        getApplication(),
                        "Already in favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is ManageFavoritesUseCase.AddFavoriteResult.LimitReached -> {
                    Toast.makeText(
                        getApplication(),
                        "Maximum 5 favorites allowed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is ManageFavoritesUseCase.AddFavoriteResult.Error -> {
                    Toast.makeText(
                        getApplication(),
                        "Failed to add favorite",
                        Toast.LENGTH_SHORT
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
            Toast.makeText(
                getApplication(),
                "Removed from favorites",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Opens the phone dialer app.
     */
    fun openPhoneDialer() {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                getApplication(),
                "Phone app not available",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Opens the camera app.
     */
    fun openCamera() {
        try {
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            // Try alternative method - launch camera via package name
            try {
                val cameraIntent = getApplication<Application>().packageManager
                    .getLaunchIntentForPackage("com.google.android.GoogleCamera")
                    ?: getApplication<Application>().packageManager
                        .getLaunchIntentForPackage("com.android.camera2")
                
                if (cameraIntent != null) {
                    cameraIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    getApplication<Application>().startActivity(cameraIntent)
                } else {
                    Toast.makeText(
                        getApplication(),
                        "Camera app not available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (ex: Exception) {
                Toast.makeText(
                    getApplication(),
                    "Camera app not available",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
