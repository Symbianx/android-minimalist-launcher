# Agent Instructions

To ensure efficient and effective coding tasks, the agent should utilize the Context7 MCP server.

## Debugging and Testing the App

### Launching the App for Testing

**Only do this workflow if the user explicitly tells you to.**

When debugging or testing UI changes, use the following workflow:

**1. Build and Install:**
```bash
# Build the debug APK
./gradlew assembleDebug

# Install on connected device/emulator
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**2. Launch the Main Activity:**
```bash
# Start the launcher home screen
~/Library/Android/sdk/platform-tools/adb shell am start -n com.symbianx.minimalistlauncher.debug/com.symbianx.minimalistlauncher.MainActivity
```

**3. Navigate to Settings (for testing Settings UI):**
- Use Mobile MCP to long-press on an empty area of the home screen
- Or manually long-press on the device/emulator
- Settings Activity will open automatically

**4. Using Mobile MCP for UI Testing:**
```bash
# Take screenshots to see current UI state
mobile-mcp-mobile_take_screenshot

# List interactive elements
mobile-mcp-mobile_list_elements_on_screen

# Interact with UI
mobile-mcp-mobile_click_on_screen_at_coordinates
mobile-mcp-mobile_long_press_on_screen_at_coordinates
```

**5. Check Logs:**
```bash
# View recent errors
~/Library/Android/sdk/platform-tools/adb logcat -d | grep -i "exception\|error\|crash" | tail -20

# Follow live logs for specific package
~/Library/Android/sdk/platform-tools/adb logcat | grep com.symbianx.minimalistlauncher
```

**Note:** The Settings Activity is not exported, so it cannot be launched directly via adb. Always navigate through the main activity.

## Compose Clickable/CombinedClickable Safety Note

Recent versions of Jetpack Compose require explicit handling of `clickable` and `combinedClickable` indications. Using the default ripple (`PlatformRipple`) without an `IndicationNodeFactory` can cause a runtime crash:

IllegalArgumentException: clickable only supports IndicationNodeFactory instances provided to LocalIndication, but Indication was provided instead.

To avoid this, ALWAYS use one of the following patterns when adding click or long-press interactions:

- Provide a custom `interactionSource` and disable ripple:

```kotlin
modifier.clickable(
	interactionSource = remember { MutableInteractionSource() },
	indication = null,
	onClick = onClick,
)
```

- Or, for `combinedClickable`, similarly:

```kotlin
modifier.combinedClickable(
	onClick = { /* ... */ },
	onLongClick = { /* ... */ },
	interactionSource = remember { MutableInteractionSource() },
	indication = null,
)
```

- If ripple is desired, use the clickable overload that takes an `indication` and pass `LocalIndication.current` explicitly:

```kotlin
modifier.clickable(
	interactionSource = remember { MutableInteractionSource() },
	indication = LocalIndication.current,
	onClick = onClick,
)
```

This convention must be followed across all UI components to prevent crashes during attach/measure phases in Compose.
