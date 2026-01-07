# Data Model: Usage Awareness

**Date**: 2025-12-22  
**Feature**: Usage Awareness (006)  
**Source**: Extracted from [spec.md](./spec.md) functional requirements

## Overview

This data model supports tracking phone unlocks and app launches on a daily basis with automatic midnight resets. All data is stored locally using SharedPreferences with JSON serialization (following existing `FavoritesDataSourceImpl` pattern).

## Core Entities

### 1. UnlockEvent

Represents a single phone unlock occurrence.

**Purpose**: Track individual unlock events for counting and timestamp recording.

**Fields**:
- `timestamp: Long` - Epoch milliseconds when unlock occurred
- `date: String` - ISO 8601 date string (e.g., "2025-12-22") for daily grouping

**Lifecycle**: Ephemeral - not persisted individually, only aggregated into `DailyUnlockSummary`

**Validation Rules**:
- `timestamp` must be > 0
- `date` must match `LocalDate.now().toString()` format

---

### 2. DailyUnlockSummary

Aggregates all unlock events for the current day.

**Purpose**: Provide daily unlock statistics for home screen display (FR-003, FR-004).

**Fields**:
- `date: String` - ISO 8601 date string (e.g., "2025-12-22")
- `unlockCount: Int` - Total unlocks today (resets at midnight)
- `lastUnlockTimestamp: Long` - Epoch millis of most recent unlock

**Storage**: Persisted in SharedPreferences as part of `UsageData` JSON

**Validation Rules**:
- `unlockCount` >= 0
- `lastUnlockTimestamp` >= 0 (0 means no unlocks today)
- `date` must be valid ISO 8601 date

**State Transitions**:
- Day boundary detected → reset `unlockCount = 0`, `lastUnlockTimestamp = 0`, update `date`
- Unlock event received → increment `unlockCount`, update `lastUnlockTimestamp`

---

### 3. AppLaunchEvent

Represents a single app launch from the launcher.

**Purpose**: Track individual app launch occurrences for counting and timestamps.

**Fields**:
- `packageName: String` - Unique app identifier (e.g., "com.android.chrome")
- `timestamp: Long` - Epoch milliseconds when launch occurred
- `date: String` - ISO 8601 date string for daily grouping

**Lifecycle**: Ephemeral - not persisted individually, only aggregated into `AppLaunchSummary`

**Validation Rules**:
- `packageName` must not be empty
- `timestamp` must be > 0
- `date` must match `LocalDate.now().toString()` format

---

### 4. AppLaunchSummary

Aggregates launch events for a single app on the current day.

**Purpose**: Provide per-app launch statistics for display when opening apps (FR-008, FR-013).

**Fields**:
- `packageName: String` - App identifier
- `launchCount: Int` - Total launches today (resets at midnight)
- `lastLaunchTimestamp: Long` - Epoch millis of most recent launch (0 if not launched today)

**Storage**: Stored in map within `UsageData` JSON (`Map<String, AppLaunchSummary>`)

**Validation Rules**:
- `packageName` must not be empty
- `launchCount` >= 0
- `lastLaunchTimestamp` >= 0 (0 means not launched today)

**State Transitions**:
- Day boundary detected → clear all entries (map becomes empty)
- App launched → if app in map: increment count, update timestamp; else: add new entry with count=1
- App not launched today → entry doesn't exist in map (implicit count=0)

---

### 5. UsageData (Root Storage Model)

Top-level data structure persisted in SharedPreferences.

**Purpose**: Container for all usage tracking data with date-based reset mechanism.

**Fields**:
- `currentDate: String` - ISO 8601 date of data (e.g., "2025-12-22")
- `unlockCount: Int` - Current day's unlock count
- `lastUnlockTimestamp: Long` - Most recent unlock timestamp
- `appLaunches: Map<String, AppUsageData>` - Per-app launch data (key = packageName)

**Nested Type - AppUsageData**:
```kotlin
data class AppUsageData(
    val launchCount: Int,
    val lastLaunchTimestamp: Long
)
```

**Storage Key**: `"usage_tracking_data"` in SharedPreferences

**Reset Logic** (Lazy Evaluation):
```
On READ:
  1. Deserialize JSON from SharedPreferences
  2. Compare stored.currentDate with LocalDate.now().toString()
  3. If different:
     - Reset unlockCount = 0
     - Reset lastUnlockTimestamp = 0
     - Clear appLaunches map
     - Update currentDate = today
  4. Return (possibly reset) data
```

**Validation Rules**:
- `currentDate` must be valid ISO 8601 date
- `unlockCount` >= 0
- `lastUnlockTimestamp` >= 0
- `appLaunches` keys must be non-empty strings
- All `AppUsageData.launchCount` >= 0
- All `AppUsageData.lastLaunchTimestamp` >= 0

---

### 6. UsageAwarenessState (UI State Model)

UI state for displaying usage information in Compose.

**Purpose**: Provide presentation-ready data for home screen and app launch overlays.

**Fields**:
- `unlockCountToday: Int` - Current day's unlock count
- `lastUnlockTimeAgo: String?` - Human-readable time (e.g., "2h ago", "just now", null if no unlocks)
- `appLaunchOverlay: AppLaunchOverlayState?` - Overlay state (null when not showing)

**Nested Type - AppLaunchOverlayState**:
```kotlin
data class AppLaunchOverlayState(
    val appName: String,              // Display name (e.g., "Chrome")
    val launchCount: Int,             // "8th time today"
    val lastLaunchTimeAgo: String?,   // "last opened 20m ago" or null if first today
    val visible: Boolean              // Controls overlay visibility
)
```

**Lifecycle**: Transient UI state in `HomeViewModel`, not persisted

---

## Relationships

```
UsageData (Storage Root)
├── unlockCount, lastUnlockTimestamp (DailyUnlockSummary data)
└── appLaunches: Map<packageName, AppUsageData>
    └── AppUsageData (per-app launch summary)

UnlockEvent (ephemeral) → aggregates into → DailyUnlockSummary
AppLaunchEvent (ephemeral) → aggregates into → AppLaunchSummary

DailyUnlockSummary + AppLaunchSummary → transforms into → UsageAwarenessState (UI)
```

---

## Data Flow

### Unlock Tracking Flow
```
1. System broadcasts ACTION_USER_PRESENT
2. BroadcastReceiver captures event
3. TrackUnlockUseCase called
4. Repository reads current UsageData
5. Check date: if today → increment count; if different day → reset + set count=1
6. Update lastUnlockTimestamp = now
7. Persist updated UsageData JSON to SharedPreferences
8. Emit updated state to HomeViewModel
9. HomeScreen recomposes with new unlock count display
```

### App Launch Tracking Flow
```
1. User taps app in launcher
2. TrackAppLaunchUseCase called with packageName
3. Repository reads current UsageData
4. Check date: if today → update app entry; if different day → reset all + create new entry
5. Update/create AppUsageData for this app (increment count, set lastLaunchTimestamp)
6. Persist updated UsageData JSON
7. GetUsageStatsUseCase retrieves app stats
8. HomeViewModel shows AppLaunchOverlay with stats (0.5-1 second)
9. After delay, dismiss overlay and fire app.launchIntent
```

---

## Serialization Schema

**Format**: JSON via `kotlinx-serialization`

**Example**:
```json
{
  "currentDate": "2025-12-22",
  "unlockCount": 12,
  "lastUnlockTimestamp": 1703259600000,
  "appLaunches": {
    "com.android.chrome": {
      "launchCount": 8,
      "lastLaunchTimestamp": 1703259300000
    },
    "com.google.android.gm": {
      "launchCount": 3,
      "lastLaunchTimestamp": 1703258800000
    }
  }
}
```

**Storage**: SharedPreferences key `"usage_tracking_data"`

**Corruption Handling**: If JSON deserialization fails, clear corrupted data and return fresh empty `UsageData` (graceful degradation per FR-018)

---

## Time Formatting

**Relative Time Display** (for UI):
- < 1 minute: "just now"
- 1-59 minutes: "Xm ago"
- 1-23 hours: "Xh ago"
- Different day: "first time today" (for apps) or reset counter (for unlocks)

**Implementation**: `TimeFormatter` utility class with `formatRelativeTime(timestampMillis: Long): String?`

---

## Edge Case Handling

| Scenario | Behavior |
|----------|----------|
| First app launch (ever) | No data exists → create new `UsageData` with currentDate = today |
| Midnight boundary during read | Date mismatch detected → reset all counts, update date |
| Device reboot | Data persisted in SharedPreferences → survives reboot |
| Storage write failure | Catch exception, log, proceed with app launch (don't block user) |
| Corrupted JSON | Clear data, start fresh with empty `UsageData` |
| Timezone change | `LocalDate.now()` adapts automatically, counts may reset early/late once |
| Clock set backwards | Date comparison still works (if date changes, reset; if same, keep) |

---

## Summary

- **3 Domain Models**: UnlockEvent, AppLaunchEvent, DailyUnlockSummary/AppLaunchSummary
- **1 Storage Model**: UsageData (with nested AppUsageData)
- **1 UI State Model**: UsageAwarenessState (with nested AppLaunchOverlayState)
- **Storage**: SharedPreferences + JSON (consistent with existing favorites pattern)
- **Reset Mechanism**: Lazy date-based check on read (no background jobs)
- **Privacy**: All data local, no cloud sync, current day only
