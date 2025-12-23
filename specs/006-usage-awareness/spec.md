# Feature Specification: Usage Awareness

**Feature Branch**: `006-usage-awareness`  
**Created**: 2025-12-22  
**Status**: Draft  
**Input**: User description: "The launcher should keep track of how many times the phone was unlocked in the day (and display this in a very minimal way in the launcher). Should keep track of the last unlock (and also display this, very minimally). Should keep track of how many times an app is launched in the day (and display this once the app is opened, very minimally). Should keep track of the last time each app is launched (and display this once the app is openned). Ultimately, the goal is make the user AWARE of their usage given them data to make changes IF THEY WANT TO. We should never shame him."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Daily Unlock Awareness (Priority: P1)

As a user, when I return to the home screen throughout the day, I can see a minimal display showing how many times I've unlocked my phone today and when the last unlock occurred. This helps me become aware of my phone-checking habits without feeling judged.

**Why this priority**: This is the foundation of usage awareness—phone unlocks are the gateway to all other usage. Making this visible creates the first moment of consciousness without requiring any additional user action.

**Independent Test**: Can be fully tested by unlocking the phone multiple times and verifying the unlock count increments and last unlock time updates on the home screen. Delivers immediate value by making unconscious unlocking behavior visible.

**Acceptance Scenarios**:

1. **Given** the phone has been locked, **When** I unlock the phone and view the home screen, **Then** I see the unlock count increment and the "last unlock" time display as "now" or "just now"
2. **Given** I've unlocked my phone 5 times today, **When** I view the home screen, **Then** I see "5 unlocks today" displayed minimally (small text, neutral color, non-intrusive position)
3. **Given** it's midnight (day boundary), **When** I unlock my phone, **Then** the unlock count resets to 1 for the new day
4. **Given** my last unlock was 2 hours ago, **When** I view the home screen, **Then** I see "last unlock: 2h ago" displayed near the unlock count

---

### User Story 2 - App Launch Frequency Awareness (Priority: P2)

As a user, when I open an app from the launcher, I briefly see how many times I've opened that app today before the app fully launches. This creates a small pause for reflection without blocking or shaming me.

**Why this priority**: App-specific awareness is the next level—it helps users notice patterns like "I've opened Instagram 20 times today" without preventing them from doing so. It's a gentle nudge toward conscious app usage.

**Independent Test**: Can be fully tested by launching the same app multiple times from the launcher and verifying the launch count displays correctly each time. Works independently of unlock tracking.

**Acceptance Scenarios**:

1. **Given** I haven't opened Instagram today, **When** I tap Instagram in the app search, **Then** I see "1st time today" displayed briefly (0.5-1 second) before Instagram opens
2. **Given** I've opened Instagram 7 times today, **When** I tap Instagram again, **Then** I see "8th time today" displayed briefly before the app opens
3. **Given** I've opened different apps, **When** I tap each one, **Then** each app shows its own independent launch count
4. **Given** it's midnight (day boundary), **When** I launch any app, **Then** the launch count for that app resets to 1

---

### User Story 3 - Last App Launch Time Awareness (Priority: P3)

As a user, when I open an app from the launcher, I briefly see when I last opened this app (e.g., "last opened 20m ago"). This helps me notice patterns like checking the same app repeatedly within short time windows.

**Why this priority**: Time-based awareness complements frequency tracking—it's especially useful for highlighting compulsive checking patterns (e.g., opening Twitter every 5 minutes). Lower priority because frequency is more impactful initially.

**Independent Test**: Can be fully tested by launching an app, waiting varying amounts of time, and launching it again to verify the "last opened" timestamp updates correctly. Works independently of other tracking.

**Acceptance Scenarios**:

1. **Given** I haven't opened Gmail today, **When** I tap Gmail for the first time, **Then** I see "first time today" (no previous timestamp)
2. **Given** I opened Gmail 15 minutes ago, **When** I tap Gmail again, **Then** I see "last opened 15m ago" displayed briefly before Gmail opens
3. **Given** I opened Gmail 3 hours ago, **When** I tap Gmail again, **Then** I see "last opened 3h ago" displayed briefly
4. **Given** I opened Gmail yesterday, **When** I tap Gmail today, **Then** I see "first time today" (yesterday's data doesn't carry over to "last opened")

---

### Edge Cases

- **What happens when the phone restarts mid-day?** The unlock count and app launch data should persist across reboots (stored locally, not lost)
- **What happens at exactly midnight during active use?** The day boundary should trigger cleanly—counts reset to 0 for the new day, and "last unlock/opened" times are relative to the new day
- **What happens if app launch tracking fails or permissions are missing?** The app still launches normally—tracking failure should never block app access. Show no data rather than error messages.
- **What happens if the user launches an app from outside the launcher (e.g., from another app)?** Those launches are not tracked—we only track conscious launches from our launcher interface
- **What happens when clock changes (timezone travel, DST)?** Use device's current date/time for day boundaries. "Last opened" times should adapt to the new timezone gracefully
- **What happens if storage is full or data corruption occurs?** Gracefully degrade—show no usage data rather than crashing. Clear corrupted data and start fresh if needed

## Requirements *(mandatory)*

### Functional Requirements

#### Unlock Tracking (P1)

- **FR-001**: System MUST detect when the phone is unlocked and increment a daily counter stored locally on the device
- **FR-002**: System MUST record the timestamp of each unlock event
- **FR-003**: System MUST display the current day's unlock count on the home screen in a minimal, non-intrusive way (small text, neutral color, unobtrusive position)
- **FR-004**: System MUST display the time elapsed since the last unlock on the home screen (e.g., "2h ago", "just now")
- **FR-005**: System MUST reset the unlock count to zero at midnight (based on device's local time/timezone)
- **FR-006**: System MUST persist unlock data across device reboots without data loss

#### App Launch Tracking (P2)

- **FR-007**: System MUST track the number of times each app is launched per day from the launcher interface
- **FR-008**: System MUST display the app's daily launch count when the user taps to open an app (brief display before app opens, 0.5-1 second)
- **FR-009**: System MUST track each app's launch count independently (Instagram's count doesn't affect Gmail's count, etc.)
- **FR-010**: System MUST reset all app launch counts to zero at midnight (based on device's local time/timezone)
- **FR-011**: System MUST only track app launches initiated from this launcher (not launches from other apps or system)

#### Last Launch Time Tracking (P3)

- **FR-012**: System MUST record the timestamp of the most recent launch for each app
- **FR-013**: System MUST display time elapsed since last launch when user taps to open an app (e.g., "last opened 20m ago")
- **FR-014**: System MUST display "first time today" when an app hasn't been launched yet today
- **FR-015**: System MUST clear the "last launched" timestamp for all apps at midnight so the next launch shows "first time today"

#### Data Persistence & Reliability

- **FR-016**: System MUST store all tracking data locally on the device (no cloud sync, no external transmission)
- **FR-017**: System MUST handle storage failures gracefully—if data cannot be written, the app launch must still proceed normally
- **FR-018**: System MUST handle corrupted data gracefully—clear invalid data and start fresh rather than crashing
- **FR-019**: System MUST adapt to timezone changes and daylight saving time without losing or duplicating counts

#### Display & UX Principles

- **FR-020**: All usage statistics MUST be displayed in a factual, non-judgmental tone (e.g., "12 unlocks today" not "You've unlocked 12 times already!")
- **FR-021**: Usage data displays MUST never block user actions—app launches proceed regardless of tracking state
- **FR-022**: Usage data displays MUST use neutral colors and small text to minimize visual prominence (avoid red/warning colors)
- **FR-023**: Brief app-launch displays (FR-008, FR-013) MUST disappear automatically without user action required
- **FR-024**: System MUST NOT require invasive permissions (no overlay drawing, no accessibility services, no screen content reading)

### Key Entities

- **UnlockEvent**: Represents a single phone unlock, including timestamp and date for daily aggregation
- **DailyUnlockSummary**: Aggregates unlock data for a single day, including total count and most recent unlock timestamp
- **AppLaunchEvent**: Represents a single app launch from the launcher, including app identifier, timestamp, and date
- **AppLaunchSummary**: Aggregates launch data per app per day, including launch count and most recent launch timestamp

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can see their unlock count on the home screen within 100ms of the screen becoming visible
- **SC-002**: App launch tracking displays appear and disappear smoothly without delaying app launch by more than 50ms
- **SC-003**: Usage data persists correctly across device reboots—unlock count and app launch counts from before reboot are retained and visible after reboot
- **SC-004**: Day boundary transitions (midnight) reset counts correctly—at 12:00:00 AM, all counters reset to 0 for the new day
- **SC-005**: Tracking failures never prevent app launches—100% of app taps result in the app opening, even if tracking fails
- **SC-006**: Usage displays use minimal screen space—unlock count occupies no more than 5% of home screen area
- **SC-007**: Time displays are human-readable and update appropriately ("just now" for <1 minute, "5m ago", "2h ago", "first time today")
- **SC-008**: System works entirely within standard launcher permissions—no requests for overlay, accessibility, or usage stats permissions

### Assumptions

- Device has sufficient storage for tracking data (minimal—a few KB per day)
- Device clock/timezone is reasonably accurate (for day boundary calculations)
- User grants standard launcher permissions (home screen replacement)
- "Day" is defined as midnight-to-midnight in the device's current timezone
- Brief app-launch displays (0.5-1 second) are sufficient for user awareness without being intrusive
- Factual, numeric displays (e.g., "8 unlocks today") are sufficient to create awareness without additional explanations or coaching
- Users understand relative time formats ("2h ago", "just now") as these are common in modern UIs
