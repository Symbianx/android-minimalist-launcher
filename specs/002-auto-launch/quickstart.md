# Quickstart: Auto-Launch on Single Search Result

## Prerequisites
- Android Studio (latest stable)
- Kotlin, Jetpack Compose, AndroidX
- Clone repo and checkout `002-auto-launch` branch

## How to Run
1. Open project in Android Studio
2. Build and run on emulator or device (Android 10+)
3. Use search: type an app name
   - If only one result, app should auto-launch after ~300ms pause
   - If multiple results, no auto-launch
   - If input changes, auto-launch cancels
   - Brief haptic/visual feedback before launch

## How to Test
- Run unit and UI tests: `./gradlew test connectedAndroidTest`
- Manual: Try searching for unique and non-unique app names
- Observe logs for auto-launch events

## Feature Toggle
- Auto-launch is enabled by default
- (Future) Settings menu will allow toggling this feature

---

For more details, see [spec.md](spec.md) and [plan.md](plan.md).
