# UI Tidy Up - League Selector

The user wants to only show the League Selector at the top of the Tables page (`AppDestinations.TABLES`), and remove it from all other main pages (Rounds, Analysis, etc.).

## Proposed Changes

1. **`MainActivity.kt`**
   - Locate the `LeagueSelectorBand` inside the main `Scaffold`'s `Column` content block.
   - Update the visibility condition. Currently it is:
     `if (availableLeagues.isNotEmpty() && selectedLeague != null)`
   - Change it to also check if we are on the Tables page and no drawer route is active:
     `if (availableLeagues.isNotEmpty() && selectedLeague != null && currentDestination == AppDestinations.TABLES && currentDrawerRoute.isEmpty())`
