# Implementation Tasks: Minimalist Android Launcher

**Branch**: `001-minimalist-launcher`  
**Date**: 2025-12-19  
**Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md)

## Overview

This document breaks down the implementation into phases organized by user story. Most foundational work (User Story 1, 2, 3, 4) is already complete. Remaining work focuses on **User Story 5 (Favorites)** and refinements to meet the updated specification requirements.

**Current Implementation Status**:
- ✅ User Story 1 (Search) - Implemented with swipe gesture, real-time filtering
- ✅ User Story 2 (Status Display) - Implemented with time, battery, Now Playing
- ✅ User Story 3 (Launcher Registration) - Complete with HOME intent filter
- ✅ User Story 4 (Now Playing) - Implemented with graceful degradation
- ⚠️ User Story 5 (Favorites) - **NOT IMPLEMENTED** - Primary focus of remaining tasks
- ⚠️ Refinements Needed - Date display, battery positioning, 64dp touch targets

---

## Phase 1: Refinements to Existing Implementation

**Goal**: Update existing implementation to match refined specification requirements

### Layout & UI Refinements

- [X] T001 [P] Update StatusBar component to display battery percentage ABOVE time in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/StatusBar.kt
- [X] T002 [P] Add date display BELOW time in short format ("Thu, Dec 19") in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/StatusBar.kt
- [X] T003 [P] Update DeviceStatus model to include currentDate field in app/src/main/java/com/symbianx/minimalistlauncher/domain/model/DeviceStatus.kt
- [X] T004 [P] Update DeviceStatusRepository to provide date updates in app/src/main/java/com/symbianx/minimalistlauncher/data/repository/DeviceStatusRepositoryImpl.kt
- [X] T005 [P] Increase search result item touch target height to 64dp minimum in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/SearchView.kt
- [X] T006 [P] Position status information (battery, time, date) at top of screen in HomeScreen layout in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeScreen.kt

**Independent Test (Refinements)**: 
- Open launcher → Verify battery is above time, date is below time in format "Thu, Dec 19"
- Swipe to search → Verify search result items are significantly larger (64dp minimum height)
- Verify status bar is positioned at very top of screen

---

## Phase 2: User Story 5 - Favorite Apps Quick Access (Priority: P2)

**Goal**: Implement favorite apps feature allowing users to pin up to 5 apps on home screen

**Why this story**: Enhances usability by providing instant access to most-used apps while maintaining minimalism through strict 5-app limit. Core functionality works without it but significantly improves daily experience.

**Independent Test**: Long-press app in search results → App added to favorites → Return to home screen → Verify app appears in favorites list → Tap to launch → Long-press to remove

### Domain Layer - Favorites Model & Logic

- [X] T007 [US5] Create FavoriteApp data class in app/src/main/java/com/symbianx/minimalistlauncher/domain/model/FavoriteApp.kt
- [X] T008 [US5] Create FavoritesRepository interface in app/src/main/java/com/symbianx/minimalistlauncher/domain/repository/FavoritesRepository.kt
- [X] T009 [US5] Create ManageFavoritesUseCase interface in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/ManageFavoritesUseCase.kt
- [X] T010 [US5] Implement ManageFavoritesUseCase in app/src/main/java/com/symbianx/minimalistlauncher/domain/usecase/ManageFavoritesUseCaseImpl.kt

### Data Layer - Persistence

- [X] T011 [US5] Create FavoritesDataSource interface in app/src/main/java/com/symbianx/minimalistlauncher/data/local/FavoritesDataSource.kt
- [X] T012 [US5] Implement FavoritesDataSource using SharedPreferences in app/src/main/java/com/symbianx/minimalistlauncher/data/local/FavoritesDataSourceImpl.kt
- [X] T013 [US5] Implement FavoritesRepository in app/src/main/java/com/symbianx/minimalistlauncher/data/repository/FavoritesRepositoryImpl.kt

### UI Layer - Favorites Display & Interaction

- [X] T014 [US5] Create FavoritesList composable in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/FavoritesList.kt
- [X] T015 [US5] Add long-press gesture detection to SearchView results in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/SearchView.kt
- [X] T016 [US5] Update HomeViewModel to manage favorites state in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeViewModel.kt
- [X] T017 [US5] Integrate FavoritesList into HomeScreen layout in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeScreen.kt

### Testing

- [ ] T018 [US5] Create FavoritesTest integration test in app/src/androidTest/java/com/symbianx/minimalistlauncher/FavoritesTest.kt

**Independent Test Criteria (User Story 5)**:
1. Long-press "Chrome" in search results → Returns to home screen → Chrome appears in favorites list
2. Tap "Chrome" in favorites on home screen → Chrome launches immediately
3. Long-press "Chrome" in favorites on home screen → Chrome removed from favorites
4. Add 5 favorites → Attempt to add 6th → Error message "Maximum 5 favorites allowed" or oldest replaced
5. Restart app → Favorites still displayed
6. Uninstall favorite app → Favorite automatically removed from list

---

## Phase 3: Polish & Cross-Cutting Concerns

**Goal**: Final touches, documentation, and quality assurance

### Documentation

- [ ] T019 [P] Add KDoc documentation to all public APIs in domain layer (models, repositories, use cases)
- [ ] T020 [P] Add KDoc documentation to FavoriteApp model and FavoritesRepository
- [ ] T021 [P] Update README.md with feature list including favorites functionality

### Code Quality

- [ ] T022 Run ktlint on entire codebase and fix violations: ./gradlew ktlintCheck ktlintFormat
- [ ] T023 Verify 80%+ code coverage for business logic: ./gradlew jacocoTestReport
- [ ] T024 Performance profiling: Verify 120 FPS rendering and <100ms search results using Android Studio Profiler

### Final Validation

- [ ] T025 Test all 5 user stories end-to-end on Pixel 8 Pro device
- [ ] T026 Test graceful degradation on emulator (Now Playing hidden/placeholder)
- [ ] T027 Verify TalkBack accessibility for all interactive elements
- [ ] T028 Verify APK size <5MB: ./gradlew assembleRelease && ls -lh app/build/outputs/apk/release/
- [ ] T029 Test portrait-only orientation lock (attempt landscape, verify stays portrait)

---

## Dependencies & Execution Strategy

### User Story Completion Order

```
Phase 1 (Refinements) → Must complete before Phase 2
    ↓
Phase 2 (User Story 5 - Favorites) → Can start after Phase 1
    ↓
Phase 3 (Polish) → Must complete after all user stories
```

### Task Dependencies Within Phase 2 (User Story 5)

```
T007 (FavoriteApp model)
  ↓
T008 (FavoritesRepository interface) + T009 (ManageFavoritesUseCase interface)
  ↓
T011 (FavoritesDataSource interface)
  ↓
T012 (FavoritesDataSource impl) → T013 (FavoritesRepository impl)
  ↓                                  ↓
T010 (ManageFavoritesUseCase impl)  ↓
  ↓                                  ↓
T014 (FavoritesList UI) + T015 (Long-press in SearchView)
  ↓
T016 (Update HomeViewModel)
  ↓
T017 (Integrate into HomeScreen)
  ↓
T018 (Integration tests)
```

### Parallel Execution Opportunities

**Phase 1 - All refinement tasks can run in parallel (T001-T006)** - Different files, no dependencies

**Phase 2 - Parallel groups**:
- After T007: T008 and T009 can be done in parallel (both are interface definitions)
- After T011: T012 and T010 can be done in parallel (T012 is data layer, T010 is domain layer)
- After T013: T014 and T015 can be done in parallel (different UI components)

**Phase 3 - Most polish tasks can run in parallel (T019-T021, T025-T029)**

---

## Implementation Strategy

### MVP Scope (Already Complete!)

**User Story 1 (P1)** is the MVP and is already fully implemented:
- ✅ Swipe right-to-left activates search
- ✅ Real-time app filtering
- ✅ Tap to launch apps
- ✅ Text-only interface

### Incremental Delivery

1. **Phase 1 (Refinements)** - Quick wins, ~1-2 days
   - Update layout to match refined spec
   - Improve touch targets for better UX

2. **Phase 2 (Favorites)** - Core new feature, ~3-4 days
   - Day 1: Domain layer (T007-T010)
   - Day 2: Data layer (T011-T013)
   - Day 3: UI layer (T014-T017)
   - Day 4: Testing (T018)

3. **Phase 3 (Polish)** - Final touches, ~1-2 days
   - Documentation, linting, performance profiling
   - End-to-end validation

**Total Estimated Time**: 5-8 days

---

## Task Summary

**Total Tasks**: 29

**By Phase**:
- Phase 1 (Refinements): 6 tasks
- Phase 2 (User Story 5 - Favorites): 12 tasks
- Phase 3 (Polish): 11 tasks

**By Story**:
- Refinements: 6 tasks
- User Story 5: 12 tasks
- Cross-cutting: 11 tasks

**Parallel Opportunities**: 15+ tasks can be executed in parallel across different phases

**Already Implemented**:
- User Story 1 (Search): ~20 tasks completed
- User Story 2 (Status): ~15 tasks completed
- User Story 3 (Launcher): ~5 tasks completed
- User Story 4 (Now Playing): ~10 tasks completed

**Remaining Work**: Primarily User Story 5 (Favorites) + refinements

---

## Format Validation

✅ All tasks follow checklist format: `- [ ] [TaskID] [P?] [Story?] Description with file path`
✅ Task IDs sequential (T001-T029)
✅ [P] marker on parallelizable tasks
✅ [US5] marker on User Story 5 tasks
✅ All tasks include specific file paths
✅ Dependencies clearly documented
✅ Independent test criteria provided for each user story
✅ MVP scope identified (already complete)
