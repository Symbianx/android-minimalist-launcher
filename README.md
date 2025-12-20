# Android Minimalist Launcher

A distraction-free Android launcher designed to reduce phone usage time through deliberate simplicity. Built entirely using [github/spec-kit](https://github.com/github/spec-kit) to explore spec-driven development with Android/Kotlin.

## Features

### Core Functionality
- **Text-Only Interface**: Clean home screen with no app icons or widgets
- **Status Display**: Time, date, battery percentage at the top of the screen
- **Gesture-Based Search**: Swipe right-to-left to activate app search
- **Instant Search**: Real-time filtering as you type with fuzzy matching
- **Favorite Apps**: Pin up to 5 frequently-used apps for quick access on home screen
- **Long-Press Actions**: Add/remove favorites by long-pressing apps in search or on home screen

### Pixel-Specific Features
- **Now Playing Integration**: Shows currently detected ambient music (Pixel devices only)
- **120Hz Optimization**: Smooth 120 FPS rendering on Pixel 8 Pro

### Design Philosophy
- **Minimalist by Default**: No visual clutter, only essential information
- **Deliberate Access**: Apps accessible through intentional search, not mindless scrolling
- **Portrait-Only**: Focused single-orientation experience
- **Graceful Degradation**: Features like Now Playing hidden on non-Pixel devices

> **Note**: This launcher is part of a personal journey to reduce phone distractions and spend less time mindlessly scrolling. By removing visual clutter and making apps deliberately accessible through search, it encourages more intentional phone usage.

## Development Journey

This project demonstrates spec-driven development using GitHub's [spec-kit](https://github.com/github/spec-kit) framework with:
* **Android Ecosystem**: First Android project in 10 years
* **Kotlin**: Learning a new language from scratch
* **Mobile Stack**: Building mobile expertise from the ground up
* **Modern Architecture**: Clean architecture with MVVM, Jetpack Compose, and Kotlin Flows

### Technical Stack
- **Language**: Kotlin 1.9+ with Android SDK 36
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with repository pattern
- **State Management**: Kotlin Flows and StateFlow
- **Testing**: JUnit 4, Espresso, Compose UI testing
- **Performance**: 120 FPS on 120Hz displays, <100ms search latency

### Project Structure
```
app/src/main/java/com/symbianx/minimalistlauncher/
├── ui/              # Compose UI components and ViewModels
├── domain/          # Business logic (models, repositories, use cases)
└── data/            # Data sources (system APIs, local storage)
```

## Building & Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android SDK with API 26+ (target API 36)
- Physical Pixel device recommended (for Now Playing and 120Hz testing)

### Build Steps
```bash
# Clone the repository
git clone <repository-url>
cd android-minimalist-launcher

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Setting as Default Launcher
1. Open device Settings
2. Navigate to Apps → Default apps → Home app
3. Select "Minimalist Launcher"
4. Press the Home button to activate

## Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Integration Tests
```bash
./gradlew connectedAndroidTest
```

### Automated Emulator Tests

The project includes comprehensive instrumentation tests that can run on any emulator:

**Test Suites**:
- `FavoritesTest` - Tests favorites feature (add, remove, persistence)
- `NowPlayingGracefulDegradationTest` - Tests app works without Now Playing
- `AccessibilityTest` - Tests TalkBack compatibility and touch targets
- `OrientationLockTest` - Tests portrait-only lock
- `EndToEndUserStoryTest` - Tests core user stories end-to-end

**Run on emulator**:
```bash
# Start an emulator first (any API 26+ device)
# Then run:
./gradlew connectedAndroidTest

# Or run specific test:
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.symbianx.minimalistlauncher.AccessibilityTest
```

**Note**: Now Playing feature tests verify graceful degradation on non-Pixel devices.

### Code Coverage
```bash
./gradlew jacocoTestReport
# Report available at: app/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Linting
```bash
./gradlew ktlintCheck ktlintFormat
```


## Future Improvements

### Planned Features
- **Auto-Launch**: Automatically open app when search returns single result
- **Dynamic Backgrounds**: Automatic background generation (exploring generative art)
- **UI Polish**: Animation refinements and micro-interactions
- **Accessibility**: Enhanced TalkBack support and high-contrast themes
- **Settings**: Customizable search behavior and display preferences

### Known Limitations
- Portrait orientation only
- Now Playing feature requires Pixel device with feature enabled
- Maximum 5 favorite apps (by design)
- No app widgets support (intentional minimalism)

## Contributing

This is a personal learning project, but feedback and suggestions are welcome! Feel free to:
- Open issues for bugs or feature requests
- Share your experience using spec-kit
- Suggest improvements to the architecture or implementation

## License

See [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with [github/spec-kit](https://github.com/github/spec-kit) - Specification-driven development framework
- Inspired by minimalist launcher projects and digital wellbeing initiatives
- Special thanks to the Android and Kotlin communities for excellent documentation
