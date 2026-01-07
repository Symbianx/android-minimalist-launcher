# Quickstart Guide: Settings Activity Implementation

**Feature**: Launcher Settings Activity  
**Branch**: `007-settings-activity`  
**Date**: 2026-01-07

## Overview

This guide provides developers with essential information to implement the Settings Activity feature, including setup instructions, key file locations, testing approach, and integration points.

---

## Prerequisites

- Android Studio latest stable
- Android SDK API 26+ (target API 36)
- Kotlin 1.9+
- Existing Android Minimalist Launcher project cloned

---

## Quick Start

### 1. Add Dependencies

Add DataStore Preferences to `app/build.gradle.kts`:

```kotlin
dependencies {
    // Existing dependencies...
    
    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
```

Sync project with Gradle.

---

### 2. Create Package Structure

Create new packages under `app/src/main/java/com/symbianx/minimalistlauncher/`:

```bash
ui/settings/
ui/settings/components/
domain/model/ (settings models)
domain/repository/ (SettingsRepository)
domain/usecase/ (settings use cases)
data/local/ (SettingsDataSource)
data/repository/ (SettingsRepositoryImpl)
util/ (SettingsLogger)
```

---

### 3. Implement Data Layer (Bottom-Up)

#### Step 3a: Define Domain Models

**File**: `domain/model/LauncherSettings.kt`

```kotlin
data class LauncherSettings(
    val autoLaunchEnabled: Boolean = true,
    val leftQuickAction: QuickActionConfig = QuickActionConfig.defaultLeft(),
    val rightQuickAction: QuickActionConfig = QuickActionConfig.defaultRight(),
    val batteryIndicatorMode: BatteryThresholdMode = BatteryThresholdMode.BELOW_50,
    val lastModified: Long = System.currentTimeMillis()
) {
    companion object {
        fun defaults() = LauncherSettings()
    }
}

data class QuickActionConfig(
    val packageName: String,
    val label: String,
    val isDefault: Boolean = true
) {
    companion object {
        fun defaultLeft() = QuickActionConfig(
            packageName = "com.google.android.dialer",
            label = "Phone",
            isDefault = true
        )
        fun defaultRight() = QuickActionConfig(
            packageName = "com.google.android.GoogleCamera",
            label = "Camera",
            isDefault = true
        )
    }
}

enum class BatteryThresholdMode {
    ALWAYS, BELOW_50, BELOW_20, NEVER;
    
    fun shouldShow(batteryPercent: Int): Boolean = when (this) {
        ALWAYS -> true
        BELOW_50 -> batteryPercent < 50
        BELOW_20 -> batteryPercent < 20
        NEVER -> false
    }
}
```

#### Step 3b: Create DataStore Data Source

**File**: `data/local/SettingsDataSource.kt`

```kotlin
interface SettingsDataSource {
    fun readSettings(): Flow<LauncherSettings>
    suspend fun writeSettings(settings: LauncherSettings)
    suspend fun clearSettings()
}
```

**File**: `data/local/SettingsDataSourceImpl.kt`

```kotlin
class SettingsDataSourceImpl(
    private val context: Context
) : SettingsDataSource {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "launcher_settings"
    )
    
    private object Keys {
        val AUTO_LAUNCH = booleanPreferencesKey("auto_launch_enabled")
        val LEFT_PACKAGE = stringPreferencesKey("left_quick_action_package")
        val LEFT_LABEL = stringPreferencesKey("left_quick_action_label")
        val RIGHT_PACKAGE = stringPreferencesKey("right_quick_action_package")
        val RIGHT_LABEL = stringPreferencesKey("right_quick_action_label")
        val BATTERY_MODE = stringPreferencesKey("battery_threshold_mode")
        val LAST_MODIFIED = longPreferencesKey("last_modified")
    }
    
    override fun readSettings(): Flow<LauncherSettings> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { prefs ->
                LauncherSettings(
                    autoLaunchEnabled = prefs[Keys.AUTO_LAUNCH] ?: true,
                    leftQuickAction = QuickActionConfig(
                        packageName = prefs[Keys.LEFT_PACKAGE] ?: "com.google.android.dialer",
                        label = prefs[Keys.LEFT_LABEL] ?: "Phone",
                        isDefault = prefs[Keys.LEFT_PACKAGE] == null
                    ),
                    rightQuickAction = QuickActionConfig(
                        packageName = prefs[Keys.RIGHT_PACKAGE] ?: "com.google.android.GoogleCamera",
                        label = prefs[Keys.RIGHT_LABEL] ?: "Camera",
                        isDefault = prefs[Keys.RIGHT_PACKAGE] == null
                    ),
                    batteryIndicatorMode = prefs[Keys.BATTERY_MODE]?.let { 
                        BatteryThresholdMode.valueOf(it)
                    } ?: BatteryThresholdMode.BELOW_50,
                    lastModified = prefs[Keys.LAST_MODIFIED] ?: System.currentTimeMillis()
                )
            }
    }
    
    override suspend fun writeSettings(settings: LauncherSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_LAUNCH] = settings.autoLaunchEnabled
            prefs[Keys.LEFT_PACKAGE] = settings.leftQuickAction.packageName
            prefs[Keys.LEFT_LABEL] = settings.leftQuickAction.label
            prefs[Keys.RIGHT_PACKAGE] = settings.rightQuickAction.packageName
            prefs[Keys.RIGHT_LABEL] = settings.rightQuickAction.label
            prefs[Keys.BATTERY_MODE] = settings.batteryIndicatorMode.name
            prefs[Keys.LAST_MODIFIED] = System.currentTimeMillis()
        }
    }
    
    override suspend fun clearSettings() {
        context.dataStore.edit { it.clear() }
    }
}
```

#### Step 3c: Implement Repository

**File**: `domain/repository/SettingsRepository.kt`

```kotlin
interface SettingsRepository {
    fun getSettings(): Flow<LauncherSettings>
    suspend fun updateSettings(settings: LauncherSettings): Result<Unit>
    suspend fun resetToDefaults(): Result<Unit>
}
```

**File**: `data/repository/SettingsRepositoryImpl.kt`

```kotlin
class SettingsRepositoryImpl(
    private val dataSource: SettingsDataSource,
    private val appRepository: AppRepository,
    private val logger: SettingsLogger
) : SettingsRepository {
    
    override fun getSettings(): Flow<LauncherSettings> {
        return dataSource.readSettings().map { settings ->
            validateAndCorrect(settings)
        }
    }
    
    override suspend fun updateSettings(settings: LauncherSettings): Result<Unit> {
        return try {
            val validated = validateAndCorrect(settings)
            dataSource.writeSettings(validated)
            logger.log("Settings updated: $validated")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.log("Failed to update settings: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun resetToDefaults(): Result<Unit> {
        return try {
            dataSource.clearSettings()
            logger.log("Settings reset to defaults")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.log("Failed to reset settings: ${e.message}")
            Result.failure(e)
        }
    }
    
    private suspend fun validateAndCorrect(settings: LauncherSettings): LauncherSettings {
        val installedApps = appRepository.getInstalledApps().first()
        val installedPackages = installedApps.map { it.packageName }.toSet()
        
        val leftValid = settings.leftQuickAction.packageName in installedPackages
        val rightValid = settings.rightQuickAction.packageName in installedPackages
        
        if (!leftValid || !rightValid) {
            logger.log("Quick action app(s) uninstalled, reverting to defaults")
        }
        
        return settings.copy(
            leftQuickAction = if (leftValid) settings.leftQuickAction else QuickActionConfig.defaultLeft(),
            rightQuickAction = if (rightValid) settings.rightQuickAction else QuickActionConfig.defaultRight()
        )
    }
}
```

---

### 4. Create Use Cases

**File**: `domain/usecase/LoadSettingsUseCase.kt`

```kotlin
class LoadSettingsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<LauncherSettings> = repository.getSettings()
}
```

**File**: `domain/usecase/SaveSettingsUseCase.kt`

```kotlin
class SaveSettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(settings: LauncherSettings): Result<Unit> {
        return repository.updateSettings(settings)
    }
}
```

**File**: `domain/usecase/ResetSettingsUseCase.kt`

```kotlin
class ResetSettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.resetToDefaults()
    }
}
```

---

### 5. Implement UI Layer

#### Step 5a: Create ViewModel

**File**: `ui/settings/SettingsViewModel.kt`

```kotlin
class SettingsViewModel(
    private val loadSettingsUseCase: LoadSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase,
    private val appRepository: AppRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            loadSettingsUseCase().collect { settings ->
                _uiState.value = SettingsUiState.Loaded(settings)
            }
        }
    }
    
    fun updateAutoLaunch(enabled: Boolean) {
        viewModelScope.launch {
            val current = (_uiState.value as? SettingsUiState.Loaded)?.settings ?: return@launch
            saveSettingsUseCase(current.copy(autoLaunchEnabled = enabled))
        }
    }
    
    fun updateLeftQuickAction(app: App) {
        viewModelScope.launch {
            val current = (_uiState.value as? SettingsUiState.Loaded)?.settings ?: return@launch
            val config = QuickActionConfig(app.packageName, app.label, isDefault = false)
            saveSettingsUseCase(current.copy(leftQuickAction = config))
        }
    }
    
    fun updateRightQuickAction(app: App) {
        viewModelScope.launch {
            val current = (_uiState.value as? SettingsUiState.Loaded)?.settings ?: return@launch
            val config = QuickActionConfig(app.packageName, app.label, isDefault = false)
            saveSettingsUseCase(current.copy(rightQuickAction = config))
        }
    }
    
    fun updateBatteryMode(mode: BatteryThresholdMode) {
        viewModelScope.launch {
            val current = (_uiState.value as? SettingsUiState.Loaded)?.settings ?: return@launch
            saveSettingsUseCase(current.copy(batteryIndicatorMode = mode))
        }
    }
    
    fun resetToDefaults() {
        viewModelScope.launch {
            resetSettingsUseCase()
        }
    }
}

sealed interface SettingsUiState {
    object Loading : SettingsUiState
    data class Loaded(val settings: LauncherSettings) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}
```

#### Step 5b: Create SettingsActivity

**File**: `ui/settings/SettingsActivity.kt`

```kotlin
class SettingsActivity : ComponentActivity() {
    
    private val viewModel: SettingsViewModel by viewModels {
        // Use dependency injection or manual factory
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MinimalistLauncherTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
```

**Add to AndroidManifest.xml**:
```xml
<activity
    android:name=".ui.settings.SettingsActivity"
    android:label="Settings"
    android:theme="@style/Theme.MinimalistLauncher"
    android:exported="false" />
```

#### Step 5c: Create Settings UI

**File**: `ui/settings/components/SettingsScreen.kt`

```kotlin
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is SettingsUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SettingsUiState.Loaded -> {
                SettingsContent(
                    settings = state.settings,
                    onAutoLaunchChange = viewModel::updateAutoLaunch,
                    onLeftQuickActionClick = { /* Show app picker */ },
                    onRightQuickActionClick = { /* Show app picker */ },
                    onBatteryModeChange = viewModel::updateBatteryMode,
                    onResetClick = viewModel::resetToDefaults,
                    modifier = Modifier.padding(padding)
                )
            }
            is SettingsUiState.Error -> {
                // Show error state
            }
        }
    }
}

@Composable
private fun SettingsContent(
    settings: LauncherSettings,
    onAutoLaunchChange: (Boolean) -> Unit,
    onLeftQuickActionClick: () -> Unit,
    onRightQuickActionClick: () -> Unit,
    onBatteryModeChange: (BatteryThresholdMode) -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            SwitchPreference(
                title = "Auto-launch apps",
                description = "Automatically open app when search returns single result",
                checked = settings.autoLaunchEnabled,
                onCheckedChange = onAutoLaunchChange
            )
        }
        
        item { Divider() }
        
        item {
            PreferenceItem(
                title = "Left quick action",
                description = settings.leftQuickAction.label,
                onClick = onLeftQuickActionClick
            )
        }
        
        item {
            PreferenceItem(
                title = "Right quick action",
                description = settings.rightQuickAction.label,
                onClick = onRightQuickActionClick
            )
        }
        
        item { Divider() }
        
        item {
            RadioGroupPreference(
                title = "Battery indicator",
                options = BatteryThresholdMode.values().toList(),
                selected = settings.batteryIndicatorMode,
                onSelect = onBatteryModeChange
            )
        }
        
        item { Divider() }
        
        item {
            PreferenceItem(
                title = "Reset to defaults",
                description = "Restore all settings to default values",
                onClick = onResetClick
            )
        }
    }
}
```

---

### 6. Integrate with HomeScreen

**File**: `ui/home/HomeScreen.kt` - Add long-press handler:

```kotlin
val context = LocalContext.current

Box(
    modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                }
            )
        }
) {
    // Existing home screen content
}
```

**File**: `ui/home/HomeViewModel.kt` - Load settings:

```kotlin
class HomeViewModel(
    // ... existing dependencies
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    val settings: StateFlow<LauncherSettings> = settingsRepository
        .getSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherSettings.defaults())
    
    // Use settings.value.autoLaunchEnabled for auto-launch logic
    // Use settings.value.batteryIndicatorMode for battery visibility
}
```

**File**: `ui/home/components/QuickActionButtons.kt` - Use custom apps:

```kotlin
@Composable
fun QuickActionButtons(
    leftAction: QuickActionConfig,
    rightAction: QuickActionConfig,
    onLeftClick: (String) -> Unit,  // Pass package name
    onRightClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionButton(
            config = leftAction,
            onClick = { onLeftClick(leftAction.packageName) }
        )
        
        QuickActionButton(
            config = rightAction,
            onClick = { onRightClick(rightAction.packageName) }
        )
    }
}
```

---

## Testing

### Unit Tests

Run unit tests:
```bash
./gradlew testDebugUnitTest
```

Key test files:
- `SettingsDataSourceImplTest.kt` - DataStore read/write
- `SettingsRepositoryImplTest.kt` - Validation logic
- `LoadSettingsUseCaseTest.kt` - Use case tests
- `SettingsViewModelTest.kt` - ViewModel state

### Instrumented Tests

Run on device/emulator:
```bash
./gradlew connectedDebugAndroidTest
```

Key test files:
- `SettingsActivityTest.kt` - UI interactions
- `SettingsIntegrationTest.kt` - End-to-end flows

---

## Debugging

### View DataStore Contents

Use Android Studio's App Inspection:
1. Run app on device
2. View → Tool Windows → App Inspection
3. Select app process
4. Navigate to DataStore viewer
5. View `launcher_settings.preferences_pb`

### Enable Logging

Add to `SettingsLogger.kt`:
```kotlin
class SettingsLogger {
    fun log(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("Settings", message)
        }
    }
}
```

View logs:
```bash
adb logcat | grep "Settings:"
```

---

## Common Issues

### Issue: Settings not persisting
**Solution**: Check DataStore directory permissions, verify DataStore instance is singleton

### Issue: App picker showing no apps
**Solution**: Verify AppRepository is returning launchable apps, check intent filter

### Issue: Quick action buttons not updating
**Solution**: Ensure Flow is being collected, check StateFlow emission

---

## Next Steps

After implementing core functionality:
1. Add app picker dialog with search
2. Implement reset confirmation dialog  
3. Add haptic feedback to interactions
4. Add analytics/logging for settings changes
5. Write comprehensive tests

---

## Resources

- [DataStore Documentation](https://developer.android.com/topic/libraries/architecture/datastore)
- [Jetpack Compose State](https://developer.android.com/jetpack/compose/state)
- [Material3 Components](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Feature Spec](./spec.md)
- [Data Model](./data-model.md)
- [Repository Contract](./contracts/settings-repository.md)
