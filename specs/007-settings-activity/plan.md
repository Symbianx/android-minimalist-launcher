# Implementation Plan: Launcher Settings Activity

**Branch**: `007-settings-activity` | **Date**: 2026-01-07 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/007-settings-activity/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Implement a comprehensive settings activity for the Android Minimalist Launcher that allows users to customize launcher behavior including: toggling auto-launch functionality, customizing quick action button apps (left/right corners), controlling battery indicator visibility thresholds, and resetting all settings to defaults. The settings will be accessed via long-press on the home screen background and will use SharedPreferences/DataStore for persistence following the existing favorites storage pattern.

## Technical Context

**Language/Version**: Kotlin (latest stable), Gradle, Android SDK (target API 36, min API 26)  
**Primary Dependencies**: Jetpack Compose, Material3, AndroidX (ViewModel, Activity Compose), DataStore Preferences, Kotlinx Serialization  
**Storage**: DataStore Preferences for settings persistence (following modern Android best practices)  
**Testing**: JUnit (unit tests), Robolectric (Android unit tests), Compose UI Test (instrumented tests)  
**Target Platform**: Android 8.0+ (API 26+)  
**Project Type**: Mobile (single Android app, launcher)  
**Performance Goals**: Settings load/save under 100ms, UI updates within 1 second, app picker list scrolls at 60fps  
**Constraints**: Settings must persist across app restarts and reboots, must handle corrupted data gracefully, all changes must be atomic  
**Scale/Scope**: Core settings feature affecting 4 existing features (auto-launch, quick actions, battery indicator, home screen), ~5-7 settings total

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Initial Check (Pre-Phase 0)**: ✅ PASSED

- **Test-First**: All settings read/write operations, UI interactions, and edge cases (corrupted data, uninstalled apps) must be covered by tests before implementation is considered complete
- **Simplicity**: Reuse existing storage patterns (SharedPreferences/DataStore similar to FavoritesDataSourceImpl), use Material3 Preference components or standard Compose UI, minimize new abstractions
- **Integration Testing**: End-to-end tests required for: settings persistence across app restarts, quick action button customization flow, reset to defaults functionality
- **Observability**: Log settings changes, corrupted data recovery, and uninstalled app detection for debugging

**Post-Phase 1 Check**: ✅ PASSED

- **Test-First**: Contract tests defined in `/contracts/settings-repository.md`, unit test scenarios documented in data-model.md, UI test scenarios in quickstart.md
- **Simplicity**: Design mirrors existing FavoritesDataSourceImpl pattern, no unnecessary abstractions added, clean architecture separation maintained
- **Integration Testing**: End-to-end test scenarios defined for: persistence, uninstalled app recovery, reset functionality, reactive Flow updates
- **Observability**: SettingsLogger defined for all state changes, DataStore corruption handlers log errors, validation failures logged with warnings

✓ **No constitution violations** - Feature follows established patterns for data persistence and UI composition

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/
├── src/
│   ├── main/
│   │   └── java/com/symbianx/minimalistlauncher/
│   │       ├── ui/
│   │       │   ├── settings/
│   │       │   │   ├── SettingsActivity.kt            # NEW: Main settings screen
│   │       │   │   ├── SettingsViewModel.kt           # NEW: Settings state management
│   │       │   │   └── components/
│   │       │   │       ├── SettingsScreen.kt          # NEW: Settings UI composable
│   │       │   │       ├── AppPickerDialog.kt         # NEW: App selection dialog
│   │       │   │       └── BatteryThresholdPicker.kt  # NEW: Battery threshold selector
│   │       │   └── home/
│   │       │       ├── HomeScreen.kt                  # Update: Add long-press handler
│   │       │       ├── HomeViewModel.kt               # Update: Read settings
│   │       │       └── components/
│   │       │           ├── QuickActionButtons.kt      # Update: Use custom apps
│   │       │           └── CircularBatteryIndicator.kt # Update: Use threshold setting
│   │       ├── domain/
│   │       │   ├── model/
│   │       │   │   ├── LauncherSettings.kt            # NEW: Settings data model
│   │       │   │   ├── QuickActionConfig.kt           # NEW: Quick action config
│   │       │   │   └── BatteryIndicatorConfig.kt      # NEW: Battery config
│   │       │   ├── repository/
│   │       │   │   └── SettingsRepository.kt          # NEW: Settings interface
│   │       │   └── usecase/
│   │       │       ├── LoadSettingsUseCase.kt         # NEW: Load settings
│   │       │       ├── SaveSettingsUseCase.kt         # NEW: Save settings
│   │       │       └── ResetSettingsUseCase.kt        # NEW: Reset to defaults
│   │       ├── data/
│   │       │   ├── local/
│   │       │   │   ├── SettingsDataSource.kt          # NEW: Settings persistence interface
│   │       │   │   └── SettingsDataSourceImpl.kt      # NEW: DataStore implementation
│   │       │   └── repository/
│   │       │       └── SettingsRepositoryImpl.kt      # NEW: Repository implementation
│   │       └── util/
│   │           └── SettingsLogger.kt                  # NEW: Settings event logging
│   ├── androidTest/
│   │   └── java/com/symbianx/minimalistlauncher/
│   │       ├── ui/settings/
│   │       │   ├── SettingsActivityTest.kt            # NEW: Settings UI tests
│   │       │   └── SettingsIntegrationTest.kt         # NEW: End-to-end tests
│   │       └── data/local/
│   │           └── SettingsDataSourceTest.kt          # NEW: Persistence tests
│   └── test/
│       └── java/com/symbianx/minimalistlauncher/
│           ├── domain/usecase/
│           │   ├── LoadSettingsUseCaseTest.kt         # NEW: Use case tests
│           │   ├── SaveSettingsUseCaseTest.kt         # NEW: Use case tests
│           │   └── ResetSettingsUseCaseTest.kt        # NEW: Use case tests
│           └── data/repository/
│               └── SettingsRepositoryTest.kt          # NEW: Repository tests
└── build.gradle.kts
```

**Structure Decision**: Mobile single-app structure following clean architecture pattern established in existing features. Settings module mirrors favorites implementation (data/local → repository → use cases → UI). New SettingsActivity launched via intent from HomeScreen long-press gesture. All new components follow existing naming conventions and package organization.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations to track - feature follows established patterns:
- Mirrors FavoritesDataSourceImpl approach for persistence
- Reuses existing AppRepository for app selection
- Uses standard Compose UI patterns already in project
- No new abstractions beyond what's required for clean architecture
- Test-first approach with contract tests ensures quality
