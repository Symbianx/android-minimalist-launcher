package com.symbianx.minimalistlauncher.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.symbianx.minimalistlauncher.data.local.SettingsDataSourceImpl
import com.symbianx.minimalistlauncher.data.repository.AppRepositoryImpl
import com.symbianx.minimalistlauncher.data.repository.SettingsRepositoryImpl
import com.symbianx.minimalistlauncher.data.system.AppListDataSourceImpl
import com.symbianx.minimalistlauncher.domain.model.App
import com.symbianx.minimalistlauncher.domain.model.BatteryThresholdMode
import com.symbianx.minimalistlauncher.domain.model.QuickActionConfig
import com.symbianx.minimalistlauncher.domain.usecase.LoadSettingsUseCase
import com.symbianx.minimalistlauncher.util.SettingsLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 * Manages loading and updating launcher settings.
 *
 * @param application Application context
 */
class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val appRepository =
        AppRepositoryImpl(
            AppListDataSourceImpl(application.applicationContext),
        )

    private val settingsDataSource = SettingsDataSourceImpl(application.applicationContext)

    private val settingsRepository =
        SettingsRepositoryImpl(
            settingsDataSource,
            appRepository,
            SettingsLogger(),
        )

    private val loadSettingsUseCase = LoadSettingsUseCase(settingsRepository)

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _showAppPicker = MutableStateFlow<AppPickerTarget?>(null)
    val showAppPicker: StateFlow<AppPickerTarget?> = _showAppPicker.asStateFlow()

    private val _showResetDialog = MutableStateFlow(false)
    val showResetDialog: StateFlow<Boolean> = _showResetDialog.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            loadSettingsUseCase()
                .catch { error ->
                    _uiState.value =
                        SettingsUiState.Error(
                            error.message ?: "Failed to load settings",
                        )
                }.collect { settings ->
                    _uiState.value = SettingsUiState.Loaded(settings)
                }
        }
    }

    fun updateAutoLaunch(enabled: Boolean) {
        viewModelScope.launch {
            val current = (_uiState.value as? SettingsUiState.Loaded)?.settings ?: return@launch
            val updated = current.copy(autoLaunchEnabled = enabled)
            settingsRepository.updateSettings(updated)
        }
    }

    fun updateLeftQuickAction(app: App) {
        viewModelScope.launch {
            val current = (_uiState.value as? SettingsUiState.Loaded)?.settings ?: return@launch
            val config = QuickActionConfig(app.packageName, app.label, isDefault = false)
            val updated = current.copy(leftQuickAction = config)
            settingsRepository.updateSettings(updated)
            _showAppPicker.value = null
        }
    }

    fun updateRightQuickAction(app: App) {
        viewModelScope.launch {
            val current = (_uiState.value as? SettingsUiState.Loaded)?.settings ?: return@launch
            val config = QuickActionConfig(app.packageName, app.label, isDefault = false)
            val updated = current.copy(rightQuickAction = config)
            settingsRepository.updateSettings(updated)
            _showAppPicker.value = null
        }
    }

    fun updateClockApp(app: App) {
        viewModelScope.launch {
            val current = (_uiState.value as? SettingsUiState.Loaded)?.settings ?: return@launch
            val updated =
                current.copy(
                    clockAppPackage = app.packageName,
                    clockAppLabel = app.label,
                )
            settingsRepository.updateSettings(updated)
            _showAppPicker.value = null
        }
    }

    fun resetClockAppToDefault() {
        viewModelScope.launch {
            val current = (_uiState.value as? SettingsUiState.Loaded)?.settings ?: return@launch
            val updated =
                current.copy(
                    clockAppPackage = null,
                    clockAppLabel = null,
                )
            settingsRepository.updateSettings(updated)
        }
    }

    fun updateBatteryMode(mode: BatteryThresholdMode) {
        viewModelScope.launch {
            val current = (_uiState.value as? SettingsUiState.Loaded)?.settings ?: return@launch
            val updated = current.copy(batteryIndicatorMode = mode)
            settingsRepository.updateSettings(updated)
        }
    }

    fun showAppPickerFor(target: AppPickerTarget) {
        _showAppPicker.value = target
    }

    fun dismissAppPicker() {
        _showAppPicker.value = null
    }

    fun showResetDialog() {
        _showResetDialog.value = true
    }

    fun dismissResetDialog() {
        _showResetDialog.value = false
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
            _showResetDialog.value = false
        }
    }
}

/**
 * Target for app picker dialog.
 */
enum class AppPickerTarget {
    LEFT,
    RIGHT,
    CLOCK,
}
