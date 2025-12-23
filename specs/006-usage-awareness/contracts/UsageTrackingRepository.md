# Repository Contract: UsageTrackingRepository

**Purpose**: Domain layer contract for usage tracking operations  
**Implementation**: `UsageTrackingRepositoryImpl` (data layer)

## Interface Definition

```kotlin
interface UsageTrackingRepository {
    
    /**
     * Records a phone unlock event.
     * Increments daily unlock counter and updates last unlock timestamp.
     * Automatically resets counters if day boundary crossed.
     * 
     * @return Updated daily unlock summary
     * @throws IOException if storage write fails (caller should handle gracefully)
     */
    suspend fun recordUnlock(): DailyUnlockSummary
    
    /**
     * Records an app launch event.
     * Increments app-specific launch counter and updates last launch timestamp.
     * Automatically resets counters if day boundary crossed.
     * 
     * @param packageName Unique app identifier (e.g., "com.android.chrome")
     * @return Updated app launch summary for this app
     * @throws IllegalArgumentException if packageName is empty
     * @throws IOException if storage write fails (caller should handle gracefully)
     */
    suspend fun recordAppLaunch(packageName: String): AppLaunchSummary
    
    /**
     * Retrieves current day's unlock statistics.
     * Automatically resets if day boundary crossed since last read.
     * 
     * @return Daily unlock summary (count + last timestamp)
     */
    suspend fun getDailyUnlockSummary(): DailyUnlockSummary
    
    /**
     * Retrieves launch statistics for a specific app.
     * Automatically resets if day boundary crossed since last read.
     * 
     * @param packageName App identifier
     * @return App launch summary (count + last timestamp), or default (count=0) if never launched
     */
    suspend fun getAppLaunchSummary(packageName: String): AppLaunchSummary
    
    /**
     * Clears all usage tracking data.
     * Used for testing or user-initiated data reset.
     */
    suspend fun clearAllData()
}
```

## Domain Models (from data-model.md)

```kotlin
/**
 * Daily unlock statistics.
 * 
 * @property date ISO 8601 date string (e.g., "2025-12-22")
 * @property unlockCount Total unlocks today
 * @property lastUnlockTimestamp Epoch millis of most recent unlock (0 if none today)
 */
data class DailyUnlockSummary(
    val date: String,
    val unlockCount: Int,
    val lastUnlockTimestamp: Long
)

/**
 * Per-app launch statistics.
 * 
 * @property packageName App identifier
 * @property launchCount Total launches today
 * @property lastLaunchTimestamp Epoch millis of most recent launch (0 if none today)
 */
data class AppLaunchSummary(
    val packageName: String,
    val launchCount: Int,
    val lastLaunchTimestamp: Long
)
```

## Behavior Contracts

### Day Boundary Reset

All methods that read or write data MUST check if the stored date matches today's date:
- If `storedDate != LocalDate.now().toString()`: reset all counts to 0, update date
- If `storedDate == LocalDate.now().toString()`: use existing data

This ensures automatic midnight resets without background jobs.

### Error Handling

- **Storage failures**: Methods throw `IOException` if SharedPreferences write fails
- **Corrupted data**: Silently clear and reinitialize with empty data
- **Invalid input**: Throw `IllegalArgumentException` for empty/null packageNames
- **Graceful degradation**: Callers (UseCases) must catch exceptions and proceed with app launch

### Thread Safety

All methods are `suspend` functions designed for Kotlin Coroutines with `Dispatchers.IO`.  
SharedPreferences access is synchronized internally by Android.

### Performance

- `recordUnlock()`: < 10ms (single SharedPreferences write)
- `recordAppLaunch()`: < 10ms (single SharedPreferences write)
- `getDailyUnlockSummary()`: < 5ms (single SharedPreferences read + date check)
- `getAppLaunchSummary()`: < 5ms (single SharedPreferences read + map lookup)

---

## Usage Examples

### Record Unlock (from BroadcastReceiver)

```kotlin
class UnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            // Launch coroutine to record unlock
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val summary = repository.recordUnlock()
                    Log.d(TAG, "Unlock recorded: ${summary.unlockCount} today")
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to record unlock", e)
                    // Continue - don't disrupt user
                }
            }
        }
    }
}
```

### Record App Launch (from LaunchAppUseCase)

```kotlin
suspend fun launchApp(app: App): Result<AppLaunchSummary> {
    return try {
        // Record launch first
        val summary = repository.recordAppLaunch(app.packageName)
        
        // Return success with stats
        Result.success(summary)
    } catch (e: IOException) {
        Log.e(TAG, "Failed to track launch", e)
        // Return default stats, still allow launch
        Result.success(AppLaunchSummary(app.packageName, 0, 0))
    }
}
```

### Get Stats for Display

```kotlin
suspend fun getHomeScreenState(): UsageAwarenessState {
    val unlockSummary = repository.getDailyUnlockSummary()
    
    return UsageAwarenessState(
        unlockCountToday = unlockSummary.unlockCount,
        lastUnlockTimeAgo = TimeFormatter.formatRelativeTime(unlockSummary.lastUnlockTimestamp),
        appLaunchOverlay = null
    )
}
```

---

## Testing Contract

Implementations MUST support:
- **Mocking**: Interface can be mocked for unit tests
- **Test doubles**: Provide in-memory test implementation (no SharedPreferences dependency)
- **Date injection**: Allow overriding "now" for testing day boundary logic

Example test double:
```kotlin
class FakeUsageTrackingRepository : UsageTrackingRepository {
    private var currentDate: String = LocalDate.now().toString()
    private var unlockCount: Int = 0
    private var lastUnlockTimestamp: Long = 0
    private val appLaunches = mutableMapOf<String, Pair<Int, Long>>()
    
    fun setTestDate(date: String) { currentDate = date }
    
    // ... implement interface methods with in-memory storage
}
```
