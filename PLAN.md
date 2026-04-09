# Hoist Opponent Selection and Auto-Select Class Leader

The user wants the graph logic to automatically pin the Class Leader as an opponent, and allow custom selections to persist across different races/sessions instead of wiping out every time we navigate.

## Proposed Changes

1. **State Hoisting (`MainActivity.kt`)**
   - Create `var perpetuatedOpponents by remember { mutableStateOf<Set<Int>?>(null) }` in `MainActivity` to act as our global memory. Null implies that the initial default state needs to execute.
   - When we route to `AppDestinations.ANALYSIS` and open `LapTimeGraphScreen`, pass down `standings`, `perpetuatedOpponents`, and a new configuration hook `onOpponentsChange = { perpetuatedOpponents = it }`.

2. **LapTimeGraphScreen Architecture (`LapTimeGraphScreen.kt`)**
   - **Default Calculation:** If `perpetuatedOpponents == null`:
       1. Identify the user's `displayName` from the race results.
       2. Use `standings.find { it.driverName == userDriver.displayName }` to find which class the user is racing in.
       3. Find the driver in that class where `it.position == 1`.
       4. Translate that leader's name to their `custId` inside `allSessionDrivers`.
       5. Dispatch `onOpponentsChange` setting the default opponents specifically to that leader (if they are in the current session)!
   - **Persistent Selection Filter:** If `perpetuatedOpponents != null`:
       1. Intersect the stored IDs with the IDs who are actually currently driving in `allSessionDrivers`.
       2. If someone dropped out of the session, they are removed. We dispatch `onOpponentsChange` strictly with the surviving drivers!

3. **Cleanup**
   - We will rip out the internal `var selectedOpponents by remember` within `LapTimeGraphScreen` and wire everything cleanly to the injected `perpetuatedOpponents` state.

## Edge Cases Dealt With
- If the user is the class leader, the chart will gracefully only map the user!
- If the class leader from the standings DNFd or isn't grouped in this specific subsession (like splitting heats), it will naturally just omit them instead of crashing.
