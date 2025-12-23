# Quickstart: Usage Awareness Implementation

**Feature**: Usage Awareness (006)  
**Branch**: `006-usage-awareness`  
**Prerequisites**: Android Studio, Kotlin 2.3.0, API 26+

## Overview

Implement usage tracking to display unlock counts and app launch frequency in the launcher. This guide follows the prioritized implementation order (P1 → P2 → P3).

---

## Phase 1: Daily Unlock Tracking (P1) 🎯

**Goal**: Display unlock count and last unlock time on home screen.

### 1.1 Create Domain Models

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/domain/model/DailyUnlockSummary.kt`

```kotlin
package com.symbianx.minimalistlauncher.domain.model

data class DailyUnlockSummary(
    val date: String,              // ISO 8601 date (e.g., "2025-12-22")
    val unlockCount: Int,          // Total unlocks today
    val lastUnlockTimestamp: Long  // Epoch millis (0 if no unlocks)
)
```

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/domain/model/AppLaunchSummary.kt`

```kotlin
package com.symbianx.minimalistlauncher.domain.model

data class AppLaunchSummary(
    val packageName: String,
    val launchCount: Int,
    val lastLaunchTimestamp: Long
)
```

### 1.2 Create Storage Models

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/data/local/UsageData.kt`

```kotlin
package com.symbianx.minimalistlauncher.data.local

import kotlinx.serialization.Serializable

@Serializable
data class UsageData(
    val currentDate: String,
    val unlockCount: Int = 0,
    val lastUnlockTimestamp: Long = 0,
    val appLaunches: Map<String, AppUsageData> = emptyMap()
)

@Serializable
data class AppUsageData(
    val launchCount: Int,
    val lastLaunchTimestamp: Long
)
```

### 1.3 Implement Data Source

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/data/local/UsageTrackingDataSource.kt`

```kotlin
package com.symbianx.minimalistlauncher.data.local

interface UsageTrackingDataSource {
    suspend fun saveUsageData(data: UsageData)
    suspend fun loadUsageData(): UsageData
    suspend fun clearUsageData()
}
```

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/data/local/UsageTrackingDataSourceImpl.kt`

```kotlin
package com.symbianx.minimalistlauncher.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

class UsageTrackingDataSourceImpl(context: Context) : UsageTrackingDataSource {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    override suspend fun saveUsageData(data: UsageData) = withContext(Dispatchers.IO) {
        val jsonString = json.encodeToString(data)
        prefs.edit().putString(KEY_USAGE_DATA, jsonString).apply()
    }
    
    override suspend fun loadUsageData(): UsageData = withContext(Dispatchers.IO) {
        val jsonString = prefs.getString(KEY_USAGE_DATA, null)
        val today = LocalDate.now().toString()
        
        if (jsonString == null) {
            // First run - return fresh data
            return@withContext UsageData(currentDate = today)
        }
        
        try {
            val stored = json.decodeFromString<UsageData>(jsonString)
            
            // Check day boundary - reset if different day
            if (stored.currentDate != today) {
                UsageData(currentDate = today)
            } else {
                stored
            }
        } catch (e: Exception) {
            // Corrupted data - clear and start fresh
            clearUsageData()
            UsageData(currentDate = today)
        }
    }
    
    override suspend fun clearUsageData() = withContext(Dispatchers.IO) {
        prefs.edit().remove(KEY_USAGE_DATA).apply()
    }
    
    companion object {
        private const val PREFS_NAME = "usage_tracking_prefs"
        private const val KEY_USAGE_DATA = "usage_data"
    }
}
```

### 1.4 Implement Repository

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/domain/repository/UsageTrackingRepository.kt`

```kotlin
package com.symbianx.minimalistlauncher.domain.repository

import com.symbianx.minimalistlauncher.domain.model.AppLaunchSummary
import com.symbianx.minimalistlauncher.domain.model.DailyUnlockSummary

interface UsageTrackingRepository {
    suspend fun recordUnlock(): DailyUnlockSummary
    suspend fun recordAppLaunch(packageName: String): AppLaunchSummary
    suspend fun getDailyUnlockSummary(): DailyUnlockSummary
    suspend fun getAppLaunchSummary(packageName: String): AppLaunchSummary
    suspend fun clearAllData()
}
```

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/data/repository/UsageTrackingRepositoryImpl.kt`

```kotlin
package com.symbianx.minimalistlauncher.data.repository

import com.symbianx.minimalistlauncher.data.local.AppUsageData
import com.symbianx.minimalistlauncher.data.local.UsageTrackingDataSource
import com.symbianx.minimalistlauncher.domain.model.AppLaunchSummary
import com.symbianx.minimalistlauncher.domain.model.DailyUnlockSummary
import com.symbianx.minimalistlauncher.domain.repository.UsageTrackingRepository

class UsageTrackingRepositoryImpl(
    private val dataSource: UsageTrackingDataSource
) : UsageTrackingRepository {
    
    override suspend fun recordUnlock(): DailyUnlockSummary {
        val current = dataSource.loadUsageData()
        val updated = current.copy(
            unlockCount = current.unlockCount + 1,
            lastUnlockTimestamp = System.currentTimeMillis()
        )
        dataSource.saveUsageData(updated)
        
        return DailyUnlockSummary(
            date = updated.currentDate,
            unlockCount = updated.unlockCount,
            lastUnlockTimestamp = updated.lastUnlockTimestamp
        )
    }
    
    override suspend fun recordAppLaunch(packageName: String): AppLaunchSummary {
        require(packageName.isNotEmpty()) { "packageName cannot be empty" }
        
        val current = dataSource.loadUsageData()
        val appData = current.appLaunches[packageName]
        val updatedAppData = AppUsageData(
            launchCount = (appData?.launchCount ?: 0) + 1,
            lastLaunchTimestamp = System.currentTimeMillis()
        )
        
        val updated = current.copy(
            appLaunches = current.appLaunches + (packageName to updatedAppData)
        )
        dataSource.saveUsageData(updated)
        
        return AppLaunchSummary(
            packageName = packageName,
            launchCount = updatedAppData.launchCount,
            lastLaunchTimestamp = updatedAppData.lastLaunchTimestamp
        )
    }
    
    override suspend fun getDailyUnlockSummary(): DailyUnlockSummary {
        val current = dataSource.loadUsageData()
        return DailyUnlockSummary(
            date = current.currentDate,
            unlockCount = current.unlockCount,
            lastUnlockTimestamp = current.lastUnlockTimestamp
        )
    }
    
    override suspend fun getAppLaunchSummary(packageName: String): AppLaunchSummary {
        val current = dataSource.loadUsageData()
        val appData = current.appLaunches[packageName]
        return AppLaunchSummary(
            packageName = packageName,
            launchCount = appData?.launchCount ?: 0,
            lastLaunchTimestamp = appData?.lastLaunchTimestamp ?: 0
        )
    }
    
    override suspend fun clearAllData() {
        dataSource.clearUsageData()
    }
}
```

### 1.5 Create Use Cases

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/TrackUnlockUseCase.kt`

```kotlin
package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.DailyUnlockSummary
import com.symbianx.minimalistlauncher.domain.repository.UsageTrackingRepository
import java.time.LocalDate

interface TrackUnlockUseCase {
    suspend operator fun invoke(): DailyUnlockSummary
}

class TrackUnlockUseCaseImpl(
    private val repository: UsageTrackingRepository
) : TrackUnlockUseCase {
    override suspend fun invoke(): DailyUnlockSummary {
        return try {
            repository.recordUnlock()
        } catch (e: Exception) {
            // Graceful degradation - return default
            DailyUnlockSummary(
                date = LocalDate.now().toString(),
                unlockCount = 0,
                lastUnlockTimestamp = 0
            )
        }
    }
}
```

### 1.6 Create Time Formatter Utility

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/util/TimeFormatter.kt`

```kotlin
package com.symbianx.minimalistlauncher.util

object TimeFormatter {
    
    fun formatRelativeTime(timestampMillis: Long): String? {
        if (timestampMillis == 0L) return null
        
        val now = System.currentTimeMillis()
        val diffMillis = now - timestampMillis
        val diffMinutes = diffMillis / (60 * 1000)
        val diffHours = diffMillis / (60 * 60 * 1000)
        
        return when {
            diffMinutes < 1 -> "just now"
            diffMinutes < 60 -> "${diffMinutes}m ago"
            diffHours < 24 -> "${diffHours}h ago"
            else -> null  // Different day
        }
    }
}
```

### 1.7 Setup Unlock Tracking Receiver

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/MainActivity.kt`

Add to MainActivity:

```kotlin
private lateinit var unlockReceiver: BroadcastReceiver
private val usageRepository: UsageTrackingRepository by lazy {
    UsageTrackingRepositoryImpl(
        UsageTrackingDataSourceImpl(applicationContext)
    )
}
private val trackUnlockUseCase: TrackUnlockUseCase by lazy {
    TrackUnlockUseCaseImpl(usageRepository)
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Register unlock receiver
    unlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_USER_PRESENT) {
                lifecycleScope.launch {
                    trackUnlockUseCase()
                }
            }
        }
    }
    registerReceiver(unlockReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
    
    // ... rest of onCreate
}

override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(unlockReceiver)
}
```

### 1.8 Display on Home Screen

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/UnlockCountDisplay.kt`

```kotlin
package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UnlockCountDisplay(
    unlockCount: Int,
    lastUnlockTimeAgo: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "$unlockCount unlocks today",
            style = MaterialTheme.typography.bodySmall,
            fontSize = 11.sp,
            color = Color.Gray
        )
        
        lastUnlockTimeAgo?.let { time ->
            Text(
                text = "last unlock: $time",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
    }
}
```

Update `HomeViewModel` to expose unlock stats and integrate into `HomeScreen`.

---

## Phase 2: App Launch Frequency (P2) 🎯

**Goal**: Show brief overlay with launch count when opening apps.

### 2.1 Create App Launch Use Case

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/TrackAppLaunchUseCase.kt`

```kotlin
package com.symbianx.minimalistlauncher.domain.usecase

import com.symbianx.minimalistlauncher.domain.model.AppLaunchSummary
import com.symbianx.minimalistlauncher.domain.repository.UsageTrackingRepository

interface TrackAppLaunchUseCase {
    suspend operator fun invoke(packageName: String): AppLaunchSummary
}

class TrackAppLaunchUseCaseImpl(
    private val repository: UsageTrackingRepository
) : TrackAppLaunchUseCase {
    override suspend fun invoke(packageName: String): AppLaunchSummary {
        return try {
            repository.recordAppLaunch(packageName)
        } catch (e: Exception) {
            AppLaunchSummary(packageName, 0, 0)
        }
    }
}
```

### 2.2 Create App Launch Overlay

**File**: `app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/AppLaunchOverlay.kt`

```kotlin
package com.symbianx.minimalistlauncher.ui.home.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AppLaunchOverlay(
    appName: String,
    launchCount: Int,
    lastLaunchTimeAgo: String?,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (visible) {
        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatLaunchCount(launchCount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    lastLaunchTimeAgo?.let {
                        Text(
                            text = "last opened $it",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatLaunchCount(count: Int): String {
    val suffix = when (count % 10) {
        1 -> if (count == 11) "th" else "st"
        2 -> if (count == 12) "th" else "nd"
        3 -> if (count == 13) "th" else "rd"
        else -> "th"
    }
    return "${count}$suffix time today"
}
```

### 2.3 Integrate Launch Tracking

Update `LaunchAppUseCase` to track launches and show overlay before starting app.

---

## Phase 3: Last Launch Time (P3) 🎯

**Goal**: Display "last opened X ago" in app launch overlay.

This is already integrated in Phase 2's overlay - just ensure `lastLaunchTimeAgo` is populated from repository data.

---

## Testing

### Unit Tests

Create tests for:
- `UsageTrackingDataSourceImpl` (SharedPreferences logic, day boundary resets)
- `UsageTrackingRepositoryImpl` (recording, retrieval)
- `TrackUnlockUseCaseImpl` (graceful degradation)
- `TimeFormatter` (relative time formatting)

### Manual Testing

1. **Unlock tracking**: Lock/unlock phone multiple times, verify count increments on home screen
2. **Midnight reset**: Change device time to 23:59, wait for midnight, verify reset
3. **App launch overlay**: Tap apps repeatedly, verify count increments
4. **Persistence**: Reboot device, verify counts survive

---

## Rollout Checklist

- [ ] P1: Unlock tracking visible on home screen
- [ ] P1: Data persists across reboots
- [ ] P2: App launch overlay shows on app tap
- [ ] P2: Overlay auto-dismisses after 800ms
- [ ] P3: "Last opened" time displays correctly
- [ ] Unit tests pass
- [ ] Manual testing complete
- [ ] No performance regressions (60fps maintained)
- [ ] ktlint passes
