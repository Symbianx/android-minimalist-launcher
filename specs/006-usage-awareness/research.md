# Research: Usage Awareness Implementation

**Date**: 2025-12-22  
**Feature**: Usage Awareness (006)  
**Purpose**: Resolve technical unknowns from Technical Context

## Research Tasks

### 1. Detecting Screen Unlock Events Without USAGE_STATS Permission

**Question**: What Android API can detect when the phone is unlocked without requiring `android.permission.PACKAGE_USAGE_STATS` or invasive permissions?

**Research Findings**:

Android provides `ACTION_USER_PRESENT` broadcast intent that fires when the device is unlocked and the user is present at the screen. This is a system broadcast that does NOT require special permissions.

**Decision**: Use `ACTION_USER_PRESENT` broadcast receiver
- **Intent**: `Intent.ACTION_USER_PRESENT` 
- **Registration**: Register dynamically in `MainActivity.onCreate()` or application lifecycle
- **Trigger**: Fires after device unlock + screen on + keyguard dismissed
- **Permission**: None required (standard broadcast)

**Rationale**:
- No special permissions needed (meets FR-024 constraint)
- Reliable system broadcast available since API 1
- Perfect match for "conscious unlock" tracking (only fires when user actively unlocks)
- Lightweight - no polling or background services required

**Alternatives Considered**:
1. ❌ `UsageStatsManager` - Requires `PACKAGE_USAGE_STATS` permission (invasive, user must grant via Settings)
2. ❌ `SCREEN_ON` broadcast - Fires on screen wake, not actual unlock (includes accidental wakes)
3. ❌ `KeyguardManager.KeyguardLock` - Deprecated and requires device admin permissions
4. ✅ `ACTION_USER_PRESENT` - Selected (no permissions, accurate unlock detection)

**Implementation Notes**:
```kotlin
// Register in MainActivity or Application class
val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
registerReceiver(unlockReceiver, filter)

// Receiver increments unlock count in UsageTrackingRepository
private val unlockReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            // Trigger TrackUnlockUseCase
        }
    }
}
```

---

### 2. Data Structure for Daily Aggregation and Midnight Reset

**Question**: What data structure optimally supports daily event aggregation and automatic midnight resets?

**Research Findings**:

For daily usage tracking with midnight resets, the optimal approach is to store:
1. **Daily summary** with date key (e.g., "2025-12-22")
2. **Event timestamps** for "last" calculations
3. **Computed counts** that reset at midnight

**Decision**: Two-level data structure with date-keyed summaries

```kotlin
// Storage schema (JSON in SharedPreferences)
data class UsageData(
    val currentDate: String,              // "2025-12-22" (ISO 8601 date)
    val unlockCount: Int,                 // Resets daily
    val lastUnlockTimestamp: Long,        // Epoch millis
    val appLaunches: Map<String, AppUsageData>  // packageName -> usage data
)

data class AppUsageData(
    val launchCount: Int,                 // Resets daily
    val lastLaunchTimestamp: Long         // Epoch millis
)
```

**Rationale**:
- **Date key check**: On each read, compare stored `currentDate` with today's date
  - If different → reset all counts to 0, update date
  - If same → use existing counts
- **Simple reset logic**: No background jobs, no `AlarmManager`, no `WorkManager`
- **Instant access**: Single SharedPreferences read gets all data
- **Consistent pattern**: Matches existing `FavoritesDataSourceImpl` JSON serialization approach

**Alternatives Considered**:
1. ❌ SQLite Room database - Overkill for simple daily aggregates, adds complexity
2. ❌ Separate files per day - Requires cleanup logic, more IO operations
3. ❌ `AlarmManager` for midnight reset - Unreliable on modern Android (doze mode), unnecessary complexity
4. ✅ **Lazy date-based reset** - Selected (simple, reliable, no background tasks)

**Implementation Pattern**:
```kotlin
// Read operation
suspend fun getCurrentUsageData(): UsageData {
    val stored = prefs.getString(KEY_USAGE_DATA, null)?.let { json.decodeFromString<UsageData>(it) }
    val today = LocalDate.now().toString()  // "2025-12-22"
    
    return if (stored == null || stored.currentDate != today) {
        // Reset: new day or first run
        UsageData(currentDate = today, unlockCount = 0, lastUnlockTimestamp = 0, appLaunches = emptyMap())
    } else {
        stored
    }
}
```

---

### 3. Displaying Brief Overlays During App Launch

**Question**: How to show brief usage stats when launching an app without delaying the launch intent or requiring overlay permissions?

**Research Findings**:

Android launchers control the launch flow. We can display information **before** firing the launch intent, then launch after a brief delay. No overlay permission needed because we're still in our own launcher UI.

**Decision**: Modal Compose dialog with auto-dismiss + launch

**Approach**:
1. User taps app → Show `AppLaunchOverlay` Composable (modal in foreground)
2. Display usage stats for 500-1000ms
3. Auto-dismiss overlay + fire `startActivity(launchIntent)`
4. Launcher remains in background, app takes foreground

**Rationale**:
- **No overlay permission**: We're displaying in our own Activity before launching
- **Non-blocking**: Launch proceeds after timeout regardless of user interaction
- **Smooth UX**: Compose animation for fade-in/fade-out
- **Meets FR-021**: If display fails, launch still proceeds (try/catch around overlay show)

**Alternatives Considered**:
1. ❌ System overlay (`SYSTEM_ALERT_WINDOW`) - Requires invasive permission (violates FR-024)
2. ❌ Toast notification - Non-dismissible by code, no control over duration, poor UX
3. ❌ Snackbar - Requires Scaffold/anchoring, not suitable for full-screen launcher
4. ✅ **Modal Compose dialog with auto-dismiss** - Selected (no permissions, full control)

**Implementation Pattern**:
```kotlin
// In HomeViewModel or AppLaunchUseCase
fun launchAppWithAwareness(app: App) {
    val stats = getUsageStatsUseCase(app.packageName)
    
    // Show modal overlay
    _showAppLaunchOverlay.value = AppLaunchOverlayState(
        appName = app.label,
        launchCount = stats.launchCount,
        lastLaunchTime = stats.lastLaunchTimestamp,
        visible = true
    )
    
    // Auto-dismiss after 800ms and launch
    viewModelScope.launch {
        delay(800)
        _showAppLaunchOverlay.value = _showAppLaunchOverlay.value.copy(visible = false)
        context.startActivity(app.launchIntent)
    }
}
```

---

### 4. Data Retention Strategy

**Question**: Should we retain historical usage data beyond the current day, or only maintain current day counts?

**Research Findings**:

The feature spec focuses on **current day awareness only**:
- "unlocks today"
- "8th time today"
- "last opened 20m ago"
- Midnight resets mentioned explicitly (FR-005, FR-010, FR-015)

No requirement for historical trends, weekly summaries, or long-term tracking.

**Decision**: Current day only - no historical retention

**Data Lifecycle**:
- Store only today's counts + timestamps
- At midnight boundary (detected via date check), reset all counts to 0
- No historical data stored (privacy-first, minimal storage)

**Rationale**:
- **Meets all requirements**: Spec only requires current day awareness
- **Privacy-first**: No long-term surveillance, aligns with design philosophy
- **Minimal storage**: ~1-2 KB total (vs. 30+ KB for 30-day history)
- **Simple implementation**: No cleanup logic, no data migration
- **Fast**: Single read/write per operation

**Alternatives Considered**:
1. ❌ 7-day rolling window - Not required by spec, adds storage/complexity
2. ❌ 30-day history - Overkill, moves toward surveillance rather than awareness
3. ❌ Configurable retention - Adds settings UI, not justified by requirements
4. ✅ **Current day only** - Selected (sufficient for spec, privacy-aligned)

**Implementation Note**: If future features need trends, we can add optional historical tracking as a separate capability.

---

## Technology Stack Decisions

### Storage: SharedPreferences + kotlinx-serialization

**Choice**: `SharedPreferences` with `kotlinx-serialization` JSON encoding  
**Rationale**: Matches existing `FavoritesDataSourceImpl` pattern, simple for key-value data, no schema migrations needed  
**Libraries**: Already included in project (`libs.plugins.kotlin-serialization`)

### Time Handling: java.time.LocalDate and System.currentTimeMillis()

**Choice**: 
- `LocalDate.now()` for date boundaries (available API 26+, meets minSdk = 26)
- `System.currentTimeMillis()` for timestamps (epoch millis)

**Rationale**: Standard library, no additional dependencies, sufficient precision for usage tracking

### UI: Jetpack Compose

**Choice**: Pure Compose for all UI components  
**Rationale**: Existing project uses Compose, no View-based code needed

---

## Summary of Resolved Unknowns

| Unknown | Resolution | Key Decision |
|---------|-----------|--------------|
| Unlock detection API | `ACTION_USER_PRESENT` broadcast | No permissions required |
| Data structure | Date-keyed JSON with lazy reset | Check date on read, reset if changed |
| Brief app overlays | Modal Compose dialog with auto-dismiss | Display in launcher UI before starting app |
| Data retention | Current day only | Reset at midnight, no historical storage |

**All unknowns resolved. Ready for Phase 1 (Design & Contracts).**
