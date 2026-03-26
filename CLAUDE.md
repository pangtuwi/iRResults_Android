# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
iRaceResults is an Android app for tracking iRacing league standings, penalties, and driver performance. Built with Jetpack Compose and Material 3, integrating with the iraceresults.co.uk API.

**Current Version:** 1.0.4 (Beta)
**Package:** `com.tudorsoft.iraceresults`
**Min SDK:** 24 (Android 7.0+) | **Target SDK:** 36
**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Retrofit 2, DataStore

---

## Build & Test Commands

### Build
```bash
# Debug build
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Release build (requires signing config)
./gradlew assembleRelease

# Android App Bundle for Play Store
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

### Testing
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests com.tudorsoft.iraceresults.ExampleUnitTest
```

### Gradle
```bash
# Sync dependencies
./gradlew build --refresh-dependencies

# Clean build
./gradlew clean build
```

---

## Architecture Overview

### Navigation Architecture (Critical)
The app uses a **dual navigation system** that required careful coordination:

1. **Bottom Navigation (5 tabs)**: Tables, Rounds, Teams, Penalties, Licence
2. **Drawer Menu (8 items)**: All bottom nav items + My Penalties, Settings, About

**Key Implementation Detail:**
- Navigation state is managed in `MainActivity.kt` (not in ViewModels yet)
- Drawer routes are checked BEFORE bottom nav routing to prevent conflicts
- Drawer-only items (My Penalties, Settings, About) overlay the current tab
- Bottom nav clicks clear drawer routes to prevent stale navigation state
- Round selection state must be passed to drawer to maintain context

**Critical Navigation Logic in MainActivity:**
```kotlin
// Check drawer routes first
if (drawerRoute != null) {
    when (drawerRoute) {
        "my_penalties" -> PenaltiesScreen(...)
        "settings" -> SettingsScreen(...)
        "about" -> AboutScreen()
        // Tab items switch the bottom nav
        "tables" -> { currentRoute = "tables"; drawerRoute = null }
        // etc...
    }
} else {
    // Then handle bottom nav routes
    when (currentRoute) { ... }
}
```

### Data Flow Pattern
```
MainActivity (state holder)
    ↓ launches coroutines
RetrofitClient.api → API call
    ↓ returns Response<T>
ApiModels (deserialization)
    ↓ maps to
Domain Models (Models.kt)
    ↓ updates
remember { mutableStateOf } in MainActivity
    ↓ triggers recomposition of
Screen Composables
```

**Current Limitation**: No ViewModels or Repository pattern yet. All state and API calls are in MainActivity (~1100+ lines). Migration to MVVM is planned.

### API Integration
**Base URL**: `https://iraceresults.co.uk/api/`
**Client**: Retrofit 2 with Gson converter (singleton in `RetrofitClient.kt`)
**Auth**: None required (public API)

**Key Endpoints**:
- `GET /:leagueid/classtotals` - Championship standings (nested arrays)
- `GET /:leagueid/fullresults` - Round event results (Map<String, Any> for flexibility)
- `GET /:leagueid/drivers` - Driver list with class info
- `GET /:leagueid/penalties` - Penalty information
- `GET /:leagueid/licencepoints` - Licence points (nested arrays)

**Special Parsing Cases**:
- `fullresults` endpoint returns `Map<String, Any>` because result structure varies
- DQ (disqualified) drivers have `position = -1` → display as "DQ" text
- DQ drivers must be sorted to bottom of results manually
- Nested arrays require `mapNotNull` to handle nulls safely

### State Management
**Pattern**: Compose `remember { mutableStateOf }` with coroutines
**Persistence**: DataStore Preferences for user settings (League ID, Customer ID, driver name)
**No offline caching yet**: Room database planned but not implemented

**Critical State Variables in MainActivity**:
- `userPreferences` - Loaded from DataStore on startup
- `standings`, `teamStandings`, `rounds`, `penalties`, `licencePoints` - API data
- `isRefreshing*` - Pull-to-refresh states per screen
- `currentRoute`, `drawerRoute` - Navigation state
- `selectedRound` - Currently selected round for details view

### Project Structure
```
app/src/main/java/com/tudorsoft/iraceresults/
├── MainActivity.kt                 # ALL navigation and state management (1100+ lines)
├── data/
│   ├── Models.kt                  # Domain models (League, Driver, StandingEntry, etc.)
│   ├── api/
│   │   ├── ApiModels.kt          # API response DTOs with @SerializedName
│   │   ├── IRaceResultsApi.kt    # Retrofit interface definitions
│   │   └── RetrofitClient.kt     # Singleton API client with logging
│   └── preferences/
│       ├── UserPreferences.kt    # Data class for user settings
│       └── PreferencesManager.kt # DataStore wrapper
└── ui/
    ├── screens/                   # 9 composable screens (each 100-300 lines)
    │   ├── SetupScreen.kt        # Onboarding flow
    │   ├── HomeScreen.kt         # Tables/Standings
    │   ├── RoundsScreen.kt       # Round list
    │   ├── RoundDetailsScreen.kt # Event results with expandable cards
    │   └── ...
    ├── navigation/
    │   ├── DrawerMenu.kt         # Drawer item definitions
    │   └── AppDrawer.kt          # Drawer UI composable
    └── theme/
        ├── Theme.kt              # Material 3 theme
        ├── Color.kt              # Custom color scheme (orange branding)
        └── Type.kt               # ADDCN custom font
```

### Compose Patterns Used
1. **Pull-to-Refresh**: All data screens use `pullRefresh` modifier with state
2. **Expandable Cards**: `AnimatedVisibility` with `expandVertically`/`fadeIn` transitions
3. **Lazy Lists**: `LazyColumn` for scrollable content with `items()` builder
4. **Material 3**: `NavigationSuiteScaffold`, `ModalNavigationDrawer`, `Card`, `ElevatedCard`
5. **State**: `rememberSaveable` for state that survives configuration changes

### Error Handling Pattern
```kotlin
try {
    val response = RetrofitClient.api.someEndpoint()
    if (response.isSuccessful) {
        response.body()?.let { data ->
            // Map API models to domain models
        }
    } else {
        Log.e("TAG", "Error: ${response.code()}")
    }
} catch (e: Exception) {
    Log.e("TAG", "Exception: ${e.message}")
}
```

### Logging Convention
- Use `android.util.Log.d()` for debug logs
- Tag format: "ScreenName" or "APICall"
- Log all API responses, especially complex JSON structures
- Log navigation state changes for debugging

---

## Known Issues & Technical Debt

### Recent Changes (1.0.4)
- **Multi-league support** (2026-03-26): Added ability to track multiple iRacing leagues with automatic migration of existing single-league data to new multi-league storage format.
- **UI layout reorganization** (2026-03-26): Moved league selector and driver info to improve layout and usability.
- **AGP updated**: Android Gradle Plugin bumped to 8.13.2.

### Recent Bug Fixes
- **Penalties not displaying** (2025-01-22): Fixed NullPointerException caused by null values in penalty API responses. Multiple fields (`roundName`, `driverName`, `stewardsDecision`, `scoreEvent`) can be null. Made all relevant fields nullable in both `Penalty` and `PenaltyResponse` models. Updated UI screens to handle nulls with fallback text: "Unknown Round", "Unknown Driver", "Unknown Event", "No decision recorded".

### Architecture (High Priority)
1. **No ViewModels**: All state in MainActivity (~1100 lines) - needs MVVM refactor
2. **No Repository Pattern**: Direct API calls from MainActivity
3. **No Dependency Injection**: Manual singleton management, Hilt needed
4. **No Offline Support**: No Room database, no data caching
5. **No Error Recovery**: No retry mechanisms for failed API calls

### Testing (High Priority)
- **Test Coverage**: 0% - no unit tests, no UI tests
- **Test Infrastructure**: Not set up (mocking, fixtures, CI/CD)

### UI/UX Improvements (Medium Priority)
- Search/filter functionality needed across screens
- No data visualization (charts, graphs)
- No notifications system
- Accessibility improvements needed (TalkBack, high contrast)

### Code Organization (Medium Priority)
- Extract common UI components (tables, cards) into reusable composables
- Split screen files (some 300+ lines)
- Better resource organization

---

## Development Notes

### When Adding New Screens
1. Create composable in `ui/screens/YourScreen.kt`
2. Add navigation logic to `MainActivity.kt` (drawer and/or bottom nav)
3. If drawer-only: add to `drawerRoute` handling
4. If bottom nav: add to both `currentRoute` and drawer tab switching
5. Pass required state variables as parameters from MainActivity
6. Use pull-to-refresh pattern if loading data
7. Handle loading, error, and empty states

### When Adding New API Endpoints
1. Add interface method to `IRaceResultsApi.kt`
2. Create response DTO in `ApiModels.kt` with `@SerializedName`
3. Create domain model in `Models.kt`
4. Add state variable in MainActivity
5. Create coroutine to fetch data
6. Map API model → domain model
7. Update UI to display new data

### Custom Resources
- **Font**: `app/src/main/res/font/addcn.ttf` (ADDCN branding font)
- **Icons**: Vector drawables in `res/drawable/`
  - `ic_launcher_background.xml` - Checkered flag pattern
  - `ic_launcher_foreground.xml` - Racing helmet
- **Colors**: Defined in `ui/theme/Color.kt` (orange branding scheme)

### Version Management
Update 3 locations when bumping version:
1. `app/build.gradle.kts` - `versionCode` and `versionName`
2. `AboutScreen.kt` - Version display text
3. This file - Project Overview section

---

## Roadmap

### Immediate Priorities
1. **MVVM Migration**: Extract state from MainActivity into ViewModels
2. **Room Database**: Add offline caching for standings, rounds, penalties
3. **Testing**: Set up test infrastructure (unit tests for API/models, UI tests)
4. **Repository Layer**: Abstract data sources, use Flow for reactive streams

### Feature Wishlist
- Search/filter across drivers, teams, penalties
- Data visualization (points progression charts)
- Push notifications for race results
- Enhanced error handling with retry logic
- Theme selection (Light/Dark/System)
- Accessibility improvements (TalkBack, high contrast)
- Internationalization support

---

## Resources

- **API Documentation**: https://iraceresults.co.uk/api/
- **GitHub Repository**: https://github.com/pangtuwi/iRResults_Android
- **Material 3 Guidelines**: https://m3.material.io
- **Jetpack Compose Docs**: https://developer.android.com/jetpack/compose

---

**Last Updated:** 2026-03-26
