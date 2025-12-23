# android-minimalist-launcher Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-12-19

## Active Technologies
- Kotlin (latest stable), Gradle, Android SDK (target: current stable) + Jetpack Compose, AndroidX, standard Android libraries (002-auto-launch)
- N/A (no persistent storage required for this feature) (002-auto-launch)
- Kotlin (latest stable), Jetpack Compose + Jetpack Compose, AndroidX, Material, Kotlin stdlib (005-battery-indicator-polish)
- N/A (no persistent storage required for indicator) (005-battery-indicator-polish)
- Kotlin 2.3.0, Java 17 targe + Jetpack Compose (Compose Compiler), kotlinx-serialization, AndroidX Core (006-usage-awareness)
- SharedPreferences with JSON serialization (existing pattern from FavoritesDataSource) (006-usage-awareness)

- Kotlin 1.9+ (Android SDK 36, minSdk 26, targetSdk 36) + Jetpack Compose (UI), AndroidX Lifecycle (ViewModel), Kotlin Coroutines + Flow (reactive streams), Material Design 3, LeakCanary (debug memory leak detection) (001-minimalist-launcher)

## Project Structure

```text
src/
tests/
```

## Commands

# Add commands for Kotlin 1.9+ (Android SDK 36, minSdk 26, targetSdk 36)

## Code Style

Kotlin 1.9+ (Android SDK 36, minSdk 26, targetSdk 36): Follow standard conventions

## Recent Changes
- 006-usage-awareness: Added Kotlin 2.3.0, Java 17 targe + Jetpack Compose (Compose Compiler), kotlinx-serialization, AndroidX Core
- 005-battery-indicator-polish: Added Kotlin (latest stable), Jetpack Compose + Jetpack Compose, AndroidX, Material, Kotlin stdlib
- 002-auto-launch: Added Kotlin (latest stable), Gradle, Android SDK (target: current stable) + Jetpack Compose, AndroidX, standard Android libraries


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
