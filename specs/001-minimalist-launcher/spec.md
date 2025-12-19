# Feature Specification: Minimalist Android Launcher

**Feature Branch**: `001-minimalist-launcher`  
**Created**: 2025-12-19  
**Status**: Draft  
**Input**: User description: "Build an android application that can replace my current launcher. I use a Google Pixel 8 Pro with the default google launcher so I want something minimalistic that prevents me from spending time on my phone. It should allow searching, finding apps (text only) and displaying the time and battery %."

## Clarifications

### Session 2025-12-19

- Q: How should the launcher handle "Now Playing" functionality for emulator testing and non-Pixel devices? → A: Make Now Playing optional/gracefully degrade - show placeholder or hide section when unavailable (works on emulator and non-Pixel)
- Q: How should the launcher behave in landscape orientation? → A: Portrait-only - lock orientation to portrait, ignore landscape completely
- Q: What is the maximum number of favorite apps that can be displayed on the home screen? → A: 5 favorites maximum
- Q: How should favorite apps be presented on the home screen? → A: Vertical list below status information (text labels)
- Q: What specific sizing should be used for search result items to make them bigger? → A: 64dp minimum touch target height
- Q: What date format should be displayed under the time? → A: Short date with abbreviated day and month (e.g., "Thu, Dec 19")
- Q: How should users remove apps from favorites? → A: Long-press on home screen favorite to remove it

### Session 2025-12-19 (Evening)

- **NEW**: Add phone button in bottom left corner to quickly open phone dialer
- **NEW**: Add camera button in bottom right corner to quickly open camera app
- **NEW**: Display battery indicator as circular progress ring around camera notch on Pixel 8 Pro
- **NEW**: Fallback to standard battery percentage display on non-Pixel devices

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Quick App Launch via Search (Priority: P1)

As a user, I want to quickly find and launch any installed app by typing its name after swiping right-to-left, so I can access apps without browsing through menus or icons, reducing time spent on my phone.

**Why this priority**: This is the core functionality of a launcher - the ability to launch apps. Without this, the launcher cannot function. Text-based search aligns with the minimalist goal by removing visual distractions.

**Independent Test**: Swipe right-to-left on home screen to activate search, type an app name (e.g., "Chrome"), and verify the app launches. Delivers immediate value as a functional launcher replacement.

**Acceptance Scenarios**:

1. **Given** I am on the home screen, **When** I swipe right-to-left, **Then** the search field appears and gains focus with keyboard shown
2. **Given** search is active and I start typing "Chro", **When** I continue typing, **Then** Chrome appears in the search results
3. **Given** search results are displayed, **When** I tap on Chrome, **Then** Chrome app launches immediately
4. **Given** I am on the home screen, **When** I type a partial app name like "cal", **Then** Calendar app appears in results
5. **Given** I type an app name that doesn't exist, **When** search completes, **Then** I see "No apps found" message
6. **Given** multiple apps match my search (e.g., "go" matches Google, Google Maps), **When** I view results, **Then** all matching apps are listed in alphabetical order

---

### User Story 2 - Home Screen with Essential Status Info (Priority: P2)

As a user, I want to see the current time, date, battery percentage, and now playing song on my home screen without any additional clutter, so I can glance at essential information while minimizing distractions.

**Why this priority**: Essential context information that users need at a glance. This completes the basic launcher experience but the launcher can function without it (P1 allows app launching).

**Independent Test**: Open the launcher and verify time, date, battery percentage (positioned at top), and now playing info (if music detected) are clearly visible and accurate. No additional UI elements present.

**Acceptance Scenarios**:

1. **Given** I am on the home screen, **When** I look at the top of the display, **Then** I see the battery percentage above the current time
2. **Given** I am on the home screen, **When** I look below the time, **Then** I see the current date in short format (e.g., "Thu, Dec 19")
3. **Given** I am on the home screen and music is detected, **When** I look at the display, **Then** I see the now playing song information
4. **Given** time changes (e.g., from 2:59 to 3:00), **When** I view the home screen, **Then** the displayed time updates automatically
5. **Given** date changes (e.g., from Dec 19 to Dec 20), **When** I view the home screen, **Then** the displayed date updates automatically
6. **Given** battery level changes, **When** I view the home screen, **Then** the battery percentage updates automatically
7. **Given** now playing song changes, **When** I view the home screen, **Then** the song information updates automatically
8. **Given** I press the home button from any app, **When** home screen appears, **Then** time, date, battery, and now playing are displayed with no animation delay
9. **Given** I am on the home screen, **When** I look at the layout, **Then** I see status information (battery, time, date) at the top of the screen

---

### User Story 3 - Set as Default Launcher (Priority: P3)

As a user, I want to set this app as my default launcher and have it activate when I press the home button, so it completely replaces the Google launcher.

**Why this priority**: Required for complete launcher replacement, but users can test P1 and P2 functionality before committing to making it the default launcher.

**Independent Test**: Install the app, set it as default launcher, press home button from another app, and verify the minimalist launcher appears.

**Acceptance Scenarios**:

1. **Given** the app is installed, **When** I press the home button, **Then** Android prompts me to choose a launcher
2. **Given** I am prompted to choose a launcher, **When** I select the minimalist launcher and tap "Always", **Then** it becomes my default launcher
3. **Given** the app is set as default launcher, **When** I press home button from any app, **Then** the minimalist launcher home screen appears
4. **Given** the app is set as default launcher, **When** I restart my phone, **Then** the minimalist launcher remains the default
5. **Given** I want to switch launchers, **When** I go to Android Settings > Apps > Default apps > Home app, **Then** I can change to a different launcher

---

### User Story 4 - Now Playing Display (Priority: P2)

As a user, I want to see the currently playing song detected by Pixel's "Now Playing" feature on my home screen, so I can identify music around me without opening additional apps or unlocking my phone.

**Why this priority**: This enhances the home screen's informational value while maintaining minimalism. It leverages existing Pixel functionality and provides useful context without requiring user interaction. Priority P2 because it's valuable but not essential for core launcher functionality.

**Device Compatibility**: This feature is optional and degrades gracefully on non-Pixel devices and emulators. When "Now Playing" is unavailable, the section is hidden or shows a placeholder. This ensures the launcher works fully in all testing and deployment environments.

**Independent Test**: Play music near the device, wait for "Now Playing" to detect it, and verify the song name and artist appear on the launcher home screen. On emulators or non-Pixel devices, verify the section is gracefully hidden or shows appropriate fallback.

**Acceptance Scenarios**:

1. **Given** "Now Playing" detects a song, **When** I view the home screen, **Then** I see the song name and artist displayed
2. **Given** no music is detected, **When** I view the home screen, **Then** the now playing area is empty or shows "No music detected"
3. **Given** a song is playing and detection changes to a new song, **When** I'm on the home screen, **Then** the displayed song updates automatically
4. **Given** "Now Playing" feature is disabled in Pixel settings, **When** I view the home screen, **Then** the now playing area shows an appropriate message or is hidden
5. **Given** a song is detected, **When** I tap on the now playing display, **Then** the Pixel "Now Playing" history activity opens (same as tapping the widget in Pixel Launcher)
6. **Given** I run the launcher on an emulator or non-Pixel device, **When** I view the home screen, **Then** the now playing section is gracefully hidden and other features work normally

---

### User Story 5 - Favorite Apps Quick Access (Priority: P2)

As a user, I want to mark up to 5 apps as favorites and have them displayed on my home screen, so I can launch my most-used apps instantly without needing to search.

**Why this priority**: Enhances usability by providing quick access to frequently-used apps while maintaining minimalism through a strict 5-app limit. Core launcher functionality (P1 search) works without this, but it significantly improves daily user experience.

**Independent Test**: Long-press an app in search results to add it to favorites, return to home screen, and verify the app appears in the favorites list. Tap the favorite to launch it. Long-press the favorite on home screen to remove it.

**Acceptance Scenarios**:

1. **Given** I am viewing search results, **When** I long-press on an app name, **Then** the app is added to my favorites list
2. **Given** I have added a favorite app, **When** I return to the home screen, **Then** I see the app displayed in the favorites list below the status information
3. **Given** I am on the home screen, **When** I tap a favorite app, **Then** the app launches immediately
4. **Given** I have 5 favorites already, **When** I try to add a 6th favorite, **Then** I see a message "Maximum 5 favorites allowed" or the oldest favorite is replaced
5. **Given** I have a favorite on the home screen, **When** I long-press it, **Then** it is removed from favorites
6. **Given** I have multiple favorites, **When** I view the home screen, **Then** favorites are displayed in a vertical list with text labels only
7. **Given** I uninstall an app that is a favorite, **When** I return to home screen, **Then** that favorite is automatically removed from the list

---

### User Story 6 - Quick Action Buttons (Priority: P2)

As a user, I want quick access buttons for the phone dialer and camera at the bottom of the home screen, so I can instantly open these essential apps without searching.

**Why this priority**: Phone and camera are the most frequently used apps that need instant access. Having dedicated buttons reduces friction for these critical use cases while maintaining the minimalist interface.

**Independent Test**: On the home screen, tap the phone button in the bottom left corner → Phone dialer opens. Tap the camera button in the bottom right corner → Camera app opens.

**Acceptance Scenarios**:

1. **Given** I am on the home screen, **When** I look at the bottom left corner, **Then** I see a phone button
2. **Given** I am on the home screen, **When** I look at the bottom right corner, **Then** I see a camera button
3. **Given** I am on the home screen, **When** I tap the phone button, **Then** the phone dialer app launches immediately
4. **Given** I am on the home screen, **When** I tap the camera button, **Then** the camera app launches immediately
5. **Given** I am in search mode, **When** I look at the bottom corners, **Then** the phone and camera buttons remain visible
6. **Given** the phone dialer is not available on the device, **When** the home screen loads, **Then** the phone button is hidden or disabled
7. **Given** the camera app is not available on the device, **When** the home screen loads, **Then** the camera button is hidden or disabled

---

### User Story 7 - Circular Battery Indicator (Priority: P3)

As a Pixel 8 Pro user, I want the battery indicator to be displayed as a circular progress ring around the camera notch area, so battery status is visible while utilizing the device's unique design.

**Why this priority**: This is a nice-to-have visual enhancement specific to Pixel 8 Pro that leverages the device's hardware design. The launcher will function fully without it (battery percentage text already exists), but it provides a more integrated and visually appealing experience.

**Device Compatibility**: 
- **Pixel 8 Pro**: Display circular battery indicator around the camera notch position at the top center
- **Other devices**: Fallback to standard battery percentage display near the status bar (already implemented)

**Independent Test**: On Pixel 8 Pro, open the launcher and verify a circular progress ring is visible around the camera notch area at the top, with the ring fill percentage matching the battery level. On non-Pixel devices, verify battery percentage is displayed as text.

**Acceptance Scenarios**:

1. **Given** I am using a Pixel 8 Pro device, **When** I view the home screen, **Then** I see a circular battery indicator around the camera notch position
2. **Given** my battery is at 75%, **When** I view the circular indicator, **Then** the ring is 75% filled
3. **Given** my battery level changes from 50% to 49%, **When** the change occurs, **Then** the circular indicator updates to reflect 49%
4. **Given** I am using a non-Pixel device or emulator, **When** I view the home screen, **Then** I see the standard battery percentage text display (no circular indicator)
5. **Given** the device has a notch/cutout in a different position than Pixel 8 Pro, **When** the home screen loads, **Then** the app gracefully falls back to standard battery display
6. **Given** I am using a Pixel 8 Pro and battery is charging, **When** I view the circular indicator, **Then** the ring displays with a charging indication (different color or animation)

---

### Edge Cases

- What happens when no apps are installed (fresh device state)?
- What happens when app names contain special characters or emojis?
- What happens when search is performed with empty/whitespace-only input?
- How does the launcher handle system permission requirements (e.g., accessing app list)?
- What happens when the device language changes - do app names update?
- What happens when battery level reaches critical (5%) or charging status changes?
- How does the launcher behave in landscape orientation?
- What happens when an app is uninstalled while the launcher is active?
- What happens when searching during app installation/uninstallation?
- What happens if user swipes right-to-left when search is already active?
- What happens if user swipes left-to-right or in other directions on home screen?
- What happens when "Now Playing" feature is disabled or unavailable on non-Pixel devices?
- What happens when "Now Playing" data becomes stale or outdated?
- What happens if "Now Playing" requires additional permissions that user denies?
- What happens when a favorite app is uninstalled?
- What happens if user tries to add a 6th favorite when already at the 5-app limit?
- What happens when user long-presses an app that is already a favorite (in search)?
- What happens to favorites list order when apps are added/removed?
- What happens when favorite app data fails to persist or becomes corrupted?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Launcher MUST retrieve and display a complete list of all installed apps (including system apps with launcher intents)
- **FR-002**: Launcher MUST detect right-to-left swipe gesture on home screen to activate search
- **FR-003**: Launcher MUST provide a text input field that appears on swipe gesture
- **FR-004**: Search field MUST gain focus automatically and show keyboard when activated via swipe gesture
- **FR-005**: Search MUST filter apps in real-time as user types (no search button required)
- **FR-006**: Search MUST match app names using case-insensitive partial matching
- **FR-007**: Search results MUST be displayed as a text-only list (no app icons)
- **FR-007a**: Search result items MUST have a minimum touch target height of 64dp for comfortable interaction
- **FR-008**: Users MUST be able to launch any app from search results with a single tap
- **FR-009**: Home screen MUST display current time at the top of the screen in 12-hour or 24-hour format based on device settings
- **FR-009a**: Home screen MUST display current date under the time in short format with abbreviated day and month (e.g., "Thu, Dec 19")
- **FR-010**: Home screen MUST display battery percentage as a numerical value positioned above the time
- **FR-011**: Home screen SHOULD display currently playing song information from Pixel's "Now Playing" feature when available (optional feature that gracefully degrades on emulators and non-Pixel devices)
- **FR-012**: Time display MUST update automatically every minute
- **FR-013**: Battery percentage MUST update automatically when battery level changes
- **FR-014**: Now playing information SHOULD update automatically when song detection changes (on devices that support it)
- **FR-015**: Launcher SHOULD attempt to access "Now Playing" history data from Pixel's ambient music recognition system where available
- **FR-016**: Now playing display SHOULD show song name and artist when available on compatible devices
- **FR-017**: Now playing display MUST handle cases where "Now Playing" is disabled, unavailable, or unsupported gracefully by hiding the section or showing appropriate placeholder
- **FR-017a**: Launcher MUST work fully on emulators and non-Pixel devices with Now Playing feature gracefully degraded or hidden
- **FR-018**: Launcher MUST register as a home app to be selectable as default launcher
- **FR-019**: Launcher MUST respond to home button press when set as default
- **FR-020**: Launcher MUST handle Android system back button appropriately (dismiss search if active and return to home screen view, otherwise no action)
- **FR-021**: Launcher MUST lock orientation to portrait mode only (landscape orientation is not supported)
- **FR-022**: Launcher MUST persist as default launcher across device reboots
- **FR-023**: Launcher MUST request necessary Android permissions (query installed packages, access "Now Playing" data)
- **FR-024**: Tapping on now playing display MUST launch the Pixel "Now Playing" history activity
- **FR-025**: Search field MUST clear and dismiss when an app is launched
- **FR-026**: Search MUST NOT auto-activate on home button press or device unlock
- **FR-027**: Launcher MUST display a message when search returns no results
- **FR-028**: Launcher MUST update app list when apps are installed or uninstalled
- **FR-029**: UI MUST render at 120Hz (8.3ms per frame) on devices with 120Hz displays
- **FR-030**: Users MUST be able to mark apps as favorites via long-press gesture in search results
- **FR-031**: Launcher MUST support a maximum of 5 favorite apps displayed on home screen
- **FR-032**: Favorite apps MUST be displayed as a vertical list of text labels below the status information on home screen
- **FR-033**: Users MUST be able to launch favorite apps with a single tap from home screen
- **FR-034**: Users MUST be able to remove favorites via long-press gesture on the favorite item on home screen
- **FR-035**: Favorites MUST persist across app restarts and device reboots
- **FR-036**: When a favorite app is uninstalled, it MUST be automatically removed from the favorites list
- **FR-037**: When attempting to add a 6th favorite, the launcher MUST show a "Maximum 5 favorites allowed" message or replace the oldest favorite

### Key Entities

- **App**: Represents an installed application with name and launch intent, includes both system and user apps with launcher intents
- **Favorite App**: A user-selected app (maximum 5) displayed on home screen for quick access, persisted across sessions
- **Device Status**: Represents current time, date, and battery level for home screen display
- **Now Playing Info**: Represents currently detected song information (song name, artist) from Pixel's ambient music recognition
- **Search Query**: User's text input used to filter the app list
- **Swipe Gesture**: Right-to-left swipe action that activates search mode

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can launch any app within 3 seconds from swiping right-to-left (including search time)
- **SC-002**: Search results appear within 100ms of each keystroke
- **SC-003**: Home screen displays in under 200ms when home button is pressed
- **SC-004**: Swipe gesture recognition occurs within 50ms of gesture completion
- **SC-005**: Now playing information updates within 2 seconds of song detection
- **SC-006**: Time, date, and battery percentage are accurate within 1% margin and visible without scrolling
- **SC-007**: 95% of user app launches happen through search or favorites (indicating users prefer quick access methods over browsing)
- **SC-008**: App successfully registers as launcher and responds to home button on all Android 8.0+ devices
- **SC-009**: All UI elements are accessible via TalkBack screen reader
- **SC-010**: Users report reduced phone usage time compared to feature-rich launchers (qualitative feedback)
- **SC-011**: Favorite apps launch in under 200ms from home screen tap
- **SC-012**: Users can add/remove favorites with no more than 2 gestures (long-press + confirm if needed)

### Constitution Compliance *(mandatory)*

- **Performance**: Launcher maintains 120 FPS on 120Hz displays (8.3ms per frame), memory footprint under 30MB, cold start under 500ms, total APK size under 5MB
- **Testing**: 100% of user stories have integration tests, 80%+ code coverage for business logic (app filtering, search, swipe gesture recognition, favorites management, now playing data access, launcher registration)
- **UX Consistency**: Follows Material Design 3 for text input and typography, supports dark/light themes with high contrast text, all interactions (including swipe gesture and long-press) provide immediate visual feedback, search result items minimum 64dp touch target height, status information positioned at top of screen, now playing display integrates seamlessly with minimal design
- **Quality**: Zero linting violations (ktlint), all public APIs documented with KDoc, minimal dependencies (Android SDK only, Pixel "Now Playing" API access, no third-party libraries unless justified)
