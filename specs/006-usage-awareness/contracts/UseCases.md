# Use Case Contracts: Usage Tracking

**Purpose**: Domain layer use cases for usage awareness feature  
**Pattern**: Single-responsibility use cases following existing codebase conventions

---

## TrackUnlockUseCase

### Interface

```kotlin
interface TrackUnlockUseCase {
    /**
     * Records a phone unlock event and returns updated statistics.
     * Handles storage errors gracefully (logs error, returns default on failure).
     * 
     * @return Daily unlock summary (never throws)
     */
    suspend operator fun invoke(): DailyUnlockSummary
}
```

### Behavior

- Called when `ACTION_USER_PRESENT` broadcast received
- Delegates to `UsageTrackingRepository.recordUnlock()`
- Catches and logs `IOException` (graceful degradation)
- Returns default `DailyUnlockSummary(date=today, count=0, timestamp=0)` on error

### Implementation Note

```kotlin
class TrackUnlockUseCaseImpl(
    private val repository: UsageTrackingRepository
) : TrackUnlockUseCase {
    override suspend fun invoke(): DailyUnlockSummary {
        return try {
            repository.recordUnlock()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to record unlock", e)
            // Return default summary on failure
            DailyUnlockSummary(
                date = LocalDate.now().toString(),
                unlockCount = 0,
                lastUnlockTimestamp = 0
            )
        }
    }
}
```

---

## TrackAppLaunchUseCase

### Interface

```kotlin
interface TrackAppLaunchUseCase {
    /**
     * Records an app launch event and returns updated statistics for that app.
     * Handles storage errors gracefully (logs error, returns default on failure).
     * 
     * @param packageName App identifier (e.g., "com.android.chrome")
     * @return App launch summary (never throws)
     */
    suspend operator fun invoke(packageName: String): AppLaunchSummary
}
```

### Behavior

- Called when user taps app in launcher (before showing overlay)
- Validates `packageName` is not empty
- Delegates to `UsageTrackingRepository.recordAppLaunch()`
- Catches and logs `IOException` (graceful degradation)
- Returns default `AppLaunchSummary(packageName, count=0, timestamp=0)` on error
- **Critical**: Must not block app launch on failure

### Implementation Note

```kotlin
class TrackAppLaunchUseCaseImpl(
    private val repository: UsageTrackingRepository
) : TrackAppLaunchUseCase {
    override suspend fun invoke(packageName: String): AppLaunchSummary {
        require(packageName.isNotEmpty()) { "packageName cannot be empty" }
        
        return try {
            repository.recordAppLaunch(packageName)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to record app launch", e)
            // Return default summary on failure
            AppLaunchSummary(
                packageName = packageName,
                launchCount = 0,
                lastLaunchTimestamp = 0
            )
        }
    }
}
```

---

## GetUsageStatsUseCase

### Interface

```kotlin
interface GetUsageStatsUseCase {
    /**
     * Retrieves complete usage statistics for home screen display.
     * Includes unlock summary and prepares for app launch overlays.
     * 
     * @return Usage awareness state with formatted display strings
     */
    suspend fun getHomeScreenStats(): UsageAwarenessState
    
    /**
     * Retrieves usage statistics for a specific app (for overlay display).
     * 
     * @param packageName App identifier
     * @param appName Display name for UI
     * @return App launch overlay state with formatted display strings
     */
    suspend fun getAppStats(packageName: String, appName: String): AppLaunchOverlayState
}
```

### Behavior

#### getHomeScreenStats()

- Retrieves `DailyUnlockSummary` from repository
- Formats timestamps using `TimeFormatter.formatRelativeTime()`
- Returns `UsageAwarenessState` for home screen display
- Never throws - returns default state on error

#### getAppStats()

- Retrieves `AppLaunchSummary` for specific app from repository
- Formats launch count ("8th time today", "1st time today")
- Formats last launch time ("last opened 20m ago", null if first today)
- Returns `AppLaunchOverlayState` for brief display
- Never throws - returns default state on error

### Implementation Note

```kotlin
class GetUsageStatsUseCaseImpl(
    private val repository: UsageTrackingRepository,
    private val timeFormatter: TimeFormatter
) : GetUsageStatsUseCase {
    
    override suspend fun getHomeScreenStats(): UsageAwarenessState {
        return try {
            val summary = repository.getDailyUnlockSummary()
            UsageAwarenessState(
                unlockCountToday = summary.unlockCount,
                lastUnlockTimeAgo = timeFormatter.formatRelativeTime(summary.lastUnlockTimestamp),
                appLaunchOverlay = null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get usage stats", e)
            UsageAwarenessState(0, null, null)
        }
    }
    
    override suspend fun getAppStats(packageName: String, appName: String): AppLaunchOverlayState {
        return try {
            val summary = repository.getAppLaunchSummary(packageName)
            AppLaunchOverlayState(
                appName = appName,
                launchCount = summary.launchCount,
                lastLaunchTimeAgo = if (summary.lastLaunchTimestamp > 0) {
                    timeFormatter.formatRelativeTime(summary.lastLaunchTimestamp)
                } else null,
                visible = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get app stats", e)
            AppLaunchOverlayState(appName, 0, null, visible = true)
        }
    }
}
```

---

## Domain Models (Reference)

```kotlin
// From data-model.md

data class UsageAwarenessState(
    val unlockCountToday: Int,
    val lastUnlockTimeAgo: String?,  // "2h ago", "just now", null if no unlocks
    val appLaunchOverlay: AppLaunchOverlayState?
)

data class AppLaunchOverlayState(
    val appName: String,
    val launchCount: Int,
    val lastLaunchTimeAgo: String?,  // "last opened 20m ago", null if first today
    val visible: Boolean
)

data class DailyUnlockSummary(
    val date: String,
    val unlockCount: Int,
    val lastUnlockTimestamp: Long
)

data class AppLaunchSummary(
    val packageName: String,
    val launchCount: Int,
    val lastLaunchTimestamp: Long
)
```

---

## Error Handling Policy

All use cases follow **graceful degradation**:

1. **Try**: Perform operation via repository
2. **Catch**: Log error with context
3. **Return**: Default/empty state (never throw to UI layer)
4. **Result**: User experience continues uninterrupted

This ensures **FR-021**: "Usage data displays MUST never block user actions"

---

## Testing Requirements

Each use case must have unit tests covering:

1. **Success path**: Repository returns valid data → use case transforms correctly
2. **Repository failure**: Repository throws IOException → use case returns default state
3. **Edge cases**: Empty data, midnight boundary, corrupted data recovery
4. **Time formatting**: Timestamps converted to human-readable strings

Use fake repository implementations for testing (no SharedPreferences dependency).
