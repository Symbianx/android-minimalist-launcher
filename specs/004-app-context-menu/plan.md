# Implementation Plan: App Context Menu

**Branch**: `004-app-context-menu` | **Date**: 2025-12-21 | **Spec**: [specs/004-app-context-menu/spec.md](specs/004-app-context-menu/spec.md)
**Input**: Feature specification from `/specs/004-app-context-menu/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Implement a long-press context menu on app items in search results that provides quick access to favoriting apps and opening Android app info settings. The menu will display "Add to Favorites" / "Remove from Favorites" (based on current state) and "Go to App Info" options. Technical approach uses Jetpack Compose's gesture detection APIs for long-press handling, Material3 ModalBottomSheet for the menu UI, and Android system intents for app info navigation.

## Technical Context

**Language/Version**: Kotlin (latest stable), Gradle, Android SDK (target API 34, min API 29)  
**Primary Dependencies**: Jetpack Compose, Material3, AndroidX (ViewModel, Navigation), Compose Foundation (gesture APIs)  
**Storage**: Existing FavoritesRepository with local DataStore for favorites persistence  
**Testing**: JUnit (unit tests), Espresso + Compose UI Test (instrumented tests)  
**Target Platform**: Android 10+ (API 29+)
**Project Type**: Mobile (single Android app, launcher)  
**Performance Goals**: Long-press detection within 500ms, context menu appearance under 300ms, 60fps animations  
**Constraints**: Must not interfere with list scrolling, menu must be touch-friendly (48dp minimum targets), haptic feedback required  
**Scale/Scope**: Single feature affecting search results UI, 2 user stories (P1: favorites, P2: app info), minimal new components

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Test-First**: All long-press gesture detection and context menu interactions must be covered by UI tests before implementation is considered complete
- **Simplicity**: Reuse existing favorites management logic (ManageFavoritesUseCase); use Material3 components for menu UI without custom implementations
- **Integration Testing**: End-to-end test required for long-press → menu → favorites workflow
- **Observability**: Log context menu events (open, dismiss, action selected) for debugging

✓ **No constitution violations** - Feature follows existing patterns and reuses established components

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
│   │       ├── ui/home/
│   │       │   ├── HomeViewModel.kt           # Add openAppInfo() function
│   │       │   └── components/
│   │       │       ├── SearchView.kt          # Integrate long-press on AppListItem
│   │       │       ├── AppListItem.kt         # Add long-press detection
│   │       │       └── AppContextMenu.kt      # NEW: Context menu component
│   │       ├── domain/
│   │       │   └── usecase/
│   │       │       └── ManageFavoritesUseCase.kt  # Reuse existing
│   │       └── util/
│   │           └── ContextMenuLogger.kt       # NEW: Event logging
│   ├── androidTest/
│   │   └── java/com/symbianx/minimalistlauncher/
│   │       └── ui/home/
│   │           ├── AppContextMenuTest.kt      # NEW: UI tests for menu
│   │           └── SearchViewTest.kt          # Add long-press tests
│   └── test/
│       └── java/com/symbianx/minimalistlauncher/
│           └── ui/home/
│               └── HomeViewModelTest.kt       # Add openAppInfo() tests
└── build.gradle.kts
```

**Structure Decision**: Mobile single-app structure. New components added to existing ui/home/components/ directory. Reuses established favorites management use case. Minimal new files (AppContextMenu.kt, ContextMenuLogger.kt) following existing patterns.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No violations to track - feature follows established patterns and reuses existing components.
