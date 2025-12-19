# Quickstart Guide: Minimalist Android Launcher

**Feature**: Minimalist Android Launcher  
**Branch**: 001-minimalist-launcher  
**Date**: 2025-12-19

## Prerequisites

### Required Software

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17 or later
- **Android SDK**: API 26 (Android 8.0) minimum, API 34 (Android 14) target
- **Kotlin**: 1.9+ (bundled with Android Studio)
- **Gradle**: 8.0+ (use wrapper)

### Recommended Tools

- **Physical Pixel Device**: For 120Hz and Now Playing testing (Pixel 8 Pro recommended)
- **LeakCanary**: Debug builds only (auto-configured)
- **Android Emulator**: API 34 with Play Store for testing

### System Requirements

- **macOS**: 10.14+ with Xcode Command Line Tools
- **RAM**: 16GB minimum (Android Studio + emulator)
- **Disk**: 10GB free space

---

## Initial Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd android-minimalist-launcher
git checkout 001-minimalist-launcher
```

### 2. Open in Android Studio

1. Launch Android Studio
2. File → Open → Select `android-minimalist-launcher` directory
3. Wait for Gradle sync to complete (5-10 minutes first time)
4. Accept SDK licenses if prompted

### 3. Verify Configuration

Check `local.properties` (auto-generated):
```properties
sdk.dir=/Users/<your-username>/Library/Android/sdk
```

### 4. Sync Project

Build → Make Project (⌘F9) to verify setup

---

## Project Structure

```
android-minimalist-launcher/
├── app/
│   ├── build.gradle.kts          # App module build config
│   └── src/
│       ├── main/                  # Production code
│       ├── test/                  # Unit tests
│       └── androidTest/           # Integration tests
├── build.gradle.kts               # Root build config
├── settings.gradle.kts            # Project settings
├── gradle.properties              # Gradle properties
└── specs/                         # Documentation
```

---

## Build Configuration

### build.gradle.kts (Root)

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
```

### build.gradle.kts (App)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.symbianx.minimalistlauncher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.symbianx.minimalistlauncher"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Android Testing
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
```

### gradle.properties

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=false
kotlin.code.style=official
android.nonTransitiveRClass=true
```

---

## Running the Application

### On Physical Device (Recommended)

1. Enable Developer Options on Pixel device:
   - Settings → About Phone → Tap "Build number" 7 times
   - Settings → System → Developer Options → Enable "USB Debugging"

2. Connect device via USB

3. Run in Android Studio:
   - Click green "Run" button or press ⌃R
   - Select connected Pixel device
   - Wait for build and installation (~2 minutes first time)

4. Set as default launcher:
   - Press home button
   - Select "Minimalist Launcher"
   - Tap "Always"

### On Emulator

1. Create emulator (if not exists):
   - Tools → Device Manager → Create Device
   - Select "Pixel 8 Pro"
   - Download System Image: API 34 with Play Store
   - Finish setup

2. Start emulator:
   - Click green "Run" button
   - Select emulator from device list

3. **Note**: Now Playing feature will not work in emulator (Pixel-specific)

---

## Development Workflow

### 1. Test-Driven Development (TDD)

Per constitution, write tests FIRST:

```bash
# Create test file
touch app/src/test/java/com/symbianx/minimalistlauncher/domain/usecase/SearchAppsUseCaseTest.kt

# Write failing test
# Run tests: ./gradlew test
# Implement feature
# Run tests again: ./gradlew test (should pass)
```

### 2. Run Tests

```bash
# Unit tests
./gradlew test

# Integration tests (requires device/emulator)
./gradlew connectedAndroidTest

# Specific test class
./gradlew test --tests SearchAppsUseCaseTest

# With coverage report
./gradlew testDebugUnitTestCoverage
# Report: app/build/reports/coverage/test/debug/index.html
```

### 3. Linting

```bash
# Run ktlint
./gradlew ktlintCheck

# Auto-fix issues
./gradlew ktlintFormat
```

### 4. Build APK

```bash
# Debug build
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Release build (with R8 shrinking)
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### 5. Performance Profiling

1. Run app on device
2. Android Studio → View → Tool Windows → Profiler
3. Click + → Select running app
4. Monitor:
   - CPU: Frame rendering time (target <8.3ms)
   - Memory: Heap usage (target <30MB)
   - Energy: Battery drain (should be minimal)

### 6. Memory Leak Detection

LeakCanary automatically enabled in debug builds:
- If leak detected, notification will appear
- View leak trace in Android Studio logcat
- Fix leak and rebuild

---

## Testing on Pixel Device

### Enable Now Playing

1. Settings → Sound & vibration → Now Playing
2. Toggle "Identify songs playing nearby"
3. Play music near device to test detection

### Test Swipe Gesture

1. Launch minimalist launcher
2. Swipe right-to-left on home screen
3. Search field should appear with keyboard

### Test 120Hz Display

1. Settings → Display → Smooth Display (should be enabled)
2. Launch launcher
3. Use Android Studio Profiler to verify frame times <8.3ms

---

## Common Issues

### Issue: Gradle Sync Failed

**Solution**:
```bash
./gradlew clean
./gradlew --stop
rm -rf ~/.gradle/caches/
# Restart Android Studio
```

### Issue: App Not Appearing as Launcher Option

**Solution**: Verify AndroidManifest.xml has:
```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.HOME" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

### Issue: Now Playing Not Working

**Checklist**:
- Running on Pixel device (not emulator)
- Now Playing enabled in settings
- Google app updated to latest version
- ContentProvider accessible (check logs)

### Issue: 120 FPS Not Achieved

**Debug Steps**:
1. Check device display settings (Smooth Display enabled)
2. Use Compose layout inspector to find slow composables
3. Check for excessive recomposition (use Layout Inspector)
4. Profile with systrace: `python systrace.py gfx view -o trace.html`

### Issue: Memory Leaks Detected

**Solution**:
- Check ViewModel lifecycle (use viewModelScope)
- Unregister BroadcastReceivers in onPause()
- Cancel ContentObserver in onDestroy()
- Review LeakCanary trace for root cause

---

## Configuration Files

### AndroidManifest.xml (Key Sections)

```xml
<manifest>
    <!-- Permissions -->
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"
        tools:ignore="QueryAllPackagesPermission"/>
    
    <!-- Feature declarations -->
    <uses-feature android:name="android.hardware.touchscreen" />
    
    <application
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.MinimalistLauncher">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTask"
            android:clearTaskOnLaunch="true"
            android:stateNotNeeded="true"
            android:windowSoftInputMode="adjustResize">
            
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### proguard-rules.pro

```pro
# Keep model classes (for reflection)
-keep class com.symbianx.minimalistlauncher.domain.model.** { *; }

# Keep Compose @Stable/@Immutable classes
-keep @androidx.compose.runtime.Stable class * { *; }
-keep @androidx.compose.runtime.Immutable class * { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
```

---

## CI/CD Setup (Future)

Placeholder for GitHub Actions workflow:

```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ 001-minimalist-launcher ]
  pull_request:
    branches: [ 001-minimalist-launcher ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests
        run: ./gradlew test
      - name: Run linting
        run: ./gradlew ktlintCheck
      - name: Build debug APK
        run: ./gradlew assembleDebug
```

---

## Next Steps

1. ✅ Clone repository and setup Android Studio
2. ✅ Verify build and run on device
3. → Start implementing with TDD:
   - Write test for App entity
   - Implement App data class
   - Write test for AppRepository
   - Implement AppRepository
   - Continue with other components

4. → Follow tasks.md for detailed task breakdown (generated via `/speckit.tasks`)

---

## Support

- **Documentation**: See `specs/001-minimalist-launcher/` for detailed design docs
- **Issues**: Check constitution.md for quality standards
- **Testing**: Follow TDD workflow (tests first, always)

---

**Last Updated**: 2025-12-19
