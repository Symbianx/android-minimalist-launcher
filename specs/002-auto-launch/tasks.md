---
description: "Task list for Auto-Launch on Single Search Result"
---

# Tasks: Auto-Launch on Single Search Result

**Input**: Design documents from `/specs/002-auto-launch/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create/verify Android project structure for feature in app/src/
- [X] T002 [P] Ensure Jetpack Compose and dependencies are up to date in app/build.gradle.kts
- [X] T003 [P] Configure linting and formatting (ktlint, detekt) in project root

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

- [X] T004 [P] Add AutoLaunchState and UserSettings to app/src/main/java/[...]/model/
- [X] T005 [P] Add debounce/timer utility for Compose in app/src/main/java/[...]/util/
- [X] T006 [P] Add haptic/visual feedback utility in app/src/main/java/[...]/util/
- [X] T007 [P] Add logging for auto-launch events in app/src/main/java/[...]/util/

---

## Phase 3: User Story 1 - Instant App Launch (P1)

**Goal**: Instantly launch app when search returns a single result
**Independent Test Criteria**: Can be tested by searching for a unique app and observing instant launch

- [X] T008 [US1] Integrate debounce logic into search UI in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/SearchView.kt
- [X] T009 [US1] Detect single-result state and trigger auto-launch in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/SearchView.kt
- [X] T010 [US1] Launch app intent from search UI in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/SearchView.kt
- [X] T011 [P] [US1] Unit test: debounce and single-result logic in app/src/test/java/com/symbianx/minimalistlauncher/domain/usecase/AutoLaunchDeciderTest.kt
- [X] T012 [P] [US1] UI test: search triggers auto-launch in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/home/SearchViewTest.kt

---

## Phase 4: User Story 2 - Prevent Accidental Launch (P2)
- [X] T013 [US2] Cancel/reset auto-launch if input changes in app/src/main/java/[...]/ui/search/
- [X] T014 [US2] Ensure debounce timer resets on every input in app/src/main/java/[...]/ui/search/
- [X] T015 [P] [US2] UI test: rapid typing does not trigger auto-launch in app/src/androidTest/java/com/symbianx/minimalistlauncher/ui/home/SearchViewTest.kt

---

## Phase 5: User Story 3 - Accessibility and Feedback (P3)
- [X] T016 [US3] Integrate feedback utility to provide cue before launching app in app/src/main/java/com/symbianx/minimalistlauncher/ui/home/components/SearchView.kt
- [X] T017 [P] [US3] UI test: feedback cue is shown before auto-launch in app/src/androidTest/java/[...]//

---

## Final Phase: Polish & Cross-Cutting Concerns
- [X] T018 [P] Refactor and document new logic in app/src/main/java/[...]/
- [X] T019 [P] Update quickstart and README with feature usage
- [X] T020 [P] Manual QA: verify feature on device and emulator

---

## Dependencies

- Phase 1 and 2 must be complete before any user story phases
- US1 (P1) → US2 (P2) → US3 (P3) (can be delivered incrementally)

## Parallel Execution Examples

- T002, T003, T004, T005, T006, T007 can be done in parallel
- T011, T012, T015, T017, T018, T019, T020 can be run in parallel after core logic is in place

## Implementation Strategy

- MVP: Complete all US1 (P1) tasks for a working auto-launch on single result
- Incrementally deliver US2 (P2) and US3 (P3) for robustness and polish

---

All tasks follow the strict checklist format and are independently testable per user story.
