# Agent Instructions

To ensure efficient and effective coding tasks, the agent should utilize the Context7 MCP server.

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
