# Research: Launcher Settings Activity

**Date**: 2026-01-07  
**Feature**: Settings Activity for Android Minimalist Launcher

## Overview

This document captures research findings for implementing a settings activity that manages launcher behavior preferences including auto-launch, quick action buttons, battery indicator visibility, and reset functionality.

## Key Technical Decisions

### 1. Settings Persistence Storage

**Decision**: Use DataStore Preferences (androidx.datastore:datastore-preferences)

**Rationale**:
- **Modern Android best practice**: DataStore is Google's recommended replacement for SharedPreferences
- **Type-safe**: Uses Kotlin Flows for reactive updates
- **Coroutine-based**: Naturally integrates with suspend functions and Compose
- **Atomic operations**: Ensures consistency with transaction-based updates
- **Error handling**: Built-in corruption handlers and migration support
- **Existing pattern**: Project already uses similar patterns with FavoritesDataSourceImpl

**Alternatives Considered**:
- **SharedPreferences**: Legacy API, not coroutine-friendly, no type safety, but project uses it for favorites
  - *Rejected*: DataStore is the modern replacement and provides better safety
- **Room Database**: Overkill for simple key-value settings
  - *Rejected*: Too heavyweight for settings that don't need relational structure
- **Proto DataStore**: Type-safe with protocol buffers
  - *Rejected*: Preferences DataStore is simpler for flat key-value pairs

**Implementation Notes**:
```kotlin
// Create DataStore instance
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_settings")

// Define keys
val AUTO_LAUNCH_ENABLED = booleanPreferencesKey("auto_launch_enabled")
val LEFT_QUICK_ACTION_PACKAGE = stringPreferencesKey("left_quick_action_package")
val RIGHT_QUICK_ACTION_PACKAGE = stringPreferencesKey("right_quick_action_package")
val BATTERY_THRESHOLD_MODE = stringPreferencesKey("battery_threshold_mode")

// Read settings
val settings = context.settingsDataStore.data.first()

// Write settings
context.settingsDataStore.edit { preferences ->
    preferences[AUTO_LAUNCH_ENABLED] = true
}
```

---

### 2. Settings UI Architecture

**Decision**: Use Jetpack Compose with Material3 components in a dedicated SettingsActivity

**Rationale**:
- **Consistency**: Matches existing UI architecture (HomeScreen is fully Compose)
- **Material3 ready**: Can use ListItem, Switch, and Dialog components
- **Reactive**: Flow-based state management integrates naturally with DataStore
- **Separation**: Dedicated Activity keeps settings isolated from home screen complexity
- **Navigation**: Simple startActivity() intent from home screen long-press

**Alternatives Considered**:
- **Navigation Component with Compose**: Add settings as a composable destination
  - *Rejected*: Home screen doesn't use Navigation; adding it would increase complexity
- **XML-based Preference fragments**: PreferenceFragment with XML
  - *Rejected*: Would mix UI paradigms; project is fully Compose
- **Bottom sheet in HomeScreen**: Settings in modal bottom sheet
  - *Rejected*: Too many settings for a bottom sheet; dedicated screen is clearer

**UI Components**:
- Use Material3 `Switch` for boolean toggles (auto-launch)
- Use Material3 `ListItem` with clickable modifier for navigation items
- Use Material3 `AlertDialog` for confirmation dialogs (reset)
- Custom `AppPickerDialog` with LazyColumn for app selection
- Custom `RadioButtonGroup` for battery threshold options

---

### 3. Quick Action App Selection

**Decision**: Full-screen dialog with searchable app list using existing AppRepository

**Rationale**:
- **Reuse logic**: AppRepository already loads and filters installed apps
- **Familiar UX**: Mirrors app search but focused on selection
- **Icons included**: Apps already have icon drawables loaded
- **Filter system apps**: Can exclude system apps user can't/shouldn't launch
- **Search capability**: For users with many apps installed

**Alternatives Considered**:
- **System app picker intent**: ACTION_PICK_ACTIVITY
  - *Rejected*: Less control over filtering and UX consistency
- **Dropdown/Spinner**: Standard Android picker
  - *Rejected*: Poor UX for large app lists; no search

**Implementation Pattern**:
```kotlin
@Composable
fun AppPickerDialog(
    onAppSelected: (App) -> Unit,
    onDismiss: () -> Unit
) {
    val apps by viewModel.installedApps.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Column {
            SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })
            LazyColumn {
                items(apps.filter { it.label.contains(searchQuery, ignoreCase = true) }) { app ->
                    AppPickerItem(app = app, onClick = { onAppSelected(app) })
                }
            }
        }
    }
}
```

---

### 4. Settings Access Entry Point

**Decision**: Long-press on home screen background opens settings menu

**Rationale**:
- **Minimal UI**: Keeps home screen clean without settings icon/button
- **Discoverable**: Long-press is standard Android gesture for contextual actions
- **Launcher pattern**: Common in launchers (Nova, Action, Lawnchair all use this)
- **Existing gesture**: Project already uses long-press on app items for favorites

**Alternatives Considered**:
- **Settings button in home screen**: Visible icon
  - *Rejected*: Violates minimalist design principle
- **Swipe gesture**: Down or diagonal swipe
  - *Rejected*: Conflicts with notification drawer (swipe down) and gestures (swipe left/right)
- **Hidden tap area**: Tap specific corner or region
  - *Rejected*: Not discoverable enough

**Implementation**:
```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { 
                    // Show settings menu or launch SettingsActivity
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                }
            )
        }
)
```

---

### 5. Battery Indicator Threshold Configuration

**Decision**: Enum-based threshold modes (ALWAYS, BELOW_50, BELOW_20, NEVER)

**Rationale**:
- **Simple choices**: Most users want fixed thresholds, not custom percentages
- **Type-safe**: Enum prevents invalid values
- **Clear options**: Each mode has clear meaning
- **Storage efficient**: Store enum name as string in DataStore

**Alternatives Considered**:
- **Custom percentage slider**: User picks exact threshold
  - *Rejected*: Overcomplicates UI; most users want common values
- **Multiple booleans**: show_always, show_when_low, etc.
  - *Rejected*: Mutually exclusive states shouldn't be separate booleans

**Implementation**:
```kotlin
enum class BatteryThresholdMode {
    ALWAYS,      // Always visible
    BELOW_50,    // Show when battery < 50%
    BELOW_20,    // Show when battery < 20%
    NEVER;       // Never show
    
    fun shouldShow(batteryPercent: Int): Boolean {
        return when (this) {
            ALWAYS -> true
            BELOW_50 -> batteryPercent < 50
            BELOW_20 -> batteryPercent < 20
            NEVER -> false
        }
    }
}
```

---

### 6. Handling Uninstalled Quick Action Apps

**Decision**: Detect on settings load, revert to default, log event, show toast

**Rationale**:
- **Graceful degradation**: Don't crash or show broken UI
- **User feedback**: Toast notification explains what happened
- **Sensible default**: Phone and Camera are safe defaults
- **Automatic recovery**: No user action needed
- **Observable**: Log for debugging

**Alternatives Considered**:
- **Keep broken reference**: Show placeholder or empty
  - *Rejected*: Confusing UX; user can't launch nothing
- **Prompt user**: Ask to pick replacement
  - *Rejected*: Interrupts user flow; auto-recovery is smoother

**Implementation Pattern**:
```kotlin
suspend fun validateQuickActionApps(leftPackage: String, rightPackage: String): Pair<String, String> {
    val installedPackages = appRepository.getInstalledApps().map { it.packageName }
    
    val validLeft = if (leftPackage in installedPackages) leftPackage else DEFAULT_PHONE_PACKAGE
    val validRight = if (rightPackage in installedPackages) rightPackage else DEFAULT_CAMERA_PACKAGE
    
    if (validLeft != leftPackage || validRight != rightPackage) {
        logger.log("Quick action app(s) uninstalled, reverted to defaults")
        // Show toast notification
    }
    
    return Pair(validLeft, validRight)
}
```

---

### 7. Reset to Defaults Strategy

**Decision**: Clear all DataStore entries, confirmation dialog required

**Rationale**:
- **Clean slate**: Clearing DataStore triggers default values in code
- **Safety**: AlertDialog prevents accidental resets
- **Simple**: No need to enumerate and set each default value
- **Consistent**: Default values defined once in data source

**Implementation**:
```kotlin
suspend fun resetToDefaults() {
    context.settingsDataStore.edit { preferences ->
        preferences.clear()
    }
    // Defaults are returned by getters when keys don't exist
}
```

---

## Dependencies Required

**New Dependencies**:
```kotlin
// DataStore Preferences
implementation("androidx.datastore:datastore-preferences:1.1.1")
```

**Existing Dependencies** (already in project):
- Jetpack Compose (UI)
- Material3 (components)
- Kotlinx Coroutines (async operations)
- ViewModel (state management)

---

## Integration Points

### Home Screen Changes
1. Add long-press gesture detection to HomeScreen background
2. Modify HomeViewModel to load settings from SettingsRepository
3. Update QuickActionButtons to use custom app packages from settings
4. Update CircularBatteryIndicator to check threshold mode setting
5. Pass settings state down to child components

### Auto-Launch Feature (Spec 002)
1. Read `autoLaunchEnabled` setting in SearchView
2. Conditionally trigger auto-launch based on setting value
3. Existing auto-launch logic remains unchanged if enabled

### Battery Indicator (Spec 005)
1. Read `batteryThresholdMode` in HomeViewModel
2. Pass threshold check to CircularBatteryIndicator visibility logic
3. Indicator animation and styling remain unchanged

---

## Testing Strategy

### Unit Tests
- SettingsDataSource read/write operations
- Default value handling
- Corruption recovery (invalid data)
- Settings validation (uninstalled apps)
- Reset to defaults functionality

### Integration Tests
- Settings persist across app restarts
- Changes reflect in home screen immediately
- Multiple rapid changes handled correctly
- DataStore file I/O under load

### UI Tests
- Open settings from long-press
- Toggle switches update state
- App picker selection flow
- Reset confirmation dialog
- Settings activity back navigation

---

## Performance Considerations

1. **DataStore reads**: Cache in ViewModel to avoid repeated reads
2. **App list loading**: Reuse AppRepository, already optimized
3. **Settings writes**: DataStore handles atomicity and debouncing
4. **UI updates**: Flow-based state propagates efficiently
5. **Memory**: Settings model is small (<1KB), negligible impact

---

## Security & Privacy

- **No sensitive data**: Settings are app preferences, not user data
- **Local storage only**: DataStore files in app private directory
- **No network**: All operations offline
- **No permissions**: Settings don't require runtime permissions

---

## Migration & Backwards Compatibility

**Current State**: No existing settings system

**Migration Path**: N/A - First settings implementation

**Default Behavior**: When keys don't exist in DataStore:
- Auto-launch: Enabled (maintains current behavior)
- Left quick action: Phone (com.google.android.dialer)
- Right quick action: Camera (com.google.android.GoogleCamera)
- Battery threshold: BELOW_50 (current behavior)

---

## Open Questions / Future Enhancements

1. **Export/Import settings**: Allow backup/restore of settings (future)
2. **Cloud sync**: Sync settings across devices (out of scope)
3. **Per-app settings**: Different behavior per app (not needed now)
4. **Accessibility settings**: Font size, contrast (future consideration)

---

## References

- [DataStore Documentation](https://developer.android.com/topic/libraries/architecture/datastore)
- [Material3 Components](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Compose State Management](https://developer.android.com/jetpack/compose/state)
- [Android Settings Best Practices](https://developer.android.com/guide/topics/ui/settings)
