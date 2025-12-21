---
description: "Task list for App Context Menu"
---

# Tasks: App Context Menu

**Input**: Design documents from `/specs/004-app-context-menu/`
**Prerequisites**: plan.md (required), spec.md (required for user stories)

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create/verify Android project structure for feature in app/src/
- [X] T002 [P] Ensure Material3 ModalBottomSheet dependencies are available in app/build.gradle.kts
- [X] T003 [P] Configure linting and formatting (ktlint, detekt) in project root

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

- [X] T004 [P] Add logging utility for context menu events in app/src/main/java/com/symbianx/minimalistlauncher/util/ContextMenuLogger.kt
- [X] T005 [P] Add long-press gesture detection to AppListItem in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/AppListItem.kt
- [X] T006 Create AppContextMenu composable component in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/AppContextMenu.kt

---

## Phase 3: User Story 1 – Add/Remove Favorites via Context Menu (P1) 🎯 MVP

**Goal**: Long-press app in search results opens context menu with Add/Remove from Favorites option that works correctly
**Independent Test**: Can be tested by long-pressing any app, selecting Add/Remove from Favorites, and verifying home screen favorites list updates

### Implementation for User Story 1

- [X] T007 [US1] Add state management for context menu (open/closed, selected app) to HomeViewModel in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeViewModel.kt
- [X] T008 [US1] Wire AppContextMenu to SearchView with menu state callbacks in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeScreen.kt
- [X] T009 [US1] Implement "Add to Favorites" menu action using ManageFavoritesUseCase in AppContextMenu.kt
- [X] T010 [US1] Implement "Remove from Favorites" menu action (show based on isFavorite state) in AppContextMenu.kt
- [X] T011 [US1] Add haptic feedback on long-press detection in AppListItem.kt
- [X] T012 [US1] Add dismiss behavior (tap outside, back button) to AppContextMenu.kt
 - [X] T013 [P] [US1] UI test: long-press opens context menu in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/home/AppContextMenuTest.kt
 - [X] T014 [P] [US1] UI test: add to favorites via context menu in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/home/AppContextMenuTest.kt
 - [X] T015 [P] [US1] UI test: remove from favorites via context menu in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/home/AppContextMenuTest.kt

**Checkpoint**: At this point, User Story 1 should be fully functional - users can add/remove favorites via context menu

---

## Phase 4: User Story 2 – Open App Info from Context Menu (P2)

**Goal**: Selecting "Go to App Info" from context menu opens the Android system app info screen
**Independent Test**: Can be tested by long-pressing any app, selecting "Go to App Info", and verifying system app settings screen opens

### Implementation for User Story 2

 - [X] T016 [US2] Implement openAppInfo() function in HomeViewModel using ACTION_APPLICATION_DETAILS_SETTINGS intent in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeViewModel.kt
 - [X] T017 [US2] Add "Go to App Info" menu option to AppContextMenu with callback to openAppInfo() in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/AppContextMenu.kt
 - [X] T018 [US2] Add error handling for failed app info intent (app not found, uninstalled) in HomeViewModel.kt
 - [X] T019 [P] [US2] UI test: go to app info from context menu in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/home/AppContextMenuTest.kt
 - [X] T020 [P] [US2] Unit test: openAppInfo() intent creation in app/src/test/java/com/symbianx/minimalistlauncher/ui/home/HomeViewModelTest.kt

**Checkpoint**: At this point, User Story 2 is complete - context menu provides access to system app info

---

## Final Phase: Polish & Cross-Cutting Concerns

 - [X] T021 [P] Add context menu animations (fade in/slide up) using AnimationUtil
 - [X] T022 [P] Ensure accessibility: TalkBack support with proper content descriptions
 - [X] T023 [P] Update quickstart.md with context menu usage instructions
 - [X] T024 [P] Manual QA: verify feature on physical device and emulator

---

## Dependencies

- Phase 1 and 2 must be complete before any user story phases
- US1 (P1) → US2 (P2) (can be delivered incrementally)

## Parallel Execution Examples

- T002, T003, T004 can be done in parallel
- T007, T009, T011 can be done in parallel (different aspects of US1)
- T013, T014, T015, T019, T020 can be run in parallel after core logic is in place
- T021, T022, T023, T024 can be done in parallel

## Implementation Strategy

- MVP: Complete all US1 (P1) tasks for favorites management via context menu
- Incrementally deliver US2 (P2) for app info access
- Final phase adds polish and documentation

---

All tasks follow the strict checklist format and are independently testable per user story.
