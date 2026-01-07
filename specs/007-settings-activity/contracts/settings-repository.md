# Settings Repository Contract

**Feature**: Launcher Settings Activity  
**Date**: 2026-01-07

## Overview

This document defines the contract between the Settings domain layer (use cases) and the data layer (repository implementation). This contract ensures that the settings persistence layer can be tested, mocked, and potentially swapped without affecting business logic.

---

## Interface: SettingsRepository

**Location**: `app/src/main/java/com/symbianx/minimalistlauncher/domain/repository/SettingsRepository.kt`

### Purpose
Provides access to launcher settings with reactive updates, validation, and persistence guarantees.

### Methods

#### getSettings(): Flow<LauncherSettings>

**Description**: Returns a Flow that emits the current settings and all future updates.

**Signature**:
```kotlin
fun getSettings(): Flow<LauncherSettings>
```

**Returns**: 
- `Flow<LauncherSettings>` - Hot flow that emits current settings immediately and updates on changes

**Behavior**:
- Emits current settings immediately on collection
- Emits updated settings whenever changed via `updateSettings()`
- Always emits valid settings (defaults if none exist)
- Validates quick action app installations on each emission
- Never emits null or throws exceptions
- Safe to collect multiple times

**Guarantees**:
- First emission within 100ms (cached or from DataStore)
- Subsequent emissions immediate (in-memory updates)
- Settings are always internally consistent
- Quick action packages always reference installed apps

**Example Usage**:
```kotlin
class HomeViewModel(private val settingsRepository: SettingsRepository) {
    val settings: StateFlow<LauncherSettings> = settingsRepository
        .getSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, LauncherSettings.defaults())
}
```

---

#### updateSettings(settings: LauncherSettings): Result<Unit>

**Description**: Persists updated settings atomically.

**Signature**:
```kotlin
suspend fun updateSettings(settings: LauncherSettings): Result<Unit>
```

**Parameters**:
- `settings: LauncherSettings` - Complete settings object with all fields

**Returns**:
- `Result.success(Unit)` if save succeeded
- `Result.failure(Exception)` if save failed

**Behavior**:
- Validates settings before saving (checks installed apps)
- Writes atomically to DataStore (all-or-nothing)
- Updates lastModified timestamp automatically
- Triggers Flow emission to all observers
- Fails fast if validation fails

**Guarantees**:
- Write completes within 50ms under normal conditions
- If write fails, previous settings remain unchanged
- All observers notified within 10ms of successful write
- No partial updates (transaction semantics)

**Validation Rules**:
- `leftQuickAction.packageName` must be installed or defaults applied
- `rightQuickAction.packageName` must be installed or defaults applied
- `batteryIndicatorMode` must be valid enum value

**Example Usage**:
```kotlin
viewModelScope.launch {
    val result = settingsRepository.updateSettings(
        currentSettings.copy(autoLaunchEnabled = false)
    )
    result.onFailure { error ->
        _uiState.value = UiState.Error("Failed to save: ${error.message}")
    }
}
```

---

#### resetToDefaults(): Result<Unit>

**Description**: Resets all settings to their default values.

**Signature**:
```kotlin
suspend fun resetToDefaults(): Result<Unit>
```

**Returns**:
- `Result.success(Unit)` if reset succeeded
- `Result.failure(Exception)` if reset failed

**Behavior**:
- Clears all DataStore entries
- Next read returns default values
- Triggers Flow emission with defaults
- Logs reset event

**Guarantees**:
- Reset completes within 50ms
- Defaults match LauncherSettings.defaults() exactly
- All observers notified immediately
- Idempotent (safe to call multiple times)

**Example Usage**:
```kotlin
fun onResetConfirmed() {
    viewModelScope.launch {
        settingsRepository.resetToDefaults().onSuccess {
            _uiState.value = UiState.Success("Settings reset to defaults")
        }
    }
}
```

---

## Interface: SettingsDataSource

**Location**: `app/src/main/java/com/symbianx/minimalistlauncher/data/local/SettingsDataSource.kt`

### Purpose
Low-level persistence layer that handles DataStore read/write operations.

### Methods

#### readSettings(): Flow<LauncherSettings>

**Description**: Reads settings from DataStore and maps to domain model.

**Signature**:
```kotlin
fun readSettings(): Flow<LauncherSettings>
```

**Returns**:
- `Flow<LauncherSettings>` - Emits settings from DataStore

**Behavior**:
- Maps DataStore Preferences to LauncherSettings
- Returns defaults for missing keys
- Handles corrupted data gracefully
- Does NOT validate app installations (repository's job)

---

#### writeSettings(settings: LauncherSettings)

**Description**: Writes settings to DataStore atomically.

**Signature**:
```kotlin
suspend fun writeSettings(settings: LauncherSettings)
```

**Parameters**:
- `settings: LauncherSettings` - Settings to persist

**Throws**:
- `IOException` if write fails

**Behavior**:
- Serializes settings to DataStore keys
- Uses transaction for atomicity
- Overwrites all keys (not incremental)

---

#### clearSettings()

**Description**: Removes all settings from DataStore.

**Signature**:
```kotlin
suspend fun clearSettings()
```

**Behavior**:
- Clears all settings keys
- Used by resetToDefaults()

---

## Contract Tests

### Repository Contract Tests
**Location**: `app/src/test/.../data/repository/SettingsRepositoryContractTest.kt`

Required test scenarios:
1. **Default settings on first read**: No DataStore file → emits defaults
2. **Settings persistence**: Write settings → read settings → values match
3. **Reactive updates**: Collect Flow → update settings → Flow emits update
4. **Uninstalled app recovery**: Settings with uninstalled app → emits defaults for that slot
5. **Reset to defaults**: Reset → Flow emits defaults → DataStore cleared
6. **Concurrent reads**: Multiple collectors → all receive same updates
7. **Invalid enum value**: Corrupted mode → defaults to BELOW_50
8. **Transaction rollback**: Write fails → previous settings maintained

### DataSource Contract Tests
**Location**: `app/src/test/.../data/local/SettingsDataSourceContractTest.kt`

Required test scenarios:
1. **Read missing keys**: Empty DataStore → returns defaults
2. **Write/read round-trip**: Write settings → read → exact match
3. **Clear all keys**: Clear → read → returns defaults
4. **Corrupted file**: Corrupted DataStore → fallback handler invoked
5. **All key types**: Boolean, String, Long all serialize correctly

---

## Error Handling Contract

### Repository Layer Errors

| Error | Cause | Handling |
|-------|-------|----------|
| Uninstalled App | Quick action app not installed | Auto-revert to default, log warning |
| Invalid Enum | Unknown BatteryThresholdMode | Default to BELOW_50, log warning |
| Write Failure | Disk error, permission denied | Return Result.failure, settings unchanged |
| Corrupted Data | Invalid DataStore file | Clear file, return defaults, log error |

### Data Source Layer Errors

| Error | Cause | Handling |
|-------|-------|----------|
| IOException | File system error | Throw IOException (caught by repository) |
| DataStoreException | Protobuf serialization error | Clear file, return defaults |

---

## Performance Contract

| Operation | Max Time | Notes |
|-----------|----------|-------|
| First read (cold) | 100ms | Includes DataStore file read |
| First read (cached) | 10ms | DataStore in-memory cache |
| Write | 50ms | Includes fsync |
| Flow emission | 10ms | From write to observer notification |
| Validation | 20ms | Check installed apps |
| Reset | 50ms | Clear DataStore |

---

## Thread Safety Contract

- All repository methods are thread-safe
- DataStore handles concurrent access internally
- Flow emissions are serialized (no race conditions)
- Multiple collectors receive consistent state
- Repository holds no mutable state (stateless)

---

## Testing Mocks

### Fake Repository for UI Tests
```kotlin
class FakeSettingsRepository : SettingsRepository {
    private val _settings = MutableStateFlow(LauncherSettings.defaults())
    
    override fun getSettings(): Flow<LauncherSettings> = _settings
    
    override suspend fun updateSettings(settings: LauncherSettings): Result<Unit> {
        _settings.value = settings
        return Result.success(Unit)
    }
    
    override suspend fun resetToDefaults(): Result<Unit> {
        _settings.value = LauncherSettings.defaults()
        return Result.success(Unit)
    }
}
```

### Mock DataSource for Repository Tests
```kotlin
class MockSettingsDataSource : SettingsDataSource {
    private val _data = MutableStateFlow(LauncherSettings.defaults())
    var writeException: Exception? = null
    
    override fun readSettings(): Flow<LauncherSettings> = _data
    
    override suspend fun writeSettings(settings: LauncherSettings) {
        writeException?.let { throw it }
        _data.value = settings
    }
    
    override suspend fun clearSettings() {
        _data.value = LauncherSettings.defaults()
    }
}
```

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-07 | Initial contract definition |

---

## Notes

- This contract is technology-agnostic (could swap DataStore for Room, etc.)
- Repository hides persistence implementation from use cases
- Flow-based API enables reactive UI with minimal boilerplate
- Result type makes error handling explicit
- Contract tests ensure implementations are interchangeable
