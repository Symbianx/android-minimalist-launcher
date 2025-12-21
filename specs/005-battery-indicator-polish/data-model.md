# Data Model: Battery Indicator Polish (Spec 005)

## Entity: BatteryIndicator
- percentage: Int (0-100)
- isCharging: Boolean
- animationState: Enum (Idle, Charging)

## Relationships
- None (UI-only, no persistent data)

## Validation Rules
- percentage must be 0-100
- animationState must match isCharging

## State Transitions
- Idle → Charging (when isCharging becomes true)
- Charging → Idle (when isCharging becomes false)
