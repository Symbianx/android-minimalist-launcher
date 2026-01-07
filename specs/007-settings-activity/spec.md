# Feature Specification: Launcher Settings Activity

**Feature Branch**: `007-settings-activity`  
**Created**: January 7, 2026  
**Status**: Draft  
**Input**: User description: "Create a new settings activity to control the behaviour of the launcher. Namely it should support all settings that have been added in todos (find it in the repo) and should support a new setting to change the behaviour of the quick action buttons on the bottom left and right corners."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Access Settings from Home Screen (Priority: P1)

As a user, I need a clear way to access the launcher settings so I can customize the launcher behavior to match my preferences.

**Why this priority**: Without a way to access settings, users cannot configure any of the launcher features, making this the foundation for all configuration capabilities.

**Independent Test**: Can be fully tested by opening the launcher and finding the settings entry point, then verifying that the settings screen opens successfully.

**Acceptance Scenarios**:

1. **Given** I am on the home screen, **When** I perform a long-press on the background, **Then** a menu appears with a "Settings" option
2. **Given** I see the settings menu option, **When** I tap "Settings", **Then** the settings activity opens and displays all available settings

---

### User Story 2 - Configure Auto-Launch Behavior (Priority: P2)

As a user, I want to enable or disable the auto-launch feature so I can control whether single search results automatically open or require manual selection.

**Why this priority**: Auto-launch is already implemented but lacks user control, frustrating users who prefer different behaviors.

**Independent Test**: Can be fully tested by toggling the auto-launch setting and performing searches with single results to verify the behavior changes.

**Acceptance Scenarios**:

1. **Given** I am in the settings screen, **When** I see the "Auto-launch apps" setting, **Then** I can toggle it on or off with a clear indication of the current state
2. **Given** auto-launch is enabled, **When** I search for an app with a unique name, **Then** the app launches automatically after a brief delay
3. **Given** auto-launch is disabled, **When** I search for an app with a unique name, **Then** the app appears in results but does not launch automatically

---

### User Story 3 - Customize Quick Action Buttons (Priority: P2)

As a user, I want to customize which apps appear on the bottom left and right quick action buttons so I can access my most frequently used apps instantly.

**Why this priority**: Different users have different priorities - some may want WhatsApp instead of camera, or Messages instead of phone, making customization essential for personalization.

**Independent Test**: Can be fully tested by changing the quick action button assignments in settings and verifying the home screen updates to show the selected apps.

**Acceptance Scenarios**:

1. **Given** I am in the settings screen, **When** I tap "Left quick action button", **Then** I see a list of installed apps to choose from
2. **Given** I select an app for the left button, **When** I return to the home screen, **Then** the left quick action button launches the selected app
3. **Given** I am in the settings screen, **When** I tap "Right quick action button", **Then** I see a list of installed apps to choose from
4. **Given** I select an app for the right button, **When** I return to the home screen, **Then** the right quick action button launches the selected app
5. **Given** I have customized quick action buttons, **When** I tap each button on the home screen, **Then** my selected apps open immediately

---

### User Story 4 - Configure Battery Indicator Visibility (Priority: P3)

As a user, I want to control when the battery indicator appears so I can reduce visual clutter or keep it always visible based on my preference.

**Why this priority**: Enhances personalization and addresses different user needs - some want minimal distractions, others want constant battery awareness.

**Independent Test**: Can be fully tested by changing the battery indicator setting and observing its visibility under different battery conditions.

**Acceptance Scenarios**:

1. **Given** I am in the settings screen, **When** I see the battery indicator options, **Then** I can choose between "Always show", "Show when below 50%", "Show when below 20%", or "Never show"
2. **Given** I select "Always show", **When** I return to the home screen, **Then** the battery indicator is visible regardless of battery level
3. **Given** I select "Show when below 50%", **When** my battery is above 50%, **Then** the indicator is hidden
4. **Given** I select "Never show", **When** I return to the home screen, **Then** the battery indicator never appears

---

### User Story 5 - Reset Settings to Defaults (Priority: P3)

As a user, I want to reset all settings to their default values so I can easily recover from unwanted customizations or start fresh.

**Why this priority**: Provides a safety net for users who want to undo their changes without reinstalling the app.

**Independent Test**: Can be fully tested by customizing several settings, triggering the reset action, and verifying all settings return to defaults.

**Acceptance Scenarios**:

1. **Given** I am in the settings screen, **When** I tap "Reset to defaults", **Then** I see a confirmation dialog explaining that all settings will be reset
2. **Given** I confirm the reset action, **When** the action completes, **Then** all settings return to their default values and the home screen reflects these defaults

---

### Edge Cases

- What happens when a user selects an app for quick actions that is later uninstalled? (System should revert to default app or show a placeholder)
- How does the system handle apps that cannot be launched (disabled or system-restricted)? (Show error message and prevent selection)
- What happens if the user tries to assign the same app to both quick action buttons? (Allow it - some users may want the same app accessible from both sides)
- How does the settings activity behave when opened from a different launcher? (Should handle gracefully or not be accessible)
- What happens if settings data becomes corrupted? (System should fall back to defaults without crashing)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a settings entry point accessible via long-press on the home screen background
- **FR-002**: Settings activity MUST display all configurable launcher behaviors in a clear, organized layout
- **FR-003**: System MUST persist all setting changes immediately when modified by the user
- **FR-004**: System MUST provide a toggle to enable or disable the auto-launch feature with clear labels
- **FR-005**: System MUST allow users to customize the left quick action button by selecting from all installed apps
- **FR-006**: System MUST allow users to customize the right quick action button by selecting from all installed apps
- **FR-007**: System MUST display default apps (Phone for left, Camera for right) as the initial quick action assignments
- **FR-008**: System MUST provide options to control battery indicator visibility with at least three thresholds: always show, show below 50%, show below 20%, never show
- **FR-009**: System MUST update the home screen immediately when returning from settings to reflect any changes
- **FR-010**: System MUST provide a "Reset to defaults" action that restores all settings to their original values
- **FR-011**: System MUST display a confirmation dialog before executing the reset action to prevent accidental data loss
- **FR-012**: System MUST handle cases where selected quick action apps are uninstalled by reverting to default apps
- **FR-013**: System MUST validate that selected apps can be launched before saving the quick action assignment

### Key Entities

- **SettingsActivity**: The main settings screen that displays all configurable options
- **LauncherSettings**: Represents the collection of all user preferences including auto-launch state, quick action app selections, and battery indicator preferences
- **QuickActionButtonConfig**: Represents the configuration for each quick action button (left and right) including the selected app package name and display information
- **BatteryIndicatorPreference**: Represents the battery indicator visibility configuration with threshold values

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can access settings within 2 seconds from the home screen
- **SC-002**: 100% of setting changes persist across app restarts and device reboots
- **SC-003**: Users can customize quick action buttons and see changes reflected on home screen within 1 second of exiting settings
- **SC-004**: 90% of users successfully customize at least one setting on their first attempt without external guidance
- **SC-005**: The settings activity supports all existing launcher features mentioned in previous specifications
- **SC-006**: Reset to defaults action successfully restores all settings within 2 seconds
- **SC-007**: System handles edge cases (uninstalled apps, disabled apps) without crashes or data corruption
