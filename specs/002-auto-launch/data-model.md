# Data Model: Auto-Launch on Single Search Result

## Entities

### SearchQuery
- query: String
- timestamp: Long

### AppResult
- appName: String
- packageName: String
- icon: Drawable

### AutoLaunchState
- isEligible: Boolean
- debounceTimer: Long
- feedbackGiven: Boolean

### UserSettings
- autoLaunchEnabled: Boolean (default: true)

## Relationships
- SearchQuery produces a list of AppResult
- If AppResult.size == 1 and autoLaunchEnabled, AutoLaunchState triggers feedback and launch

## Validation Rules
- Only auto-launch if AppResult.size == 1 and user has paused input for debounce period
- Cancel auto-launch if input changes before debounce expires
- Feedback must be given before launch

## State Transitions
- Typing → Debounce → (if single result) → Feedback → Launch
- Typing → Debounce → (if input changes) → Cancel

---

This model is Compose/Android idiomatic and stateless except for debounce/feedback state.
