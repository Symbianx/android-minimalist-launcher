# Implementation Plan: Usage Awareness

**Branch**: `006-usage-awareness` | **Date**: 2025-12-22 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/specs/006-usage-awareness/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Implement usage awareness tracking to help users become conscious of their phone usage patterns without judgment. The launcher will track and minimally display: (1) daily phone unlock count and last unlock time on the home screen, (2) app launch frequency when opening apps, and (3) last launch time for each app. All tracking data is stored locally, displayed factually without shame, and never blocks user actions. The implementation follows the core principle: "Make unconscious behavior visible, then step aside."

## Technical Context

**Language/Version**: Kotlin 2.3.0, Java 17 target  
**Primary Dependencies**: Jetpack Compose (Compose Compiler), kotlinx-serialization, AndroidX Core  
**Storage**: SharedPreferences with JSON serialization (existing pattern from FavoritesDataSource)  
**Testing**: JUnit 4, AndroidX Test (existing: `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"`)  
**Target Platform**: Android 8.0+ (API 26-36), optimized for Pixel 8 Pro  
**Project Type**: Single Android application  
**Performance Goals**: 
  - Unlock count visible within 100ms of home screen render
  - App launch tracking adds <50ms overhead
  - 60fps maintained during all animations  
**Constraints**: 
  - No invasive permissions (no SYSTEM_ALERT_WINDOW, no USAGE_STATS, no accessibility services)
  - Data persists across reboots
  - Tracking failures never block app launches
  - Minimal visual footprint (<5% of home screen area)  
**Scale/Scope**: 
  - Single user device
  - Track ~50-200 apps
  - ~100-500 events per day
  - 30 days rolling data retention (NEEDS CLARIFICATION: retention policy not specified in spec)

**Unknowns requiring research**:
1. Best Android API for detecting screen unlock events without USAGE_STATS permission
2. Optimal data structure for efficient daily aggregation and midnight reset logic
3. Pattern for displaying brief overlays during app launch without delaying launch intent
4. Data retention strategy (how many days to keep historical data vs. daily reset only)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Status**: ✅ PASS (Constitution file is template-only, no project-specific constraints defined)

**Evaluation**: The `.specify/memory/constitution.md` file contains only placeholder content with no ratified project-specific principles. Therefore, there are no constitutional gates to validate at this time. 

**Recommendation**: Consider establishing project constitution principles such as:
- Data privacy requirements (local-only storage, no telemetry)
- Testing requirements (unit test coverage thresholds)
- Performance standards (60fps animations, <100ms responsiveness)
- Code quality gates (ktlint compliance, no warnings)

**Re-evaluation after Phase 1**: Will verify that the designed architecture maintains privacy-first approach and doesn't introduce invasive permissions.

## Project Structure

### Documentation (this feature)

```text
specs/006-usage-awareness/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
├── checklists/
│   └── requirements.md  # Spec validation checklist (already completed)
└── spec.md              # Feature specification
```

### Source Code (repository root)

```text
app/src/main/java/com/symbianx/minimalistlauncher/
├── domain/
│   ├── model/
│   │   ├── UnlockEvent.kt              # NEW: Phone unlock event model
│   │   ├── DailyUnlockSummary.kt       # NEW: Aggregated unlock data
│   │   ├── AppLaunchEvent.kt           # NEW: App launch event model
│   │   ├── AppLaunchSummary.kt         # NEW: Aggregated app launch data
│   │   └── UsageAwarenessState.kt      # NEW: UI state for usage displays
│   ├── repository/
│   │   └── UsageTrackingRepository.kt  # NEW: Interface for usage data
│   └── usecase/
│       ├── TrackUnlockUseCase.kt       # NEW: Record unlock events
│       ├── TrackAppLaunchUseCase.kt    # NEW: Record app launch events
│       └── GetUsageStatsUseCase.kt     # NEW: Retrieve usage data for display
├── data/
│   ├── local/
│   │   ├── UsageTrackingDataSource.kt      # NEW: Interface for local storage
│   │   └── UsageTrackingDataSourceImpl.kt  # NEW: SharedPreferences + JSON impl
│   └── repository/
│       └── UsageTrackingRepositoryImpl.kt  # NEW: Repository implementation
├── ui/
│   └── home/
│       ├── components/
│       │   ├── UnlockCountDisplay.kt       # NEW: Shows unlock count on home
│       │   └── AppLaunchOverlay.kt         # NEW: Brief display when launching app
│       └── HomeViewModel.kt                # MODIFY: Integrate usage tracking
└── util/
    ├── TimeFormatter.kt                    # NEW: Format relative times ("2h ago")
    └── MidnightResetScheduler.kt           # NEW: Schedule daily resets at midnight

app/src/test/java/com/symbianx/minimalistlauncher/
├── domain/usecase/
│   ├── TrackUnlockUseCaseTest.kt
│   ├── TrackAppLaunchUseCaseTest.kt
│   └── GetUsageStatsUseCaseTest.kt
├── data/local/
│   └── UsageTrackingDataSourceImplTest.kt
└── util/
    ├── TimeFormatterTest.kt
    └── MidnightResetSchedulerTest.kt
```

**Structure Decision**: Follow existing Android Clean Architecture pattern (domain → data → ui layers). New usage tracking components integrate alongside existing favorites and battery tracking systems. Use existing SharedPreferences + JSON serialization pattern (consistent with `FavoritesDataSourceImpl`). All tracking logic lives in domain layer with repository abstraction.

## Complexity Tracking

**Status**: No violations - complexity is justified by requirements

This feature introduces moderate complexity:

**Added Components** (13 new files):
- 4 domain models (DailyUnlockSummary, AppLaunchSummary, UsageData, AppUsageData)
- 2 data source classes (interface + impl with SharedPreferences)
- 2 repository classes (interface + impl)
- 3 use cases (TrackUnlock, TrackAppLaunch, GetUsageStats)
- 2 UI components (UnlockCountDisplay, AppLaunchOverlay)

**Justification**:
- **Essential for spec compliance**: All 24 functional requirements necessitate these components
- **Follows existing patterns**: Mirrors FavoritesDataSource architecture (proven in codebase)
- **Clean separation**: Domain/data/UI layers maintain testability
- **Privacy-first**: Local-only storage, no cloud sync, minimal permissions (FR-024)
- **Graceful degradation**: All tracking failures caught and logged, never block user (FR-021)

**Complexity Mitigation**:
- Use existing SharedPreferences + JSON pattern (no new storage paradigm)
- Single BroadcastReceiver for unlock tracking (lightweight)
- Lazy date-based resets (no AlarmManager/WorkManager overhead)
- Inline overlay (no system overlays or invasive permissions)

---

## Phase 0: Research ✅ COMPLETE

**Status**: All unknowns resolved  
**Artifact**: [research.md](./research.md)

### Decisions Made

1. **Unlock Detection**: Use `ACTION_USER_PRESENT` broadcast (no special permissions)
2. **Data Structure**: Date-keyed JSON with lazy midnight reset (no background jobs)
3. **App Launch Overlay**: Modal Compose dialog with auto-dismiss (no overlay permission)
4. **Data Retention**: Current day only (privacy-first, no historical tracking)

**Key Findings**:
- `ACTION_USER_PRESENT` is perfect for conscious unlock tracking (API 1+, no permissions)
- Lazy reset pattern is simpler and more reliable than `AlarmManager` (avoids doze mode issues)
- Compose modal can display before app launch without `SYSTEM_ALERT_WINDOW` permission
- Current-day-only data aligns with spec and design philosophy ("awareness, not surveillance")

---

## Phase 1: Design & Contracts ✅ COMPLETE

**Status**: Data model and contracts defined  
**Artifacts**: 
- [data-model.md](./data-model.md)
- [contracts/UsageTrackingRepository.md](./contracts/UsageTrackingRepository.md)
- [contracts/UseCases.md](./contracts/UseCases.md)
- [quickstart.md](./quickstart.md)

### Data Model

**Core Entities**:
1. `UnlockEvent` (ephemeral) → aggregates into `DailyUnlockSummary`
2. `AppLaunchEvent` (ephemeral) → aggregates into `AppLaunchSummary`
3. `UsageData` (storage root) - persisted in SharedPreferences with JSON
4. `UsageAwarenessState` (UI state) - transient, not persisted

**Storage Schema**:
```json
{
  "currentDate": "2025-12-22",
  "unlockCount": 12,
  "lastUnlockTimestamp": 1703259600000,
  "appLaunches": {
    "com.android.chrome": {"launchCount": 8, "lastLaunchTimestamp": 1703259300000}
  }
}
```

**Reset Mechanism**: Lazy evaluation on read - compare stored date with `LocalDate.now()`

### Contracts

**Repository Interface** (`UsageTrackingRepository`):
- `recordUnlock()` - Increment unlock counter
- `recordAppLaunch(packageName)` - Increment app-specific counter
- `getDailyUnlockSummary()` - Retrieve unlock stats
- `getAppLaunchSummary(packageName)` - Retrieve app stats
- `clearAllData()` - Reset (for testing)

**Use Cases**:
- `TrackUnlockUseCase` - Records unlock events (graceful degradation)
- `TrackAppLaunchUseCase` - Records app launches (never blocks launch)
- `GetUsageStatsUseCase` - Retrieves formatted stats for UI

All use cases follow **graceful degradation**: catch exceptions, log errors, return defaults (never throw to UI).

### Source Code Structure

```
app/src/main/java/com/symbianx/minimalistlauncher/
├── domain/
│   ├── model/ (NEW: 4 files - DailyUnlockSummary, AppLaunchSummary, UsageData, UsageAwarenessState)
│   ├── repository/ (NEW: UsageTrackingRepository interface)
│   └── usecase/ (NEW: 3 use cases)
├── data/
│   ├── local/ (NEW: UsageTrackingDataSource + Impl)
│   └── repository/ (NEW: UsageTrackingRepositoryImpl)
├── ui/home/components/ (NEW: UnlockCountDisplay, AppLaunchOverlay)
└── util/ (NEW: TimeFormatter)
```

**Agent Context Updated**: `.github/agents/copilot-instructions.md` now includes:
- Kotlin 2.3.0, Java 17 target
- Jetpack Compose, kotlinx-serialization, AndroidX Core
- SharedPreferences + JSON pattern

---

## Phase 2: Task Breakdown 🚧 NOT STARTED

**Status**: Ready for `/speckit.tasks` command  
**Next Steps**: Run `/speckit.tasks` to generate implementation tasks from quickstart guide

The `/speckit.tasks` command will create:
- `specs/006-usage-awareness/tasks.md` with P1/P2/P3 prioritized task breakdown
- Individual implementation tasks following quickstart guide structure
- Test coverage requirements per component

**Implementation Order**:
1. **P1 Tasks**: Unlock tracking (home screen display)
2. **P2 Tasks**: App launch frequency tracking (overlay display)
3. **P3 Tasks**: Last launch time display (integrated with P2 overlay)

---

## Constitution Re-Check ✅ PASS

**Re-evaluation after Phase 1 design**:

The designed architecture maintains compliance with the project's design philosophy:

✅ **Privacy-First**: 
- All data stored locally in SharedPreferences (no cloud sync)
- No telemetry, no external transmission
- Current day only (no long-term surveillance)

✅ **No Invasive Permissions**:
- Uses `ACTION_USER_PRESENT` broadcast (no special permissions)
- No `SYSTEM_ALERT_WINDOW` (overlay permission)
- No `PACKAGE_USAGE_STATS` (accessibility permission)
- No accessibility services abuse

✅ **Graceful Degradation**:
- All tracking failures caught and logged
- Tracking never blocks user actions (FR-021)
- App launches proceed regardless of tracking state

✅ **Minimal Complexity**:
- Follows existing SharedPreferences + JSON pattern (FavoritesDataSource)
- No background services, no AlarmManager, no WorkManager
- Lazy date-based resets (simple, reliable)

✅ **Non-Judgmental Design**:
- Factual numeric displays only ("12 unlocks today")
- Neutral colors, small text, minimal prominence
- No warning colors, no guilt-inducing language

**Conclusion**: Architecture aligns with "awareness, not guilt" principle and privacy-first approach.

---

## Summary

**Planning Phase Complete** ✅

- ✅ Research resolved all technical unknowns
- ✅ Data model defined (5 entities, JSON storage schema)
- ✅ Contracts established (repository + 3 use cases)
- ✅ Quickstart guide created (P1/P2/P3 implementation order)
- ✅ Agent context updated (Copilot now aware of tech stack)
- ✅ Constitution check passed (privacy-first, no invasive permissions)

**Ready for Implementation**: Run `/speckit.tasks` to generate task breakdown.

**Branch**: `006-usage-awareness`  
**Artifacts**: All planning documents in `specs/006-usage-awareness/`

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
