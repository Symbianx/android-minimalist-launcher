# Tasks: Usage Awareness

**Input**: Design documents from `/specs/006-usage-awareness/`  
**Branch**: `006-usage-awareness`  
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/, quickstart.md

**Tests**: This feature does NOT explicitly request TDD or test-first development. Unit tests will be added in the Polish phase.

**Organization**: Tasks are grouped by user story (P1, P2, P3) to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Android single application structure:
- `app/src/main/java/com/symbianx/minimalistlauncher/` - Main source
- `app/src/test/java/com/symbianx/minimalistlauncher/` - Unit tests

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization - no additional setup needed (project already exists)

- [x] T001 Verify kotlinx-serialization plugin is configured in app/build.gradle.kts
- [x] T002 Verify java.time.LocalDate is available (API 26+ requirement met)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core utilities that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T003 Create TimeFormatter utility in app/src/main/java/com/symbianx/minimalistlauncher/util/TimeFormatter.kt
- [x] T004 [P] Create UsageData storage model in app/src/main/java/com/symbianx/minimalistlauncher/data/local/UsageData.kt
- [x] T005 [P] Create AppUsageData storage model in app/src/main/java/com/symbianx/minimalistlauncher/data/local/UsageData.kt (same file as T004)
- [x] T006 Create UsageTrackingDataSource interface in app/src/main/java/com/symbianx/minimalistlauncher/data/local/UsageTrackingDataSource.kt
- [x] T007 Implement UsageTrackingDataSourceImpl with SharedPreferences in app/src/main/java/com/symbianx/minimalistlauncher/data/local/UsageTrackingDataSourceImpl.kt

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Daily Unlock Awareness (Priority: P1) 🎯 MVP

**Goal**: Display unlock count and last unlock time on home screen. User becomes aware of phone-checking habits.

**Independent Test**: Lock/unlock phone multiple times, verify unlock count increments and "last unlock" time updates on home screen.

**Acceptance Criteria**:
- Unlock count visible on home screen
- Last unlock time shows relative format ("just now", "2h ago")
- Count resets at midnight
- Data persists across reboots

### Domain Layer for User Story 1

- [x] T008 [P] [US1] Create DailyUnlockSummary model in app/src/main/java/com/symbianx/minimalistlauncher/domain/model/DailyUnlockSummary.kt
- [x] T009 [P] [US1] Create UsageTrackingRepository interface in app/src/main/java/com/symbianx/minimalistlauncher/domain/repository/UsageTrackingRepository.kt
- [x] T010 [P] [US1] Create TrackUnlockUseCase interface in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/TrackUnlockUseCase.kt

### Data Layer for User Story 1

- [x] T011 [US1] Implement UsageTrackingRepositoryImpl with recordUnlock and getDailyUnlockSummary in app/src/main/java/com/symbianx/minimalistlauncher/data/repository/UsageTrackingRepositoryImpl.kt
- [x] T012 [US1] Implement TrackUnlockUseCaseImpl with graceful error handling in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/TrackUnlockUseCaseImpl.kt

### UI Layer for User Story 1

- [x] T013 [US1] Create UnlockCountDisplay composable in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/UnlockCountDisplay.kt
- [x] T014 [US1] Register ACTION_USER_PRESENT broadcast receiver in MainActivity.kt to trigger TrackUnlockUseCase
- [x] T015 [US1] Update HomeViewModel to expose unlock stats (unlockCount, lastUnlockTimeAgo) using GetUsageStatsUseCase
- [x] T016 [US1] Integrate UnlockCountDisplay into HomeScreen composable at top-left corner with minimal styling

**Checkpoint**: User Story 1 complete - unlock tracking visible on home screen, data persists across reboots

---

## Phase 4: User Story 2 - App Launch Frequency Awareness (Priority: P2)

**Goal**: Show brief overlay with launch count when opening apps. User sees "8th time today" before app launches.

**Independent Test**: Launch same app multiple times from launcher, verify count increments and displays correctly each time (0.5-1 second display before app opens).

**Acceptance Criteria**:
- Launch count displays when tapping app
- Count is app-specific (Instagram count ≠ Gmail count)
- Overlay auto-dismisses after 800ms
- App still launches even if tracking fails
- Count resets at midnight

### Domain Layer for User Story 2

- [x] T017 [P] [US2] Create AppLaunchSummary model in app/src/main/java/com/symbianx/minimalistlauncher/domain/model/AppLaunchSummary.kt
- [x] T018 [P] [US2] Create TrackAppLaunchUseCase interface in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/TrackAppLaunchUseCase.kt
- [x] T019 [P] [US2] Create GetUsageStatsUseCase interface in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/GetUsageStatsUseCase.kt

### Data Layer for User Story 2

- [x] T020 [US2] Add recordAppLaunch and getAppLaunchSummary methods to UsageTrackingRepositoryImpl in app/src/main/java/com/symbianx/minimalistlauncher/data/repository/UsageTrackingRepositoryImpl.kt
- [x] T021 [US2] Implement TrackAppLaunchUseCaseImpl with graceful error handling in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/TrackAppLaunchUseCaseImpl.kt
- [x] T022 [US2] Implement GetUsageStatsUseCaseImpl with getHomeScreenStats and getAppStats methods in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/GetUsageStatsUseCaseImpl.kt

### UI Layer for User Story 2

- [x] T023 [US2] Create AppLaunchOverlay composable with auto-dismiss in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/AppLaunchOverlay.kt
- [x] T024 [US2] Add formatLaunchCount helper function to AppLaunchOverlay (converts count to "1st", "2nd", "8th" format)
- [x] T025 [US2] Update LaunchAppUseCase to track app launch and show overlay before startActivity in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/LaunchAppUseCaseImpl.kt
- [x] T026 [US2] Update HomeViewModel to manage AppLaunchOverlay state (visible/hidden) with 800ms auto-dismiss timer
- [x] T027 [US2] Integrate AppLaunchOverlay into HomeScreen composable (displayed as modal when visible)

**Checkpoint**: User Story 2 complete - app launch overlay displays frequency, works independently of unlock tracking

---

## Phase 5: User Story 3 - Last App Launch Time Awareness (Priority: P3)

**Goal**: Display "last opened 20m ago" in app launch overlay. User notices compulsive checking patterns.

**Independent Test**: Launch app, wait varying amounts of time (1 min, 15 min, 2 hours), launch again and verify timestamp updates correctly showing "Xm ago" or "Xh ago".

**Acceptance Criteria**:
- Last launch time displays in overlay alongside frequency
- Shows "first time today" if never launched today
- Shows relative time format ("15m ago", "3h ago")
- Yesterday's data doesn't carry over

### Implementation for User Story 3

- [x] T028 [US3] Update GetUsageStatsUseCaseImpl.getAppStats to include lastLaunchTimeAgo formatting using TimeFormatter in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/GetUsageStatsUseCaseImpl.kt
- [x] T029 [US3] Update AppLaunchOverlay to display lastLaunchTimeAgo below launch count in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/AppLaunchOverlay.kt
- [x] T030 [US3] Verify TimeFormatter handles null/zero timestamps returning null (displays "first time today")

**Checkpoint**: All user stories complete - full usage awareness feature functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Quality improvements, testing, and documentation

- [x] T031 [P] Add unit tests for TimeFormatter in app/src/test/java/com/symbianx/minimalistlauncher/util/TimeFormatterTest.kt
- [x] T032 [P] Add unit tests for UsageTrackingDataSourceImpl (day boundary resets, corruption handling) in app/src/test/java/com/symbianx/minimalistlauncher/data/local/UsageTrackingDataSourceImplTest.kt
- [x] T033 [P] Add unit tests for UsageTrackingRepositoryImpl in app/src/test/java/com/symbianx/minimalistlauncher/data/repository/UsageTrackingRepositoryImplTest.kt
- [x] T034 [P] Add unit tests for TrackUnlockUseCaseImpl (graceful degradation) in app/src/test/java/com/symbianx/minimalistlauncher/domain/usecase/TrackUnlockUseCaseTest.kt
- [x] T035 [P] Add unit tests for TrackAppLaunchUseCaseImpl in app/src/test/java/com/symbianx/minimalistlauncher/domain/usecase/TrackAppLaunchUseCaseTest.kt
- [x] T036 [P] Add unit tests for GetUsageStatsUseCaseImpl in app/src/test/java/com/symbianx/minimalistlauncher/domain/usecase/GetUsageStatsUseCaseTest.kt
- [x] T037 Verify unlock count displays with neutral gray color and small font size (11sp, non-intrusive)
- [x] T038 Verify app launch overlay uses semi-transparent black background (0.3 alpha) and fades in/out smoothly
- [x] T039 Manual test: Lock/unlock phone 10+ times, verify count and timestamps on home screen
- [x] T040 Manual test: Launch same app 5+ times, verify overlay shows correct count and last opened time
- [x] T041 Manual test: Change device time to 23:59, wait for midnight, verify counts reset to 0
- [x] T042 Manual test: Reboot device mid-day, verify unlock and app launch counts persist
- [x] T043 Run ktlint and fix any formatting issues
- [x] T044 Verify 60fps maintained during overlay animations and home screen rendering
- [x] T045 Update README.md to document usage awareness feature in Features section

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational phase completion - can start immediately after
- **User Story 2 (Phase 4)**: Depends on Foundational phase completion - can run in parallel with US1
- **User Story 3 (Phase 5)**: Depends on US2 completion (extends overlay) - integrates with US2
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: No dependencies on other stories - independently testable
- **User Story 2 (P2)**: No dependencies on US1 - independently testable (works without unlock tracking)
- **User Story 3 (P3)**: Extends US2 overlay - requires US2 complete, but doesn't require US1

### Within Each User Story

- Domain models before repositories
- Repositories before use cases
- Use cases before UI components
- UI components integration last

### Parallel Opportunities

**Foundational Phase (after Setup)**:
- T003 (TimeFormatter) in parallel with T004+T005 (storage models)
- T006 (DataSource interface) can start after T004+T005

**User Story 1 - Domain Layer**:
- T008, T009, T010 can all run in parallel (different files)

**User Story 2 - Domain Layer**:
- T017, T018, T019 can all run in parallel (different files)

**Polish Phase - All Tests**:
- T031-T036 can all run in parallel (different test files)

**Team Parallelization**:
- After Foundational phase complete, US1 and US2 can be developed in parallel by different developers
- US3 must wait for US2 but is small (only 3 tasks)

---

## Parallel Example: User Story 1

```bash
# Launch all domain models for US1 together:
Task: "Create DailyUnlockSummary model in .../domain/model/DailyUnlockSummary.kt"
Task: "Create UsageTrackingRepository interface in .../domain/repository/UsageTrackingRepository.kt"
Task: "Create TrackUnlockUseCase interface in .../domain/usecase/TrackUnlockUseCase.kt"
```

---

## Parallel Example: User Story 2

```bash
# Launch all domain models for US2 together:
Task: "Create AppLaunchSummary model in .../domain/model/AppLaunchSummary.kt"
Task: "Create TrackAppLaunchUseCase interface in .../domain/usecase/TrackAppLaunchUseCase.kt"
Task: "Create GetUsageStatsUseCase interface in .../domain/usecase/GetUsageStatsUseCase.kt"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (verify dependencies)
2. Complete Phase 2: Foundational (TimeFormatter + storage infrastructure)
3. Complete Phase 3: User Story 1 (unlock tracking)
4. **STOP and VALIDATE**: Test unlock tracking independently
   - Lock/unlock phone 10 times
   - Verify count displays on home screen
   - Verify "last unlock" time updates
   - Reboot device, verify count persists
5. Deploy/demo MVP

**MVP Delivers**: Users see their unlock count and become aware of phone-checking habits

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo (app launch awareness)
4. Add User Story 3 → Test independently → Deploy/Demo (complete feature)
5. Polish phase → Final quality improvements

Each story adds value without breaking previous stories.

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (unlock tracking)
   - Developer B: User Story 2 (app launch tracking)
3. Developer A or B: User Story 3 (extends US2 - small, 3 tasks)
4. Team: Polish phase together

---

## Task Summary

**Total Tasks**: 45

**By Phase**:
- Setup: 2 tasks
- Foundational: 5 tasks (BLOCKS all stories)
- User Story 1 (P1 - MVP): 9 tasks
- User Story 2 (P2): 11 tasks
- User Story 3 (P3): 3 tasks
- Polish: 15 tasks

**By Story**:
- US1 (Unlock tracking): 9 tasks
- US2 (App launch frequency): 11 tasks
- US3 (Last launch time): 3 tasks
- Infrastructure: 7 tasks
- Testing/Polish: 15 tasks

**Parallel Opportunities**:
- 9 tasks marked [P] can run in parallel within their phase
- US1 and US2 can be developed in parallel after Foundational phase
- All unit tests (6 tasks) can run in parallel

**Independent Test Criteria**:
- US1: Lock/unlock phone multiple times → count updates on home screen
- US2: Launch same app repeatedly → overlay shows count before app opens
- US3: Launch app with delays → overlay shows "Xm ago" / "Xh ago"

**Suggested MVP Scope**: User Story 1 only (9 tasks after foundation = ~11 total tasks)

---

## Notes

- [P] tasks = different files, no dependencies within phase
- [Story] label maps task to specific user story for traceability
- Each user story is independently completable and testable
- Tests are in Polish phase (not TDD - not requested in spec)
- Stop at any checkpoint to validate story independently
- Focus on MVP first: unlock tracking delivers immediate value
- UnlockCountDisplay uses neutral gray, 11sp font (non-intrusive per FR-022)
- AppLaunchOverlay auto-dismisses after 800ms (meets 0.5-1 second requirement)
- All tracking uses graceful degradation - failures never block user (FR-021)
- SharedPreferences pattern matches existing FavoritesDataSource implementation
