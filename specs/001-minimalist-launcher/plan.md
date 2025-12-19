# Implementation Plan: Minimalist Android Launcher

**Branch**: `001-minimalist-launcher` | **Date**: 2025-12-19 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-minimalist-launcher/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Build a minimalist Android launcher for Google Pixel 8 Pro that replaces the default Google launcher with a focus on reducing phone usage time. Core functionality includes text-based app search, favorite apps quick access (up to 5), status display (time, date, battery, now playing), and swipe-to-search gesture. Technical approach uses native Android SDK with Jetpack Compose for UI, ViewModel architecture for state management, and Flow-based reactive data streams.

## Technical Context

**Language/Version**: Kotlin 1.9+ (Android SDK 36, minSdk 26, targetSdk 36)
**Primary Dependencies**: Jetpack Compose (UI), AndroidX Lifecycle (ViewModel), Kotlin Coroutines + Flow (reactive streams), Material Design 3, LeakCanary (debug memory leak detection)
**Storage**: SharedPreferences (favorites persistence), Android ContentProvider (Now Playing data access)
**Testing**: JUnit 4, Espresso (UI tests), MockK (mocking), Truth (assertions)
**Target Platform**: Android 8.0+ (API 26+) focusing on Google Pixel 8 Pro (Android 15, API 36)
**Project Type**: Mobile (single Android application module)
**Performance Goals**: 120 FPS on 120Hz displays (8.3ms per frame), search results in <100ms, cold start <500ms, APK size <5MB
**Constraints**: Portrait-only orientation, memory footprint <30MB, text-only UI (no app icons), graceful degradation for non-Pixel devices
**Scale/Scope**: Single-module Android app, ~15-20 Compose screens/components, ~30 source files, 5 main features (search, favorites, status, gestures, launcher integration)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Performance Standards
- ✅ **120 FPS rendering**: Jetpack Compose with proper recomposition scoping
- ✅ **Memory footprint <30MB**: Single-module app, no heavyweight libraries
- ✅ **Cold start <500ms**: Minimal initialization, lazy loading where possible
- ✅ **APK size <5MB**: No third-party dependencies beyond Android SDK

### Testing Requirements
- ✅ **100% user story coverage**: Integration tests for all 5 user stories (P1-P3)
- ✅ **80%+ code coverage**: Focus on business logic (search, favorites, gesture detection, app loading, now playing)
- ✅ **Contract tests**: Not applicable (no external API contracts)

### UX Consistency
- ✅ **Material Design 3**: Typography, text input, theming
- ✅ **Dark/light theme support**: High contrast text for accessibility
- ✅ **Immediate visual feedback**: All interactions (tap, swipe, long-press)
- ✅ **Touch target sizing**: 64dp minimum for search results
- ✅ **Accessibility**: TalkBack screen reader support (SC-009)

### Quality Standards
- ✅ **Zero linting violations**: ktlint for Kotlin code style
- ✅ **KDoc documentation**: All public APIs documented
- ✅ **Minimal dependencies**: Android SDK only (Jetpack Compose, AndroidX), LeakCanary for debug, no third-party libraries

**Gate Status**: ✅ PASS - All constitution requirements aligned with spec

## Project Structure

### Documentation (this feature)

```text
specs/001-minimalist-launcher/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
android-minimalist-launcher/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/symbianx/minimalistlauncher/
│   │   │   │   ├── MainActivity.kt                    # Launcher entry point
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/                        # Material Design 3 theming
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeScreen.kt            # Main launcher screen
│   │   │   │   │   │   ├── HomeViewModel.kt         # State management
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── StatusBar.kt         # Time/date/battery/now playing
│   │   │   │   │   │       ├── FavoritesList.kt     # Favorite apps list
│   │   │   │   │   │       └── SearchView.kt        # Search overlay
│   │   │   │   │   └── gesture/
│   │   │   │   │       └── SwipeGestureDetector.kt  # Swipe gesture handling
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── App.kt                   # App entity
│   │   │   │   │   │   ├── FavoriteApp.kt           # Favorite app entity
│   │   │   │   │   │   ├── DeviceStatus.kt          # Time/date/battery status
│   │   │   │   │   │   ├── NowPlayingInfo.kt        # Now Playing data
│   │   │   │   │   │   └── SearchState.kt           # Search query state
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── AppRepository.kt         # App data access
│   │   │   │   │   │   ├── FavoritesRepository.kt   # Favorites persistence
│   │   │   │   │   │   ├── DeviceStatusRepository.kt # System status
│   │   │   │   │   │   └── NowPlayingRepository.kt  # Now Playing access
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── SearchAppsUseCase.kt     # Search filtering logic
│   │   │   │   │       ├── ManageFavoritesUseCase.kt # Add/remove favorites
│   │   │   │   │       └── LaunchAppUseCase.kt      # App launch handling
│   │   │   │   └── data/
│   │   │   │       ├── system/
│   │   │   │       │   ├── AppListDataSource.kt     # PackageManager interface
│   │   │   │       │   ├── DeviceStatusDataSource.kt # System services
│   │   │   │       │   └── NowPlayingDataSource.kt  # Media session access
│   │   │   │       └── local/
│   │   │   │           └── FavoritesDataSource.kt   # SharedPreferences
│   │   │   ├── AndroidManifest.xml                   # Launcher intent filters
│   │   │   └── res/                                  # Resources (themes, strings)
│   │   └── androidTest/                              # Integration tests (Espresso)
│   │       └── java/com/symbianx/minimalistlauncher/
│   │           ├── HomeScreenTest.kt
│   │           ├── SearchTest.kt
│   │           ├── FavoritesTest.kt
│   │           └── GestureTest.kt
│   └── build.gradle.kts                              # Module configuration
├── build.gradle.kts                                  # Root project configuration
├── gradle.properties                                 # Gradle settings
└── settings.gradle.kts                               # Project settings
```

**Structure Decision**: Mobile application (single-module Android project). The structure follows Android recommended architecture with clear separation of concerns: UI layer (Compose), domain layer (models, repositories, use cases), and data layer (system/local data sources). This enables independent testing of business logic while keeping the codebase maintainable and following Android best practices.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations detected. All constitution requirements are met by the proposed architecture.
