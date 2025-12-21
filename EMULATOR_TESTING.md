# Emulator Testing Guide

## Quick Start

### 1. Create Android Emulator (Android Studio)

**For ARM MacBooks (M1/M2/M3):**
```bash
# Use ARM system image for native performance (Android 15 - API 36)
avdmanager create avd -n MinimalistLauncherTest \
  -k "system-images;android-36.1;google_apis;arm64-v8a" \
  -d "pixel_8_pro"
```

**For Intel Macs or Windows:**
```bash
# Use x86_64 system image (Android 15 - API 36)
avdmanager create avd -n MinimalistLauncherTest \
  -k "system-images;android-36.1;google_apis;x86_64" \
  -d "pixel_8_pro"
```

**Recommended Settings:**
- **Device:** Pixel 8 Pro (or any Pixel device)
- **System Image:** Android 15 (API 36) with Google APIs
- **Architecture:** ARM64 (arm64-v8a) for ARM Macs, x86_64 for Intel
- **RAM:** 4GB minimum (8GB recommended)
- **Storage:** 2GB minimum

### 2. Launch Emulator
```bash
# From command line:
emulator -avd MinimalistLauncherTest

# Or via Android Studio: Tools → Device Manager → Play button
```

### 3. Install & Test
```bash
# Install the app from your project directory
./gradlew installDebug

# Or manually:
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. Set as Default Launcher
1. Press **Home button** in emulator
2. Select **"Minimalist Launcher"**
3. Tap **"Always"**

---

## Testing Checklist

### ✅ User Story 1: App Search
- [ ] Swipe right-to-left → search appears
- [ ] Type "Chrome" → results filter in real-time
- [ ] Tap result → app launches
- [ ] Search "xyz" (nonexistent) → "No apps found"

### ✅ User Story 2: Status Info
- [ ] Time displays correctly (matches system)
- [ ] Battery percentage shows
- [ ] Charging indicator (⚡) appears when charging
- [ ] Time updates every minute
- [ ] Battery updates on change

### ✅ User Story 3: Default Launcher
- [ ] Responds to home button press
- [ ] Survives emulator restart

### ✅ User Story 4: Now Playing (Expected Behavior)
- [ ] **Emulator:** Section is hidden/not displayed (graceful degradation) ✅
- [ ] **Non-Pixel device:** Section is hidden/not displayed (graceful degradation) ✅
- [ ] **Pixel device:** Shows song info when music detected

### ✅ Orientation
- [ ] Portrait mode locked (rotation ignored)

---

## Troubleshooting

### Emulator slow/laggy
- **ARM Mac:** Ensure you're using `arm64-v8a` system image (NOT x86_64)
- **Intel Mac/Windows:** Enable Hardware Acceleration (HAXM on Intel, WHPX on AMD)
- Increase RAM to 8GB in AVD settings
- Close other resource-intensive apps

### "System image not found" error
```bash
# Download ARM system image (for ARM Macs) - Android 15:
sdkmanager "system-images;android-36.1;google_apis;arm64-v8a"

# Or via Android Studio: SDK Manager → SDK Platforms → 
# Check "Android 15.0 (API 36)" → Show Package Details → 
# Select "ARM 64 v8a System Image"
```

### Apps not appearing in search
- Grant "Query All Packages" permission
- Install some apps first: Chrome, Gmail, Maps, etc.

### Can't set as default launcher
- Check AndroidManifest.xml has correct intent filters
- Restart emulator

### Now Playing shows error
- **Expected!** This is Pixel-specific and unavailable in emulator
- The section should be hidden (verify in StatusBar.kt)

---

## Command Reference

```bash
# List running emulators
adb devices

# Install debug APK
./gradlew installDebug

# Uninstall
adb uninstall com.symbianx.minimalistlauncher

# View logs
adb logcat | grep MinimalistLauncher

# Take screenshot
adb exec-out screencap -p > screenshot.png

# Simulate battery change (for testing battery display)
adb shell dumpsys battery set level 50
adb shell dumpsys battery set status 2  # 2 = charging

# Reset battery simulation
adb shell dumpsys battery reset
```

---

## Performance Testing in Emulator

```bash
# Start profiling
adb shell am start -W com.symbianx.minimalistlauncher/.MainActivity

# Check cold start time (should be <500ms)
# Output shows: ThisTime: XXX TotalTime: XXX

# Monitor memory
adb shell dumpsys meminfo com.symbianx.minimalistlauncher | grep TOTAL
# Should be <30MB
```

---

## Next Steps

1. ✅ Create emulator
2. ✅ Install launcher
3. ✅ Test all 4 user stories
4. ✅ Verify performance targets
5. Deploy to physical Pixel 8 Pro for Now Playing testing

**Expected:** All features work in emulator except Now Playing (which gracefully hides).
