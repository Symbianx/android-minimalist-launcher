# Data Model: Minimalist Android Launcher

**Feature**: Minimalist Android Launcher  
**Branch**: 001-minimalist-launcher  
**Date**: 2025-12-19  
**Phase**: 1 - Design & Contracts

## Overview

This document defines the data entities and their relationships for the minimalist launcher application. All entities are designed to be lightweight, immutable where possible, and optimized for 120 FPS rendering.

## Entity Definitions

### 1. App

**Purpose**: Represents an installed, launchable application on the device.

**Fields**:
```kotlin
@Immutable
data class App(
    val packageName: String,           // Unique identifier (e.g., "com.android.chrome")
    val label: String,                 // Display name (e.g., "Chrome")
    val launchIntent: Intent,          // Intent to launch the app
    val isSystemApp: Boolean = false   // Filtered out in search
)
```

**Validation Rules**:
- `packageName` MUST NOT be empty
- `label` MUST NOT be empty
- `launchIntent` MUST have ACTION_MAIN and CATEGORY_LAUNCHER
- System apps (FLAG_SYSTEM) excluded from search results

**Source**: PackageManager API via `AppListDataSource`

**Lifecycle**: Loaded once on launcher startup, cached in memory, refreshed on app install/uninstall broadcasts

**Performance**: ~1KB per app, expected 100-300 apps = 100-300KB total

---

### 2. DeviceStatus

**Purpose**: Represents current device state displayed on home screen (time and battery).

**Fields**:
```kotlin
@Immutable
data class DeviceStatus(
    val currentTime: String,           // Formatted time (e.g., "2:45 PM" or "14:45")
    val batteryPercentage: Int,        // Battery level 0-100
    val isCharging: Boolean = false    // Battery charging state
)
```

**Validation Rules**:
- `currentTime` MUST match device locale and 12/24-hour preference
- `batteryPercentage` MUST be in range 0-100
- Format uses `DateFormat.getTimeInstance()`

**Source**: System BroadcastReceivers (TIME_TICK, BATTERY_CHANGED) via `DeviceStatusRepository`

**Lifecycle**: Updates every minute (time) and on battery change events

**Performance**: <100 bytes, updates do not trigger full recomposition (Compose State)

---

### 3. NowPlayingInfo

**Purpose**: Represents currently detected ambient music from Pixel's Now Playing feature.

**Fields**:
```kotlin
@Immutable
data class NowPlayingInfo(
    val songName: String?,             // Song title (null if no song detected)
    val artistName: String?,           // Artist name (null if unavailable)
    val timestamp: Long,               // Detection timestamp (epochMillis)
    val isAvailable: Boolean = true    // False if Now Playing disabled/unavailable
)
```

**Validation Rules**:
- Both `songName` and `artistName` can be null (no music detected)
- `timestamp` used for staleness detection (>5 minutes = considered stale)
- `isAvailable` false on non-Pixel devices or when feature disabled

**Source**: MediaStore ContentProvider via `NowPlayingDataSource`

**Lifecycle**: Observed via ContentObserver, updates when new song detected

**Performance**: <200 bytes, rare updates (song detection event)

**Display Logic**:
- If `songName` is null: Display nothing or "No music detected"
- If both present: Display "[songName] • [artistName]"
- If only `songName`: Display "[songName]"

---

### 4. SearchState

**Purpose**: Represents the current state of the app search functionality.

**Fields**:
```kotlin
data class SearchState(
    val query: String = "",                     // Current search text
    val results: List<App> = emptyList(),       // Filtered app results
    val isActive: Boolean = false,              // Search UI visible
    val isLoading: Boolean = false              // App list loading
)
```

**Validation Rules**:
- `query` trimmed and case-normalized for comparison
- `results` sorted alphabetically by `App.label`
- `results` limited to 50 apps for performance
- `isActive` true when search UI shown (after swipe gesture)

**Source**: Derived from user input and app list filtering in `HomeViewModel`

**Lifecycle**: Ephemeral, exists only while launcher active

**State Transitions**:
1. **Inactive → Active**: User swipes right-to-left
2. **Active → Inactive**: User presses back button or launches app
3. **Query Change**: User types, triggers filter operation

**Performance**: Query filtering <100ms with debounce (150ms)

---

### 5. LauncherState

**Purpose**: Aggregates all home screen state into a single UI state object.

**Fields**:
```kotlin
@Stable
data class LauncherState(
    val deviceStatus: DeviceStatus,
    val nowPlaying: NowPlayingInfo,
    val searchState: SearchState,
    val appList: List<App> = emptyList()
)
```

**Validation Rules**:
- All child entities must be valid per their own rules
- State updates atomic (no partial updates)

**Source**: Composed in `HomeViewModel` from multiple repositories

**Lifecycle**: Exists while launcher activity active

**Performance**: Marked `@Stable` to minimize Compose recomposition

---

## Entity Relationships

```
┌─────────────────┐
│ LauncherState   │
└────────┬────────┘
         │
         ├─── DeviceStatus
         │
         ├─── NowPlayingInfo
         │
         ├─── SearchState
         │     └─── List<App>
         │
         └─── List<App> (all installed)
```

**Relationship Rules**:
- `LauncherState` is the single source of truth for UI
- `SearchState.results` is a filtered subset of `LauncherState.appList`
- All entities immutable (except SearchState for Compose State)
- Updates flow unidirectionally: Repository → ViewModel → State → UI

---

## State Management Strategy

### ViewModel State Flow

```kotlin
class HomeViewModel : ViewModel() {
    
    // State flows from repositories
    private val _deviceStatus = MutableStateFlow(DeviceStatus("", 0))
    private val _nowPlaying = MutableStateFlow(NowPlayingInfo(null, null, 0L))
    private val _appList = MutableStateFlow<List<App>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _isSearchActive = MutableStateFlow(false)
    
    // Combined UI state
    val launcherState: StateFlow<LauncherState> = combine(
        _deviceStatus,
        _nowPlaying,
        _appList,
        _searchQuery,
        _isSearchActive
    ) { deviceStatus, nowPlaying, appList, query, isActive ->
        LauncherState(
            deviceStatus = deviceStatus,
            nowPlaying = nowPlaying,
            searchState = SearchState(
                query = query,
                results = filterApps(appList, query),
                isActive = isActive
            ),
            appList = appList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherState(
            DeviceStatus("", 0),
            NowPlayingInfo(null, null, 0L),
            SearchState()
        )
    )
    
    private fun filterApps(apps: List<App>, query: String): List<App> {
        if (query.isBlank()) return emptyList()
        return apps
            .filter { it.label.contains(query, ignoreCase = true) }
            .sortedBy { it.label }
            .take(50)
    }
}
```

### Performance Characteristics

| Entity | Size | Update Frequency | Recomposition Impact |
|--------|------|------------------|---------------------|
| App | 1KB | Once (startup) | None (immutable list) |
| DeviceStatus | 100B | Every minute | Low (only status bar) |
| NowPlayingInfo | 200B | Per song detection | Low (only now playing view) |
| SearchState | Variable | Per keystroke | High (entire search UI) |
| LauncherState | ~500KB | Per update | Medium (smart recomposition) |

**Optimization**: Use `@Stable` and `@Immutable` annotations to prevent unnecessary recomposition.

---

## Data Persistence

### SharedPreferences (Minimal)

**Stored Data**:
- None required for MVP

**Rationale**: Launcher state is ephemeral, all data sourced from system APIs

**Future Consideration**:
- User preferences (e.g., custom app order, hidden apps)
- Would use DataStore (modern SharedPreferences replacement)

---

## Data Flow Diagram

```
┌──────────────────┐
│  System APIs     │
│  - PackageManager│
│  - BatteryManager│
│  - ContentProvider
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  Data Sources    │
│  - AppListDS     │
│  - BatteryDS     │
│  - NowPlayingDS  │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  Repositories    │
│  - AppRepo       │
│  - DeviceRepo    │
│  - NowPlayingRepo│
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  ViewModel       │
│  - Combine flows │
│  - Filter search │
│  - Expose State  │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  Compose UI      │
│  - Observe State │
│  - Render 120FPS │
│  - Handle gestures
└──────────────────┘
```

---

## Testing Strategy

### Unit Tests

**App Entity**:
- Validate packageName not empty
- Validate label not empty
- Test system app filtering

**SearchState**:
- Test filtering logic with various queries
- Test case-insensitive matching
- Test result limiting (50 apps max)
- Test empty query returns empty results

**DeviceStatus**:
- Test battery percentage range (0-100)
- Test time formatting (12/24 hour)

**NowPlayingInfo**:
- Test null handling for no song
- Test staleness detection (timestamp > 5 min)
- Test availability flag

### Integration Tests

**State Flow**:
- Test combined state updates correctly
- Test search query triggers filtering
- Test app list updates propagate to search results

**Repository Integration**:
- Test app list loads from PackageManager
- Test battery updates from system broadcasts
- Test Now Playing updates from ContentProvider

---

## Memory Budget

| Component | Estimated Size | Notes |
|-----------|---------------|-------|
| App list (200 apps) | 200KB | Loaded once, cached |
| DeviceStatus | 100B | Single instance |
| NowPlayingInfo | 200B | Single instance |
| SearchState | 10KB | Filtered list + query |
| ViewModel overhead | 5MB | Compose state management |
| **Total** | **~6MB** | Well within 30MB target |

**Actual measured**: Use Android Studio Memory Profiler to verify <30MB total app memory.

---

## Change Log

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-12-19 | Initial data model definition |

---

## Next Steps

1. Implement entities in `domain/model/` package
2. Create repositories in `domain/repository/`
3. Create data sources in `data/system/`
4. Wire up in ViewModel with StateFlow
5. Write unit tests for all entities
