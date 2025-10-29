# iRaceResults App Development

## Project Overview
Android application to display league results from iraceresults.co.uk

**Package:** `com.tudorsoft.iraceresults`
**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Adaptive Navigation

## Current State

### Completed
- Initial project setup with Android Studio
- Basic navigation structure with NavigationSuiteScaffold
- Three navigation destinations: Home, Favorites, Profile
- Material 3 theming with edge-to-edge display
- Compose UI foundation
- **Main Home Screen UI** with:
  - "iRaceResults" title using custom ADDCN font
  - Information section (league name/ID and driver info with class)
  - Class selection buttons (Gold, Silver, Bronze, Unclassified)
  - Standings table with position, driver name, and points
  - Sample data matching real league data
- **Custom Color Scheme** matching iraceresults.co.uk:
  - Orange primary color (#FF6600)
  - White surface cards on orange background
  - Custom button colors (Gold, Silver, Bronze, Gray)
  - Proper text contrast and hierarchy
- **Navigation Drawer** with hamburger menu:
  - Rounds
  - Penalties
  - My Penalties
  - Licence Points
  - Teams
  - Settings
  - About
  - Drawer header with app branding
  - Smooth slide-in animation

### Project Structure
```
app/src/main/java/com/tudorsoft/iraceresults/
├── MainActivity.kt (main entry point with initialization logic)
├── data/
│   ├── Models.kt (League, Driver, RacingClass, StandingEntry)
│   ├── api/
│   │   ├── ApiModels.kt (API response models)
│   │   ├── IRaceResultsApi.kt (Retrofit API interface)
│   │   └── RetrofitClient.kt (Retrofit singleton)
│   └── preferences/
│       ├── UserPreferences.kt (user data model)
│       └── PreferencesManager.kt (DataStore manager)
├── ui/
│   ├── navigation/
│   │   ├── DrawerMenu.kt (drawer menu items definition)
│   │   └── AppDrawer.kt (navigation drawer composable)
│   ├── screens/
│   │   ├── HomeScreen.kt (main page components)
│   │   └── SetupScreen.kt (first-time setup/onboarding)
│   └── theme/
│       ├── Color.kt (custom color scheme)
│       ├── Theme.kt (Material 3 theme)
│       └── Type.kt (custom ADDCN font)
app/src/main/res/
└── font/
    └── addcn.ttf (custom brand font)
```

### Dependencies
- Jetpack Compose with Material 3
- Adaptive Navigation Suite
- Lifecycle & Activity KTX
- DataStore Preferences (for persistent storage)
- Retrofit 2 (for API calls)
- Gson Converter (for JSON parsing)
- OkHttp Logging Interceptor (for debugging)
- Kotlin Coroutines (for async operations)
- Testing: JUnit, Espresso

## App Initialization Flow

### First Startup Process
1. User opens app for the first time
2. Setup screen is displayed requesting:
   - **League ID** (e.g., NXTGT3S8)
   - **iRacing Customer ID** (e.g., 123456)
3. On submission:
   - App calls `GET /:leagueid/drivers` endpoint
   - Searches for user's `cust_id` in drivers list
   - If found:
     - Extracts driver's `display_name` and `class`
     - Saves all data to DataStore (persistent storage)
     - Marks setup as complete
     - Navigates to main app
   - If not found:
     - Shows error message
     - Allows user to retry

### Subsequent Startups
- App loads saved preferences from DataStore
- Directly shows main app with user's data
- User info persists across app restarts

### Data Persistence
- Uses Android DataStore for preferences
- Stores: League ID, Customer ID, Display Name, Class, Setup Complete flag
- Can be reset through Settings (future implementation)

## To Do

### Phase 1: Data Layer
- [x] Add internet permission to AndroidManifest.xml
- [x] Set up networking library (Retrofit)
- [x] Define data models for race results and leagues
- [x] Create API service for iraceresults.co.uk
- [x] Implement app initialization flow with DataStore
- [ ] Create repository pattern for data access
- [ ] Fetch actual league name from API

### Phase 2: UI Implementation
- [ ] Create Home screen to display league listings
- [ ] Implement league results detail screen
- [ ] Build Favorites screen for saved leagues
- [ ] Design Profile/Settings screen
- [ ] Add proper navigation between screens

### Phase 3: Features
- [ ] Implement pull-to-refresh functionality
- [ ] Add search/filter capabilities
- [ ] Implement favorites persistence (Room/DataStore)
- [ ] Add loading states and error handling
- [ ] Implement offline caching

### Phase 4: Polish
- [ ] Add animations and transitions
- [ ] Implement proper theming (light/dark mode)
- [ ] Add unit and UI tests
- [ ] Performance optimization
- [ ] Accessibility improvements

## Notes
- Target SDK: 36
- Min SDK: 24 (Android 7.0+)
- Using adaptive navigation for tablet/phone optimization
- Edge-to-edge display enabled

## API Information

### Base URL
iraceresults.co.uk (running on configured port)

### Key Endpoints for Android App

#### Global Endpoints
- `GET /leaguelist` - Get all available leagues
- `GET /cache` - Get entire cached league data

#### League-Specific Endpoints (/:leagueid/)
- `GET /:leagueid/leaguename` - Get league name and ID
- `GET /:leagueid/classtotals` - Class totals/standings
- `GET /:leagueid/teamstotals` - Team totals
- `GET /:leagueid/fullresults` - Complete results data
- `GET /:leagueid/drivers` - All drivers in the league
- `GET /:leagueid/rounds` - Rounds information
- `GET /:leagueid/completedrounds` - Completed rounds info
- `GET /:leagueid/classes` - Classes configuration
- `GET /:leagueid/penalties` - Penalties data (penaltiesjson)
- `GET /:leagueid/licencepoints` - Licence points data
- `GET /:leagueid/displayconfig` - Display configuration

#### POST Endpoints
- `POST /:leagueid/results` - Get filtered results (round_no, cust_id)
- `POST /:leagueid/map` - Get track map image (round_name)
- `POST /:leagueid/irresults` - Get iRacing session results (round_no, session_no)

#### Images
- `GET /:leagueid/img/header.png` - League header image
- `GET /:leagueid/img/footer.png` - League footer image
- Track maps via POST /map endpoint

### Notes
- All league IDs are case-insensitive (converted to uppercase)
- Data is cached on the server
- Authentication routes available at `/auth`
- Admin routes at `/admin`

## Questions/Decisions Needed
- ~~Data source: Will we scrape iraceresults.co.uk or is there an API?~~ **RESOLVED: Full REST API available**
- What's the base URL/port for the API server?
- Update frequency: Real-time, periodic refresh, or manual?
- Offline support: How much data to cache locally?
- User accounts: Do users need to authenticate?
- Which features to prioritize: Results viewing, driver stats, team standings?

## Resources
- iraceresults.co.uk - source website
- API_DOCUMENTATION.md - complete API reference
