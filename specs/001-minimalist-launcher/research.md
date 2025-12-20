# Research: Minimalist Android Launcher

**Feature**: Minimalist Android Launcher  
**Branch**: 001-minimalist-launcher  
**Date**: 2025-12-19  
**Phase**: 0 - Outline & Research

## Overview

This document consolidates research findings for implementing a minimalist Android launcher optimized for Pixel 8 Pro with 120Hz display, gesture-based search activation, and Pixel Now Playing integration.

## Key Research Areas

### 1. Android Launcher Implementation

**Decision**: Use `android.intent.category.HOME` intent filter in AndroidManifest

**Rationale**:
- Standard Android mechanism for registering as a launcher
- Allows system to present launcher chooser dialog
- Persists across reboots when selected as default
- No special permissions required for basic launcher functionality

**Implementation Details**:
```xml
<activity android:name=".MainActivity"
    android:launchMode="singleTask"
    android:clearTaskOnLaunch="true"
    android:stateNotNeeded="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

**Alternatives Considered**:
- Custom launcher framework: Rejected due to unnecessary complexity
- Overlay on existing launcher: Rejected, doesn't fully replace launcher

### 2. Jetpack Compose for UI (120 FPS Performance)

**Decision**: Use Jetpack Compose with performance optimizations

**Rationale**:
- Native support for Material Design 3
- Declarative UI reduces boilerplate and improves maintainability
- Built-in state management with remember/MutableState
- Composable functions enable clean separation of concerns
- Performance profiling tools available (Compose Compiler Reports, Layout Inspector)

**120 FPS Optimization Strategies**:
1. **Avoid recomposition**: Use `remember`, `derivedStateOf`, and stable keys
2. **Lazy composition**: `LazyColumn` for app list with key-based reuse
3. **Defer reads**: Access state values inside composables, not lambdas
4. **Baseline profiles**: Generate ahead-of-time compilation profiles
5. **Hardware acceleration**: Enabled by default on API 26+

**Performance Benchmarks**:
- Compose can achieve 120 FPS with proper state management
- Frame drops typically occur due to excessive recomposition
- Use `@Stable` and `@Immutable` annotations for data classes

**Alternatives Considered**:
- XML Views: Rejected, more boilerplate, less maintainable
- Flutter: Rejected, not native Android, larger APK size
- React Native: Rejected, JavaScript bridge overhead

### 3. Gesture Detection (Swipe Right-to-Left)

**Decision**: Use Compose `Modifier.pointerInput` with `detectHorizontalDragGestures`

**Rationale**:
- Native Compose gesture API with coroutine support
- Low-level control over touch events
- Can achieve <50ms recognition with immediate callback
- Works seamlessly with Compose state management

**Implementation Pattern**:
```kotlin
Modifier.pointerInput(Unit) {
    detectHorizontalDragGestures(
        onDragEnd = {
            if (dragDistance > threshold && dragVelocity > minVelocity) {
                onSwipeDetected()
            }
        }
    )
}
```

**Performance Considerations**:
- Process gestures on UI thread (already optimized by Compose)
- Use velocity threshold to distinguish deliberate swipes
- Minimum 100dp drag distance for clear intent
- Haptic feedback on successful gesture detection

**Alternatives Considered**:
- GestureDetector: Rejected, XML-View specific
- Third-party gesture libraries: Rejected, unnecessary dependency

### 4. App List Management (PackageManager)

**Decision**: Use `PackageManager.queryIntentActivities()` with `MATCH_ALL` flag

**Rationale**:
- Standard Android API for querying installed apps
- Returns all launchable applications
- Includes app name, package name, and launch intent
- Efficient for one-time loading with caching

**Implementation Strategy**:
1. Load app list on background thread (Coroutines)
2. Cache in ViewModel with StateFlow
3. Filter apps with `Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)`
4. Sort alphabetically by label
5. Exclude system apps using `ApplicationInfo.FLAG_SYSTEM`

**Permission Required**:
- Android 11+ (API 30): `QUERY_ALL_PACKAGES` permission in manifest
- Declare package visibility in manifest for API 30+

**Alternatives Considered**:
- LauncherApps API: Rejected, more complex, designed for multi-user scenarios
- Manual package scanning: Rejected, reinventing the wheel

### 5. Real-Time Search Filtering

**Decision**: Use Kotlin Flow with `debounce` and case-insensitive matching

**Rationale**:
- Flow provides reactive stream for text changes
- Debounce (100-150ms) reduces unnecessary filtering
- Case-insensitive `contains()` provides intuitive search
- Can achieve <100ms response time with proper optimization

**Implementation Pattern**:
```kotlin
searchQuery.debounce(100).mapLatest { query ->
    if (query.isBlank()) allApps
    else allApps.filter { it.label.contains(query, ignoreCase = true) }
}.collect { filteredApps ->
    _searchResults.value = filteredApps
}
```

**Performance Optimization**:
- Pre-lowercase app names for faster comparison
- Use binary search for large app lists (>500 apps)
- Limit displayed results to 50 apps

**Alternatives Considered**:
- Fuzzy matching (Levenshtein): Rejected, too slow for real-time
- Full-text search (SQLite FTS): Rejected, overkill for app names

### 6. Pixel Now Playing Integration

**Decision**: Access via NotificationListenerService to read Now Playing notifications

**Rationale**:
- Pixel's "Now Playing" displays detected songs as a notification from com.google.android.as
- The notification contains song and artist in the title field formatted as "Song Name by Artist Name"
- NotificationListenerService provides real-time updates when songs are detected
- No content provider access is available or needed
- Requires user to grant notification access permission

**Implementation Strategy**:
1. Create NotificationListenerService to listen for notifications from com.google.android.as
2. Filter for notification ID 123 (the Now Playing notification)
3. Extract title from notification extras which contains "Song Name by Artist Name"
4. Parse the title to separate song name and artist
5. Emit updates via StateFlow for reactive UI updates
6. Handle cases where notification is removed (no music detected)

**Notification Structure**:
```
Package: com.google.android.as
ID: 123
Extras:
  - android.title: "Song Name by Artist Name"
  - android.text: "Tap to see your song history"
  - android.substName: "Now Playing"
```

**Permission Required**:
- BIND_NOTIFICATION_LISTENER_SERVICE (declared in AndroidManifest)
- User must manually enable notification access in system settings:
  Settings > Apps > Special app access > Notification access

**Fallback Strategy**:
- Check if NotificationListenerService is enabled
- Display unavailable state if permission not granted
- No crash on non-Pixel devices
- Gracefully handle missing notifications

**Alternatives Considered**:
- Content provider (content://com.google.android.as): Rejected, provider does not exist or is not queryable
- Media session API: Rejected, only for actively playing media
- Third-party Shazam API: Rejected, doesn't access Pixel's local data

### 7. Status Display (Time & Battery)

**Decision**: Use system BroadcastReceivers for time and battery updates

**Rationale**:
- Standard Android mechanism for system state changes
- Efficient, only updates when values change
- No polling required

**Time Updates**:
- Register `Intent.ACTION_TIME_TICK` (fires every minute)
- Use `DateFormat.getTimeInstance()` for locale-aware formatting
- Respect system 12/24-hour preference

**Battery Updates**:
- Register `Intent.ACTION_BATTERY_CHANGED` (fires on level change)
- Extract percentage from `BatteryManager.EXTRA_LEVEL` and `EXTRA_SCALE`
- Updates automatically, no manual refresh needed

**Performance**:
- Register receivers only when launcher visible
- Unregister in `onPause()` to save battery

**Alternatives Considered**:
- Manual polling: Rejected, battery drain
- WorkManager periodic tasks: Rejected, overkill for foreground updates

### 8. Dark/Light Theme Support

**Decision**: Use Material3 `DynamicColorScheme` with system theme detection

**Rationale**:
- Material3 provides built-in dynamic theming
- `isSystemInDarkTheme()` detects system preference
- Automatic color adaptation to Pixel's Material You
- High contrast text guaranteed by Material3 tokens

**Implementation**:
```kotlin
MaterialTheme(
    colorScheme = if (isSystemInDarkTheme()) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        dynamicLightColorScheme(LocalContext.current)
    }
)
```

**Alternatives Considered**:
- Manual theme switching: Rejected, user expects system integration
- Custom color palettes: Rejected, Material You provides device-specific colors

### 9. Accessibility (TalkBack Support)

**Decision**: Use Compose semantics modifiers for content descriptions

**Rationale**:
- Compose provides declarative accessibility API
- `Modifier.semantics` adds screen reader descriptions
- Automatic focus order with Compose layout
- WCAG 2.1 Level AA compliant with proper labeling

**Implementation Strategy**:
1. Add `contentDescription` to all interactive elements
2. Use `semantics { role = Role.Button }` for clickable items
3. Provide state descriptions for search active/inactive
4. Test with TalkBack enabled

**Key Areas**:
- App list items: "Launch [App Name]"
- Search field: "Search for apps"
- Status info: "Time [time], Battery [percentage]"
- Now Playing: "[Song] by [Artist], tap to view history"

**Alternatives Considered**:
- Manual accessibility tree: Rejected, Compose handles this

### 10. Memory & Performance Optimization

**Decision**: Multi-pronged optimization approach

**Strategies**:

1. **R8/ProGuard Shrinking**:
   - Enable code shrinking and obfuscation
   - Remove unused Compose compiler functions
   - Expected 30-40% APK size reduction

2. **Lazy Loading**:
   - Load app list on-demand (first swipe)
   - Use `LazyColumn` for app results
   - Defer Now Playing observation until UI ready

3. **Coroutine Scoping**:
   - Use `viewModelScope` for automatic cancellation
   - Cancel observers when launcher not visible
   - Structured concurrency prevents leaks

4. **Compose Optimization**:
   - Use `key()` in LazyColumn for stable identity
   - Avoid inline lambdas in frequently recomposed areas
   - Extract static composables to separate functions

5. **Memory Profiling**:
   - Use Android Studio Memory Profiler
   - LeakCanary integration for leak detection
   - Target <30MB steady-state memory

**Performance Targets**:
- Cold start: <500ms (measured with `adb shell am start -W`)
- Gesture recognition: <50ms (from touch to UI update)
- Search response: <100ms (debounced input to filtered results)
- Frame time: <8.3ms (120 FPS on Pixel 8 Pro)

**Alternatives Considered**:
- Native C++ for filtering: Rejected, premature optimization
- Custom memory pooling: Rejected, unnecessary complexity

## Technology Stack Summary

| Component | Technology | Version | Justification |
|-----------|------------|---------|---------------|
| Language | Kotlin | 1.9+ | Concise, null-safe, coroutines, standard for Android |
| UI Framework | Jetpack Compose | Latest | Declarative, Material3, performance-optimized |
| Async | Kotlin Coroutines | 1.7+ | Structured concurrency, Flow for reactive streams |
| DI | Constructor Injection | N/A | Simple, no framework overhead for small app |
| Testing | JUnit 5 + Espresso | Latest | Unit + integration testing, Android standard |
| Build | Gradle (Kotlin DSL) | 8.0+ | Type-safe build scripts, version catalogs |
| Linting | ktlint | Latest | Enforces Kotlin style guide |

## Dependencies

**Minimal dependency strategy per constitution**:

```kotlin
dependencies {
    // Android Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.0")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Testing
    testImplementation("junit:junit:5.10.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
```

**Total dependency count**: ~10 (excluding transitive)

**Justification for each**:
- Compose: Required for UI (constitution allows for justified core dependencies)
- Coroutines: Required for async operations (part of Kotlin, minimal overhead)
- Testing libraries: Required per constitution TDD mandate
- LeakCanary: Debug-only, required for memory leak detection (constitution mandate)

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Now Playing API changes | Medium | High | Fallback to empty state, version detection |
| 120 FPS not achieved | Low | Medium | Profile early, optimize composables, baseline profiles |
| Non-Pixel device compatibility | High | Low | Graceful degradation, hide Now Playing on unavailable |
| App list slow on >500 apps | Low | Medium | Implement pagination, binary search filtering |
| Memory leaks in Compose | Low | High | LeakCanary, proper lifecycle management |

## Open Questions

**All resolved** - No NEEDS CLARIFICATION markers remaining from Technical Context.

## Next Steps

Proceed to **Phase 1: Design & Contracts**
- Create data-model.md with entity definitions
- Generate contracts/ for internal component APIs
- Create quickstart.md for development setup
- Update agent context with technology stack
