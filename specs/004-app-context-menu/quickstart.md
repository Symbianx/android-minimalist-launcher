# Quickstart: App Context Menu

This feature lets you long‑press any app in the search results to open a context menu with actions:

- Add to Favorites / Remove from Favorites (contextual)
- Go to App Info (opens Android system settings for the app)

## Usage

1. Open Search and type the app name.
2. Long‑press on an app result to open the context menu.
3. Tap one of:
   - Add to Favorites: Pins the app to the home favorites list.
   - Remove from Favorites: Unpins the app from favorites (if already pinned).
   - Go to App Info: Opens the app’s system settings page for permissions, notifications, storage, etc.
4. Tap outside the sheet or press back to dismiss.

## Haptics & Accessibility

- Long‑press triggers haptic feedback.
- Menu items have content descriptions for screen readers.
- The menu container announces: "Context menu for <App Label>".

## Implementation Notes

- UI built with Jetpack Compose Material 3 `ModalBottomSheet`.
- Safe clickable/combinedClickable pattern is used to avoid ripple crashes:
  - Provide `interactionSource = remember { MutableInteractionSource() }` and `indication = null` for click/long‑press.
- Opening App Info uses `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` with a package `Uri`.

## Troubleshooting

- If long‑press does not open the menu, check that the item uses `combinedClickable` with a valid `onLongClick`.
- If runtime crashes mention `IndicationNodeFactory`, audit all `clickable`/`combinedClickable` usages and apply the safe pattern above.
