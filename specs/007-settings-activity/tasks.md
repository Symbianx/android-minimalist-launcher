# Tasks: Settings Activity

**Input**: Design documents from `/specs/007-settings-activity/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/settings-repository.md

**Tests**: Following test-first development approach as required by Constitution. All tests must be written FIRST and FAIL before implementation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4, US5)
- Include exact file paths in descriptions

## Path Conventions

- Android project: `app/src/main/java/com/symbianx/minimalistlauncher/`
- Test files: `app/src/test/java/com/symbianx/minimalistlauncher/`
- Instrumented tests: `app/src/androidTest/java/com/symbianx/minimalistlauncher/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, dependencies, and package structure

- [x] T001 Add DataStore Preferences dependency to app/build.gradle.kts
- [x] T002 [P] Create package structure app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/
- [x] T003 [P] Create package structure app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/
- [x] T004 [P] Create package structure app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/
- [x] T005 [P] Create package structure app/src/main/java/com/symbianx/minimalistlauncher/data/local/
- [x] T006 [P] Create package structure app/src/test/java/com/symbianx/minimalistlauncher/domain/usecase/
- [x] T007 [P] Create package structure app/src/test/java/com/symbianx/minimalistlauncher/data/repository/
- [x] T008 [P] Create package structure app/src/test/java/com/symbianx/minimalistlauncher/data/local/
- [x] T009 [P] Create package structure app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core models, repository interface, and logger that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T010 [P] Create LauncherSettings data model in app/src/main/java/com/symbianx/minimalistlauncher/domain/model/LauncherSettings.kt
- [x] T011 [P] Create QuickActionConfig data model in app/src/main/java/com/symbianx/minimalistlauncher/domain/model/QuickActionConfig.kt
- [x] T012 [P] Create BatteryThresholdMode enum in app/src/main/java/com/symbianx/minimalistlauncher/domain/model/BatteryThresholdMode.kt
- [x] T013 [P] Create SettingsRepository interface in app/src/main/java/com/symbianx/minimalistlauncher/domain/repository/SettingsRepository.kt
- [x] T014 [P] Create SettingsDataSource interface in app/src/main/java/com/symbianx/minimalistlauncher/data/local/SettingsDataSource.kt
- [x] T015 [P] Create SettingsLogger utility in app/src/main/java/com/symbianx/minimalistlauncher/util/SettingsLogger.kt
- [x] T016 Unit test for LauncherSettings defaults in app/src/test/java/com/symbianx/minimalistlauncher/domain/model/LauncherSettingsTest.kt
- [x] T017 [P] Unit test for BatteryThresholdMode.shouldShow() in app/src/test/java/com/symbianx/minimalistlauncher/domain/model/BatteryThresholdModeTest.kt

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Access Settings from Home Screen (Priority: P1) 🎯 MVP

**Goal**: Enable users to access settings via long-press on home screen background

**Independent Test**: Long-press home screen background → settings menu/activity opens → settings screen displays

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T018 [P] [US1] Unit test for SettingsDataSourceImpl write/read round-trip in app/src/test/java/com/symbianx/minimalistlauncher/data/local/SettingsDataSourceImplTest.kt
- [x] T019 [P] [US1] Contract test for SettingsRepository.getSettings() default emission in app/src/test/java/com/symbianx/minimalistlauncher/data/repository/SettingsRepositoryContractTest.kt
- [x] T020 [P] [US1] Unit test for LoadSettingsUseCase in app/src/test/java/com/symbianx/minimalistlauncher/domain/usecase/LoadSettingsUseCaseTest.kt
- [x] T021 [P] [US1] UI test for SettingsActivity launch from HomeScreen long-press in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/SettingsActivityLaunchTest.kt
- [x] T022 [P] [US1] UI test for SettingsScreen displays all settings sections in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/SettingsScreenTest.kt

### Implementation for User Story 1

- [x] T023 [US1] Implement SettingsDataSourceImpl with DataStore in app/src/main/java/com/symbianx/minimalistlauncher/data/local/SettingsDataSourceImpl.kt
- [x] T024 [US1] Implement SettingsRepositoryImpl with validation in app/src/main/java/com/symbianx/minimalistlauncher/data/repository/SettingsRepositoryImpl.kt
- [x] T025 [P] [US1] Implement LoadSettingsUseCase in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/LoadSettingsUseCase.kt
- [x] T026 [US1] Create SettingsUiState sealed interface in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/SettingsUiState.kt
- [x] T027 [US1] Create SettingsViewModel with settings loading in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModel.kt
- [x] T028 [US1] Create SettingsActivity with Compose setup in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/SettingsActivity.kt
- [x] T029 [US1] Create SettingsScreen composable with scaffold in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt
- [x] T030 [US1] Register SettingsActivity in AndroidManifest.xml
- [x] T031 [US1] Add long-press gesture detection to HomeScreen background in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeScreen.kt
- [x] T032 [US1] Update HomeScreen to launch SettingsActivity on long-press

**Checkpoint**: At this point, User Story 1 should be fully functional - users can access settings screen

---

## Phase 4: User Story 2 - Configure Auto-Launch Behavior (Priority: P2)

**Goal**: Enable users to toggle auto-launch feature on/off

**Independent Test**: Toggle auto-launch setting → search for unique app → verify launch behavior matches setting

### Tests for User Story 2

- [x] T033 [P] [US2] Unit test for SaveSettingsUseCase auto-launch update in app/src/test/java/com/symbianx/minimalistlauncher/domain/usecase/SaveSettingsUseCaseTest.kt
- [x] T034 [P] [US2] Contract test for SettingsRepository.updateSettings() in app/src/test/java/com/symbianx/minimalistlauncher/data/repository/SettingsRepositoryContractTest.kt
- [x] T035 [P] [US2] Integration test for settings persistence across app restart in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/SettingsPersistenceTest.kt
- [x] T036 [P] [US2] UI test for auto-launch switch toggle in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/AutoLaunchToggleTest.kt
- [x] T037 [P] [US2] Integration test for auto-launch behavior respects setting in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/home/AutoLaunchIntegrationTest.kt

### Implementation for User Story 2

- [x] T038 [US2] Implement SaveSettingsUseCase in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/SaveSettingsUseCase.kt
- [x] T039 [US2] Add updateAutoLaunch() method to SettingsViewModel in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModel.kt
- [x] T040 [US2] Create SwitchPreference composable in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SwitchPreference.kt
- [x] T041 [US2] Add auto-launch switch to SettingsScreen in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt
- [x] T042 [US2] Load settings in HomeViewModel in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeViewModel.kt
- [x] T043 [US2] Update auto-launch logic in SearchView to respect settings in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/SearchView.kt

**Checkpoint**: Auto-launch can be toggled and behavior changes accordingly

---

## Phase 5: User Story 3 - Customize Quick Action Buttons (Priority: P2)

**Goal**: Enable users to customize which apps appear on bottom left/right quick action buttons

**Independent Test**: Change quick action button apps in settings → return to home screen → verify buttons launch selected apps

### Tests for User Story 3

- [x] T044 [P] [US3] Contract test for quick action app validation in app/src/test/java/com/symbianx/minimalistlauncher/data/repository/SettingsRepositoryQuickActionTest.kt
- [x] T045 [P] [US3] Unit test for uninstalled app recovery logic in app/src/test/java/com/symbianx/minimalistlauncher/data/repository/SettingsRepositoryUninstalledAppTest.kt
- [x] T046 [P] [US3] Unit test for SettingsViewModel.updateLeftQuickAction() in app/src/test/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModelTest.kt
- [x] T047 [P] [US3] Unit test for SettingsViewModel.updateRightQuickAction() in app/src/test/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModelTest.kt
- [x] T048 [P] [US3] UI test for app picker dialog display in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/AppPickerDialogTest.kt
- [x] T049 [P] [US3] Integration test for quick action customization flow in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/QuickActionCustomizationTest.kt
- [x] T050 [P] [US3] Integration test for uninstalled app auto-recovery in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/UninstalledAppRecoveryTest.kt

### Implementation for User Story 3

- [x] T051 [P] [US3] Create AppPickerTarget enum in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/AppPickerTarget.kt
- [x] T052 [US3] Add updateLeftQuickAction() to SettingsViewModel in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModel.kt
- [x] T053 [US3] Add updateRightQuickAction() to SettingsViewModel in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModel.kt
- [x] T054 [US3] Add app picker state management to SettingsViewModel in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModel.kt
- [x] T055 [US3] Create PreferenceItem composable for clickable settings in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/PreferenceItem.kt
- [x] T056 [US3] Create AppPickerDialog composable with search in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/AppPickerDialog.kt
- [x] T057 [US3] Add left quick action preference to SettingsScreen in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt
- [x] T058 [US3] Add right quick action preference to SettingsScreen in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt
- [x] T059 [US3] Add app picker dialog to SettingsScreen in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt
- [x] T060 [US3] Update QuickActionButtons to accept QuickActionConfig parameters in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/QuickActionButtons.kt
- [x] T061 [US3] Pass custom quick action configs from HomeScreen in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeScreen.kt

**Checkpoint**: Quick action buttons can be customized and launch selected apps

---

## Phase 6: User Story 4 - Configure Battery Indicator Visibility (Priority: P3)

**Goal**: Enable users to control when battery indicator appears (always/below 50%/below 20%/never)

**Independent Test**: Change battery indicator mode → verify indicator visibility matches threshold at different battery levels

### Tests for User Story 4

- [x] T062 [P] [US4] Unit test for SettingsViewModel.updateBatteryMode() in app/src/test/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModelBatteryTest.kt
- [x] T063 [P] [US4] Contract test for battery mode persistence in app/src/test/java/com/symbianx/minimalistlauncher/data/repository/SettingsRepositoryBatteryModeTest.kt
- [x] T064 [P] [US4] UI test for battery threshold picker in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/BatteryThresholdPickerTest.kt
- [x] T065 [P] [US4] Integration test for battery indicator visibility logic in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/home/BatteryIndicatorVisibilityTest.kt

### Implementation for User Story 4

- [x] T066 [US4] Add updateBatteryMode() to SettingsViewModel in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModel.kt
- [x] T067 [US4] Create RadioGroupPreference composable in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/RadioGroupPreference.kt
- [x] T068 [US4] Create BatteryThresholdPicker composable in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/BatteryThresholdPicker.kt
- [x] T069 [US4] Add battery indicator preference to SettingsScreen in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt
- [x] T070 [US4] Update CircularBatteryIndicator to accept threshold mode in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/CircularBatteryIndicator.kt
- [x] T071 [US4] Pass battery threshold mode from HomeScreen in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeScreen.kt

**Checkpoint**: Battery indicator visibility can be configured and respects threshold settings

---

## Phase 7: User Story 5 - Reset Settings to Defaults (Priority: P3)

**Goal**: Enable users to reset all settings to default values with confirmation

**Independent Test**: Customize multiple settings → trigger reset → confirm action → verify all settings return to defaults

### Tests for User Story 5

- [x] T072 [P] [US5] Unit test for ResetSettingsUseCase in app/src/test/java/com/symbianx/minimalistlauncher/domain/usecase/ResetSettingsUseCaseTest.kt
- [x] T073 [P] [US5] Contract test for SettingsRepository.resetToDefaults() in app/src/test/java/com/symbianx/minimalistlauncher/data/repository/SettingsRepositoryResetTest.kt
- [x] T074 [P] [US5] Integration test for complete reset flow in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/SettingsResetIntegrationTest.kt
- [x] T075 [P] [US5] UI test for reset confirmation dialog in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/ResetConfirmationDialogTest.kt

### Implementation for User Story 5

- [x] T076 [US5] Implement ResetSettingsUseCase in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/ResetSettingsUseCase.kt
- [x] T077 [US5] Add resetToDefaults() to SettingsViewModel in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModel.kt
- [x] T078 [US5] Add reset dialog state management to SettingsViewModel in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModel.kt
- [x] T079 [US5] Create ResetConfirmationDialog composable in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/ResetConfirmationDialog.kt
- [x] T080 [US5] Add reset preference to SettingsScreen in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt
- [x] T081 [US5] Add reset confirmation dialog to SettingsScreen in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt

**Checkpoint**: All settings can be reset to defaults with confirmation

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and final integration

- [x] T082 [P] Add error handling and user feedback for DataStore write failures in app/src/main/java/com/symbianx/minimalistlauncher/data/local/SettingsDataSourceImpl.kt
- [x] T083 [P] Add corrupted data recovery handler to SettingsDataSourceImpl in app/src/main/java/com/symbianx/minimalistlauncher/data/local/SettingsDataSourceImpl.kt
- [x] T084 [P] Add logging for all settings changes via SettingsLogger in app/src/main/java/com/symbianx/minimalistlauncher/util/SettingsLogger.kt
- [x] T085 [P] Add toast notifications for uninstalled app recovery in app/src/main/java/com/symbianx/minimalistlauncher/data/repository/SettingsRepositoryImpl.kt
- [x] T086 [P] Add Material3 dividers between settings sections in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt
- [x] T087 [P] Add loading state animation to SettingsScreen in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt
- [x] T088 [P] Add error state UI to SettingsScreen in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt
- [x] T089 [P] Optimize app picker dialog scroll performance in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/AppPickerDialog.kt
- [x] T090 [P] Add settings descriptions/help text to all preferences in app/src/main/java/com/symbianx/minimalistlauncher/ui/settings/components/SettingsScreen.kt
- [x] T091 [P] Unit test for SettingsViewModel complete state management in app/src/test/java/com/symbianx/minimalistlauncher/ui/settings/SettingsViewModelCompleteTest.kt
- [x] T092 [P] End-to-end integration test for all user stories in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/settings/SettingsE2ETest.kt
- [x] T093 Run all quickstart.md validation scenarios
- [x] T094 [P] Update README.md with settings feature documentation
- [x] T095 Code review and cleanup across all settings components

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P2 → P3 → P3)
- **Polish (Phase 8)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - Establishes settings access pattern
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Independently adds auto-launch toggle
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Independently adds quick action customization
- **User Story 4 (P3)**: Can start after Foundational (Phase 2) - Independently adds battery indicator config
- **User Story 5 (P3)**: Can start after Foundational (Phase 2) - Independently adds reset functionality

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Use cases before ViewModel integration
- ViewModel methods before UI components
- UI components before integration with existing screens
- Story complete before moving to next priority

### Parallel Opportunities

**Phase 1 - Setup**: All package structure tasks (T002-T009) can run in parallel

**Phase 2 - Foundational**: Models (T010-T012), interfaces (T013-T014), logger (T015), and tests (T016-T017) can run in parallel

**Within Each User Story**:
- All tests marked [P] can be written in parallel
- Multiple implementation tasks marked [P] can run in parallel

**Across User Stories**: Once Phase 2 completes, all user stories (Phase 3-7) can be developed in parallel by different developers

---

## Parallel Example: User Story 1

```bash
# Write all tests for User Story 1 together:
Task T018: "Unit test for SettingsDataSourceImpl write/read round-trip"
Task T019: "Contract test for SettingsRepository.getSettings() default emission"
Task T020: "Unit test for LoadSettingsUseCase"
Task T021: "UI test for SettingsActivity launch from HomeScreen long-press"
Task T022: "UI test for SettingsScreen displays all settings sections"

# After tests fail, implement foundation in parallel:
Task T025: "Implement LoadSettingsUseCase"
Task T026: "Create SettingsUiState sealed interface"
```

---

## Parallel Example: User Story 3

```bash
# Write all tests for User Story 3 together:
Task T044: "Contract test for quick action app validation"
Task T045: "Unit test for uninstalled app recovery logic"
Task T046: "Unit test for SettingsViewModel.updateLeftQuickAction()"
Task T047: "Unit test for SettingsViewModel.updateRightQuickAction()"
Task T048: "UI test for app picker dialog display"
Task T049: "Integration test for quick action customization flow"
Task T050: "Integration test for uninstalled app auto-recovery"

# After tests fail, implement in parallel:
Task T051: "Create AppPickerTarget enum"
Task T055: "Create PreferenceItem composable"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup → Dependencies and structure ready
2. Complete Phase 2: Foundational → Core models and interfaces ready (CRITICAL)
3. Complete Phase 3: User Story 1 → Users can access settings screen
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo basic settings access

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP - settings access!)
3. Add User Story 2 → Test independently → Deploy/Demo (auto-launch control)
4. Add User Story 3 → Test independently → Deploy/Demo (quick actions customization)
5. Add User Story 4 → Test independently → Deploy/Demo (battery indicator control)
6. Add User Story 5 → Test independently → Deploy/Demo (reset functionality)
7. Polish → Final integration and refinement
8. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done (after T017):
   - Developer A: User Story 1 (Settings Access)
   - Developer B: User Story 2 (Auto-Launch)
   - Developer C: User Story 3 (Quick Actions)
   - Developer D: User Story 4 (Battery Indicator)
   - Developer E: User Story 5 (Reset)
3. Stories complete and integrate independently
4. Team reconvenes for Polish phase

---

## Summary

**Total Tasks**: 95
**Task Breakdown by User Story**:
- Setup: 9 tasks
- Foundational: 8 tasks
- User Story 1 (Access Settings): 15 tasks (5 tests + 10 implementation)
- User Story 2 (Auto-Launch Config): 11 tasks (5 tests + 6 implementation)
- User Story 3 (Quick Actions): 18 tasks (7 tests + 11 implementation)
- User Story 4 (Battery Indicator): 10 tasks (4 tests + 6 implementation)
- User Story 5 (Reset Settings): 10 tasks (4 tests + 6 implementation)
- Polish: 14 tasks

**Parallelizable Tasks**: 51 tasks marked with [P] for parallel execution

**Independent Test Criteria**:
- US1: Long-press opens settings → all sections visible
- US2: Toggle auto-launch → search behavior changes
- US3: Change quick action apps → buttons launch selected apps
- US4: Change battery mode → indicator visibility matches threshold
- US5: Reset all settings → defaults restored

**Suggested MVP Scope**: Phase 1 + Phase 2 + Phase 3 (User Story 1 only)
- Delivers: Settings screen access via long-press, basic settings display
- Validates: Architecture, DataStore integration, UI framework
- Time estimate: ~3-5 days for single developer

---

## Notes

- [P] tasks = different files, no dependencies, safe to parallelize
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- ALL tests must fail before implementing corresponding features (TDD)
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Constitution compliance: Test-first approach, reuse existing patterns (DataStore mirrors FavoritesDataSourceImpl), observability via SettingsLogger
