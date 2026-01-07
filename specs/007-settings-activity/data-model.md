# Data Model: Launcher Settings Activity

**Date**: 2026-01-07  
**Feature**: Settings Activity for Android Minimalist Launcher

## Core Entities

### LauncherSettings

The primary aggregate representing all launcher configuration preferences.

**Attributes**:
- `autoLaunchEnabled: Boolean` - Whether single search result auto-launch is active
- `leftQuickAction: QuickActionConfig` - Configuration for bottom-left quick action button
- `rightQuickAction: QuickActionConfig` - Configuration for bottom-right quick action button
- `batteryIndicatorMode: BatteryThresholdMode` - When to show battery indicator
- `lastModified: Long` - Timestamp of last settings change (for debugging)

**Default Values**:
- autoLaunchEnabled: `true` (maintains current behavior)
- leftQuickAction: QuickActionConfig with Phone dialer
- rightQuickAction: QuickActionConfig with Camera
- batteryIndicatorMode: `BELOW_50` (current behavior)

**Validation Rules**:
- leftQuickAction and rightQuickAction must reference installed apps
- If app is uninstalled, revert to default for that slot
- batteryIndicatorMode must be valid enum value

**State Lifecycle**:
- Created with defaults on first app launch
- Updated via SettingsActivity user interactions
- Persisted immediately on each change to DataStore
- Loaded on HomeViewModel initialization
- Validated on every load (check for uninstalled apps)

---

### QuickActionConfig

Configuration for a single quick action button (left or right).

**Attributes**:
- `packageName: String` - Android package name of the target app
- `label: String` - Display name of the app (for UI preview)
- `isDefault: Boolean` - Whether this is the default app (not user-customized)

**Default Instances**:
- Left: Phone dialer (`com.google.android.dialer` or device default)
- Right: Camera (`com.google.android.GoogleCamera` or device default)

**Validation Rules**:
- packageName must not be empty
- packageName must correspond to launchable installed app
- If app is uninstalled, reset to default for that position

**Relationships**:
- Referenced by LauncherSettings (composition)
- Resolved using AppRepository to get full App details
- Used by QuickActionButtons component for rendering and launch

---

### BatteryThresholdMode (Enum)

Defines when the battery indicator should be visible on the home screen.

**Values**:
- `ALWAYS` - Indicator always visible regardless of battery level
- `BELOW_50` - Indicator visible when battery < 50%
- `BELOW_20` - Indicator visible when battery < 20% (low battery warning)
- `NEVER` - Indicator never displayed

**Behavior**:
```kotlin
fun shouldShow(batteryPercent: Int): Boolean {
    return when (this) {
        ALWAYS -> true
        BELOW_50 -> batteryPercent < 50
        BELOW_20 -> batteryPercent < 20
        NEVER -> false
    }
}
```

**Relationships**:
- Referenced by LauncherSettings
- Used by CircularBatteryIndicator to control visibility
- Combined with BatteryDataSource battery percentage reading

---

### SettingsState (UI State)

UI-specific state for the SettingsActivity, separate from domain model.

**Attributes**:
- `currentSettings: LauncherSettings` - Current settings values
- `isLoading: Boolean` - Whether settings are being loaded
- `showAppPicker: AppPickerTarget?` - Which quick action button is being configured (null if picker not shown)
- `showResetDialog: Boolean` - Whether reset confirmation dialog is visible
- `installedApps: List<App>` - Available apps for quick action selection
- `errorMessage: String?` - Error message to display (e.g., "Failed to save settings")

**State Transitions**:
- `Idle` → `Loading` (on screen enter)
- `Loading` → `Loaded(settings)` (settings retrieved)
- `Loaded` → `Saving` (user changes setting)
- `Saving` → `Loaded(updated)` (save complete)
- `Loaded` → `ShowingAppPicker(target)` (user taps quick action config)
- `ShowingAppPicker` → `Loaded(updated)` (user selects app or cancels)
- `Loaded` → `ShowingResetDialog` (user taps reset)
- `ShowingResetDialog` → `Loaded(defaults)` (user confirms reset)

---

### AppPickerTarget (Enum)

Indicates which quick action button is being configured in the app picker.

**Values**:
- `LEFT` - Configuring left quick action button
- `RIGHT` - Configuring right quick action button

**Usage**: Passed to AppPickerDialog to know which setting to update on selection.

---

## Data Flow Architecture

### Read Path (Loading Settings)

```
SettingsActivity
    ↓ (onCreate)
SettingsViewModel.loadSettings()
    ↓
LoadSettingsUseCase.execute()
    ↓
SettingsRepository.getSettings(): Flow<LauncherSettings>
    ↓
SettingsDataSource.readSettings(): Flow<Preferences>
    ↓ (maps to domain model)
DataStore<Preferences>
    ↓ (validates installed apps)
LauncherSettings with validated QuickActionConfigs
    ↓ (emit to UI)
SettingsState.Loaded(settings)
```

### Write Path (Saving Changes)

```
User interaction (toggle switch, select app)
    ↓
SettingsViewModel.updateSetting(change)
    ↓
SaveSettingsUseCase.execute(newSettings)
    ↓
SettingsRepository.updateSettings(settings)
    ↓
SettingsDataSource.writeSettings(settings)
    ↓ (atomic transaction)
DataStore.edit { preferences -> ... }
    ↓ (persisted to disk)
File: /data/data/{package}/files/datastore/launcher_settings.preferences_pb
    ↓ (triggers Flow update)
All observers receive updated settings
    ↓
HomeViewModel receives update
    ↓
UI recomposes with new settings
```

### Validation Path (Uninstalled Apps)

```
SettingsRepository.getSettings()
    ↓
SettingsDataSource.readSettings()
    ↓ (retrieves package names)
packageName from DataStore
    ↓
AppRepository.getInstalledApps()
    ↓ (check if package exists)
if (packageName !in installedApps) {
    log warning
    return default package
    save corrected settings
}
    ↓
Valid LauncherSettings returned
```

---

## Persistence Schema (DataStore Keys)

### Key Definitions

```kotlin
object SettingsKeys {
    val AUTO_LAUNCH_ENABLED = booleanPreferencesKey("auto_launch_enabled")
    val LEFT_QUICK_ACTION_PACKAGE = stringPreferencesKey("left_quick_action_package")
    val LEFT_QUICK_ACTION_LABEL = stringPreferencesKey("left_quick_action_label")
    val RIGHT_QUICK_ACTION_PACKAGE = stringPreferencesKey("right_quick_action_package")
    val RIGHT_QUICK_ACTION_LABEL = stringPreferencesKey("right_quick_action_label")
    val BATTERY_THRESHOLD_MODE = stringPreferencesKey("battery_threshold_mode")
    val LAST_MODIFIED = longPreferencesKey("last_modified")
}
```

### File Format

DataStore persists as binary Protocol Buffer file:
- Location: `{app_data}/files/datastore/launcher_settings.preferences_pb`
- Format: Binary protobuf (not human-readable)
- Size: ~200-500 bytes typical
- Atomic writes: Transactional updates prevent corruption

### Example Stored Values

```
{
  "auto_launch_enabled": true,
  "left_quick_action_package": "com.google.android.dialer",
  "left_quick_action_label": "Phone",
  "right_quick_action_package": "com.android.whatsapp",
  "right_quick_action_label": "WhatsApp",
  "battery_threshold_mode": "BELOW_50",
  "last_modified": 1704650400000
}
```

---

## Relationships Between Entities

### LauncherSettings ↔ QuickActionConfig
- **Type**: Composition (1:2)
- **Cardinality**: LauncherSettings owns exactly 2 QuickActionConfig instances
- **Lifecycle**: QuickActionConfigs are created/destroyed with LauncherSettings
- **Navigation**: settings.leftQuickAction, settings.rightQuickAction

### QuickActionConfig → App (via AppRepository)
- **Type**: Reference
- **Cardinality**: Each QuickActionConfig references 1 App by packageName
- **Lifecycle**: App may be uninstalled independently
- **Resolution**: `appRepository.getAppByPackage(config.packageName)`
- **Fallback**: If app not found, revert config to default

### LauncherSettings → BatteryThresholdMode
- **Type**: Enumeration
- **Cardinality**: LauncherSettings has exactly 1 BatteryThresholdMode
- **Behavior**: Mode defines visibility logic for battery indicator

### SettingsState → LauncherSettings
- **Type**: Reference (UI → Domain)
- **Cardinality**: SettingsState wraps 1 LauncherSettings instance
- **Lifecycle**: UI state may be null during loading
- **Updates**: UI state updates when domain model changes (Flow)

---

## Invariants & Business Rules

1. **Quick Action Apps Must Be Launchable**
   - Every QuickActionConfig.packageName must resolve to an installed, launchable app
   - If validation fails, automatically revert to default (Phone or Camera)
   - Log warning and optionally show toast to user

2. **Settings Must Always Exist**
   - First read initializes with defaults if DataStore empty
   - No null settings state in ViewModel after initialization
   - Repository returns Flow that always emits (at least defaults)

3. **Atomic Updates**
   - All setting changes must be atomic (DataStore.edit transaction)
   - No partial updates that leave settings inconsistent
   - If write fails, previous state maintained

4. **Default Values Are Constants**
   - Default phone package: Determined by device's default dialer
   - Default camera package: Determined by device's default camera
   - Fallback to known packages if device defaults unavailable
   - Constants defined in SettingsDefaults object

5. **Battery Threshold Mode Exclusive**
   - Only one mode active at a time (enum ensures this)
   - Mode change immediately affects indicator visibility
   - HomeViewModel observes settings changes and recomputes visibility

6. **Last Modified Timestamp**
   - Updated on every settings change
   - Used for debugging and potential future sync features
   - Not displayed to user in current version

---

## Error Handling

### Corrupted DataStore
- **Scenario**: File corruption, unexpected format
- **Handler**: DataStore ReplaceFileCorruptionHandler
- **Action**: Delete corrupted file, return defaults, log error

### Uninstalled App
- **Scenario**: User uninstalls app set as quick action
- **Detection**: On settings load, validate packages
- **Action**: Revert to default package, save correction, log warning, show toast

### Write Failure
- **Scenario**: Disk full, permission denied (unlikely in app storage)
- **Detection**: DataStore.edit throws exception
- **Action**: Show error message, settings unchanged, retry option

### Invalid Enum Value
- **Scenario**: Unknown BatteryThresholdMode string in DataStore
- **Detection**: Enum.valueOf() throws IllegalArgumentException
- **Action**: Default to BELOW_50, log warning, save corrected value

---

## Performance Characteristics

### Read Performance
- **Cold start**: ~10-50ms (file read + deserialization)
- **Cached**: <1ms (in-memory Flow cache)
- **Concurrent reads**: Thread-safe, no blocking

### Write Performance
- **Single setting**: ~5-20ms (serialize + atomic file write)
- **Multiple settings**: Same (batched in single transaction)
- **Frequency**: Writes are rare (user-initiated only)

### Memory Footprint
- **LauncherSettings object**: ~200 bytes
- **DataStore cache**: ~1KB
- **UI state**: ~500 bytes
- **Total**: <2KB (negligible)

---

## Migration Strategy

### Version 1 (Current)
- Initial implementation
- All settings defined above
- No migrations needed

### Future Versions
If settings schema changes:
1. Define migration in DataStore setup
2. Migrate old keys to new keys
3. Set version number in metadata
4. Provide rollback if migration fails

Example future migration:
```kotlin
val migration = object : DataMigration<Preferences> {
    override suspend fun migrate(currentData: Preferences): Preferences {
        return currentData.toMutablePreferences().apply {
            // Example: rename key
            this[NEW_KEY] = currentData[OLD_KEY] ?: DEFAULT
            this.remove(OLD_KEY)
        }
    }
}
```

---

## Testing Considerations

### Unit Test Scenarios
- Create LauncherSettings with defaults
- Validate quick action configs with missing apps
- BatteryThresholdMode.shouldShow() for all modes and percentages
- SettingsDataSource read/write round-trip
- Enum serialization to/from string

### Integration Test Scenarios
- Save settings, restart app, verify persistence
- Uninstall quick action app, reload settings, verify default restored
- Rapid setting changes, verify all applied atomically
- Corrupted DataStore file, verify recovery to defaults

### Property-Based Tests
- Any valid LauncherSettings serializes and deserializes identically
- BatteryThresholdMode.shouldShow() is monotonic within mode
- QuickActionConfig with valid package never fails validation

---

## Summary

The data model for Settings Activity follows clean architecture principles:
- **Domain models** (LauncherSettings, QuickActionConfig, BatteryThresholdMode) are pure Kotlin with no Android dependencies
- **Data layer** (SettingsDataSource) handles DataStore persistence with serialization
- **Repository** (SettingsRepository) bridges data and domain with validation logic
- **UI state** (SettingsState) wraps domain models with presentation concerns
- **Reactive updates** via Flow ensure UI always reflects current settings
- **Validation** ensures settings remain consistent even when apps are uninstalled
- **Defaults** provide sensible behavior when settings are missing

This model supports all functional requirements from the specification while maintaining testability, type safety, and performance.
