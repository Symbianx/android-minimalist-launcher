# Internal Contracts: Repository Interfaces

**Feature**: Minimalist Android Launcher  
**Branch**: 001-minimalist-launcher  
**Date**: 2025-12-19

## Overview

This document defines the internal contracts (interfaces) between the domain layer and data layer. All repositories expose Kotlin Flows for reactive updates.

---

## AppRepository

**Purpose**: Manages the list of installed launchable applications.

### Interface

```kotlin
interface AppRepository {
    
    /**
     * Retrieves all installed launchable applications.
     * Excludes system apps.
     * 
     * @return Flow emitting list of apps, sorted alphabetically
     * @throws SecurityException if QUERY_ALL_PACKAGES permission denied
     */
    fun getInstalledApps(): Flow<List<App>>
    
    /**
     * Launches the specified application.
     * 
     * @param app The application to launch
     * @return Result indicating success or failure
     */
    suspend fun launchApp(app: App): Result<Unit>
    
    /**
     * Refreshes the app list (called when package installed/uninstalled).
     * 
     * @return Result indicating success or failure with new app list
     */
    suspend fun refreshAppList(): Result<List<App>>
}
```

### Expected Behavior

- `getInstalledApps()`: Emits on subscription, then on package changes
- Apps sorted alphabetically by label (case-insensitive)
- System apps filtered out (ApplicationInfo.FLAG_SYSTEM)
- Updates automatically via BroadcastReceiver for PACKAGE_ADDED/REMOVED
- Thread-safe (uses Dispatchers.IO for PackageManager calls)

### Error Handling

- SecurityException: Permission denied (API 30+)
- ActivityNotFoundException: App uninstalled between load and launch
- IllegalStateException: PackageManager unavailable

### Performance Expectations

- Initial load: <200ms for 200 apps
- Refresh: <100ms (incremental update)
- Memory: ~1KB per app

---

## DeviceStatusRepository

**Purpose**: Provides current device time and battery status.

### Interface

```kotlin
interface DeviceStatusRepository {
    
    /**
     * Observes device status updates (time and battery).
     * Emits immediately with current status, then on changes.
     * 
     * @return Flow emitting DeviceStatus updates
     */
    fun observeDeviceStatus(): Flow<DeviceStatus>
}
```

### Expected Behavior

- Emits immediately with current time and battery level
- Time updates every minute (ACTION_TIME_TICK broadcast)
- Battery updates on level changes (ACTION_BATTERY_CHANGED)
- Format respects system locale and 12/24-hour preference
- Automatically unregisters receivers when Flow cancelled

### Error Handling

- No exceptions thrown (system broadcasts always available)
- Defaults to "00:00" and 0% if broadcasts not received

### Performance Expectations

- Update latency: <10ms from broadcast to emission
- Memory: <1KB for broadcast receivers
- Battery impact: Negligible (system optimized)

---

## NowPlayingRepository

**Purpose**: Accesses Pixel's Now Playing ambient music detection data.

### Interface

```kotlin
interface NowPlayingRepository {
    
    /**
     * Observes Now Playing song detection updates.
     * Emits immediately with latest detected song, then on changes.
     * 
     * @return Flow emitting NowPlayingInfo updates
     */
    fun observeNowPlaying(): Flow<NowPlayingInfo>
    
    /**
     * Opens the Now Playing history activity.
     * 
     * @return Result indicating success or failure
     */
    suspend fun openNowPlayingHistory(): Result<Unit>
    
    /**
     * Checks if Now Playing is available on this device.
     * 
     * @return true if available, false otherwise
     */
    fun isNowPlayingAvailable(): Boolean
}
```

### Expected Behavior

- `observeNowPlaying()`: Emits immediately with latest song (or null)
- Updates via ContentObserver on ambient_music_tracks URI
- Returns null song/artist if no detection or feature disabled
- `isNowPlayingAvailable()`: Checks ContentProvider existence
- `openNowPlayingHistory()`: Launches Google Quick Search Box activity

### Error Handling

- ContentProvider unavailable: Returns NowPlayingInfo(isAvailable = false)
- Permission denied: Returns null song data, doesn't crash
- Activity not found: Returns failure Result for openNowPlayingHistory()

### Performance Expectations

- Query latency: <50ms (ContentProvider optimized)
- Update frequency: Only on song detection (few per hour)
- Graceful degradation on non-Pixel devices

---

## Use Cases

**Purpose**: Encapsulate business logic for specific user actions.

### SearchAppsUseCase

```kotlin
interface SearchAppsUseCase {
    
    /**
     * Filters apps by search query.
     * 
     * @param apps The complete list of apps
     * @param query The search text (case-insensitive)
     * @return Filtered and sorted list of matching apps (max 50)
     */
    suspend fun execute(apps: List<App>, query: String): List<App>
}
```

**Implementation Notes**:
- Case-insensitive partial matching
- Returns empty list if query blank
- Limits to 50 results for performance
- Sorting: alphabetical by label

### LaunchAppUseCase

```kotlin
interface LaunchAppUseCase {
    
    /**
     * Launches the specified app.
     * 
     * @param app The app to launch
     * @return Result indicating success or failure
     */
    suspend fun execute(app: App): Result<Unit>
}
```

**Implementation Notes**:
- Delegates to AppRepository
- Adds analytics/logging (if needed)
- Handles ActivityNotFoundException gracefully

---

## Data Source Interfaces

**Purpose**: Abstract system API access for testing.

### AppListDataSource

```kotlin
interface AppListDataSource {
    
    /**
     * Queries PackageManager for launchable apps.
     * 
     * @return List of apps with launch intents
     */
    suspend fun queryInstalledApps(): List<App>
    
    /**
     * Registers listener for package install/uninstall events.
     * 
     * @param onPackageChanged Callback invoked on package change
     */
    fun registerPackageChangeListener(onPackageChanged: () -> Unit)
    
    /**
     * Unregisters package change listener.
     */
    fun unregisterPackageChangeListener()
}
```

### BatteryDataSource

```kotlin
interface BatteryDataSource {
    
    /**
     * Observes battery level changes.
     * 
     * @return Flow emitting battery percentage (0-100) and charging state
     */
    fun observeBatteryLevel(): Flow<Pair<Int, Boolean>>
}
```

### NowPlayingDataSource

```kotlin
interface NowPlayingDataSource {
    
    /**
     * Queries Now Playing ContentProvider for latest song.
     * 
     * @return NowPlayingInfo or null if unavailable
     */
    suspend fun queryLatestSong(): NowPlayingInfo?
    
    /**
     * Observes Now Playing updates via ContentObserver.
     * 
     * @return Flow emitting song updates
     */
    fun observeSongUpdates(): Flow<NowPlayingInfo?>
    
    /**
     * Checks if ContentProvider is available.
     * 
     * @return true if accessible, false otherwise
     */
    fun isAvailable(): Boolean
}
```

---

## Contract Testing Strategy

Each interface will have contract tests verifying:

1. **Happy path**: Normal operation with valid data
2. **Error handling**: Graceful degradation on failures
3. **Edge cases**: Empty lists, null values, permission denials
4. **Performance**: Operations complete within expected time
5. **Thread safety**: Concurrent access doesn't corrupt state

Example:
```kotlin
@Test
fun `getInstalledApps returns non-empty list on first emission`() = runTest {
    val apps = appRepository.getInstalledApps().first()
    assertTrue(apps.isNotEmpty())
    assertTrue(apps.all { it.label.isNotBlank() })
}

@Test
fun `launchApp succeeds for valid app`() = runTest {
    val app = App("com.android.chrome", "Chrome", intent, false)
    val result = appRepository.launchApp(app)
    assertTrue(result.isSuccess)
}
```

---

## Dependency Injection

**Strategy**: Constructor injection (manual, no framework)

```kotlin
class HomeViewModel(
    private val appRepository: AppRepository,
    private val deviceStatusRepository: DeviceStatusRepository,
    private val nowPlayingRepository: NowPlayingRepository,
    private val searchAppsUseCase: SearchAppsUseCase,
    private val launchAppUseCase: LaunchAppUseCase
) : ViewModel() {
    // ...
}
```

**Justification**: Simple app with few dependencies, no need for Dagger/Hilt complexity.

**Testing**: Easy to mock interfaces for unit tests.

---

## Thread Safety Guarantees

- All repository methods thread-safe (use appropriate dispatchers)
- Flows emit on Dispatchers.Main (safe for Compose)
- State updates atomic (StateFlow ensures consistency)
- ContentObserver callbacks marshalled to main thread

---

## Change Log

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-12-19 | Initial contract definitions |
