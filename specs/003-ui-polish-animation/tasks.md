---
description: "Task list for UI Polish – Animation Refinements & Micro-Interactions"
---

# Tasks: UI Polish – Animation Refinements & Micro-Interactions

**Input**: Design documents from `/specs/003-ui-polish-animation/`
**Prerequisites**: plan.md (required), spec.md (required for user stories)

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create/verify Android project structure for feature in app/src/
- [X] T002 [P] Ensure Jetpack Compose and animation dependencies are up to date in app/build.gradle.kts
- [X] T003 [P] Configure linting and formatting (ktlint, detekt) in project root

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

- [X] T004 [P] Add navigation/gesture utility for swipe detection in app/src/main/java/com/symbianx/minimalistlauncher/util/
- [X] T005 [P] Add animation utility for Compose transitions in app/src/main/java/com/symbianx/minimalistlauncher/util/
- [X] T006 [P] Add logging for navigation and quick access events in app/src/main/java/com/symbianx/minimalistlauncher/util/

---

## Phase 3: User Story 1 – Swipe Back to Home (P1)

**Goal**: Swipe from left to right in search returns to home with animation
**Independent Test Criteria**: Can be tested by swiping in search and observing animated return to home

- [X] T007 [US1] Integrate swipe-back gesture in search UI in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/SearchView.kt
- [X] T008 [US1] Implement animated transition from search to home in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeScreen.kt
- [X] T009 [P] [US1] UI test: swipe-back returns to home with animation in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/home/SearchViewTest.kt

---

## Phase 4: User Story 2 – Clock Quick Access (P2)

**Goal**: Tap time/date area to open system clock/alarm app
**Independent Test Criteria**: Can be tested by tapping time/date and verifying clock app opens

- [X] T010 [US2] Make time/date area tappable in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/StatusBar.kt
- [X] T011 [US2] Launch system clock/alarm intent from home in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/HomeViewModel.kt
- [X] T012 [P] [US2] UI test: tapping time/date opens clock app in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/home/components/StatusBarTest.kt

---

## Phase 5: User Story 3 – Animation & Micro-Interaction Polish (P3)

**Goal**: All transitions and micro-interactions use smooth, modern animations
**Independent Test Criteria**: Can be tested by navigating and observing all transitions

- [X] T013 [US3] Refine all navigation transitions with Compose animation in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/
- [X] T014 [US3] Add micro-interaction feedback (e.g., button press, swipe) in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/
- [X] T015 [P] [US3] UI test: verify animation polish and feedback in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/home/AnimationPolishTest.kt

---

## Final Phase: Polish & Cross-Cutting Concerns

- [X] T016 [P] Refactor and document new logic in app/src/main/java/com/symbianx/minimalistlauncher/
- [X] T017 [P] Update quickstart and README with feature usage
- [X] T018 [P] Manual QA: verify feature on device and emulator

---

## Dependencies

- Phase 1 and 2 must be complete before any user story phases
- US1 (P1) → US2 (P2) → US3 (P3) (can be delivered incrementally)

## Parallel Execution Examples

- T002, T003, T004, T005, T006 can be done in parallel
- T009, T012, T015, T016, T017, T018 can be run in parallel after core logic is in place

## Implementation Strategy

- MVP: Complete all US1 (P1) tasks for swipe-back gesture and animation
- Incrementally deliver US2 (P2) and US3 (P3) for quick access and polish

---

All tasks follow the strict checklist format and are independently testable per user story.
