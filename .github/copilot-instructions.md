# Copilot Instructions

## Code Style

This project uses [ktlint](https://pinterest.github.io/ktlint/) for Kotlin code formatting. All code changes must pass `./gradlew ktlintCheck` before being committed.

### Key ktlint rules to follow

- **Multiline expressions must start on a new line**: When assigning a multiline expression (lambda, `apply` block, constructor call, etc.) to a variable, the expression body must start on the next line after the `=` sign.

  ```kotlin
  // ✅ Correct
  val intent =
      Intent(Intent.ACTION_MAIN).apply {
          setClassName("com.example", "com.example.Activity")
      }

  // ❌ Wrong
  val intent = Intent(Intent.ACTION_MAIN).apply {
      setClassName("com.example", "com.example.Activity")
  }
  ```

- **Function expression bodies**: Same rule applies to single-expression functions with multiline bodies.

  ```kotlin
  // ✅ Correct
  fun hasAction(action: String) =
      filters.any { filter ->
          filter.getAction() == action
      }

  // ❌ Wrong
  fun hasAction(action: String) = filters.any { filter ->
      filter.getAction() == action
  }
  ```

### Running ktlint

- **Check**: `./gradlew ktlintCheck`
- **Auto-format**: `./gradlew ktlintFormat`

Always run `./gradlew ktlintCheck` after making code changes to ensure they pass linting before committing.
