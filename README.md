# iRaceResults Android

An Android application for viewing iRacing league results from [iraceresults.co.uk](https://iraceresults.co.uk).

## Features

### Current Features
- **First-Time Setup Flow**: Seamless onboarding to collect League ID and iRacing Customer ID
- **User Data Persistence**: Automatic storage and retrieval of user preferences using DataStore
- **League Standings Display**: View driver standings filtered by racing class (Gold, Silver, Bronze, Unclassified)
- **Navigation Drawer**: Quick access to key features including Rounds, Penalties, Teams, and Settings
- **Custom Branding**: Orange color scheme and custom ADDCN font matching iraceresults.co.uk
- **Material 3 Design**: Modern UI with adaptive navigation for phones and tablets

### In Development
- Fetch and display actual league data from API
- Real-time standings updates
- Detailed race results view
- Penalty tracking
- Teams standings
- License points tracking

## Screenshots

<!-- Add screenshots here when available -->

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material 3
- **Architecture**: MVVM (planned)
- **Networking**: Retrofit 2 + OkHttp
- **Data Persistence**: DataStore Preferences
- **Async Operations**: Kotlin Coroutines
- **Navigation**: Adaptive Navigation Suite

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11 or higher
- Android SDK with minimum API level 24 (Android 7.0)
- Target SDK 36

### Installation

1. Clone the repository:
```bash
git clone https://github.com/pangtuwi/iRResults_Android.git
cd iRResults_Android
```

2. Open the project in Android Studio

3. Sync Gradle dependencies

4. Run the app on an emulator or physical device

### First Run

On first launch, you'll be prompted to enter:
- **League ID**: Found in the iraceresults.co.uk URL (e.g., `NXTGT3S8`)
- **iRacing Customer ID**: Your iRacing account customer number

The app will validate your credentials against the league's driver list and automatically configure your profile.

## Project Structure

```
app/src/main/java/com/tudorsoft/iraceresults/
├── MainActivity.kt                    # Main entry point with initialization logic
├── data/
│   ├── Models.kt                      # Data models
│   ├── api/
│   │   ├── ApiModels.kt              # API response models
│   │   ├── IRaceResultsApi.kt        # Retrofit API interface
│   │   └── RetrofitClient.kt         # Retrofit singleton
│   └── preferences/
│       ├── UserPreferences.kt        # User data model
│       └── PreferencesManager.kt     # DataStore manager
├── ui/
│   ├── navigation/
│   │   ├── DrawerMenu.kt             # Navigation drawer items
│   │   └── AppDrawer.kt              # Drawer UI component
│   ├── screens/
│   │   ├── HomeScreen.kt             # Main standings screen
│   │   └── SetupScreen.kt            # First-time setup screen
│   └── theme/
│       ├── Color.kt                  # Custom color scheme
│       ├── Theme.kt                  # Material 3 theme
│       └── Type.kt                   # Custom typography
```

## API Integration

The app integrates with the iraceresults.co.uk API to fetch:
- Driver lists for league validation
- League standings and results
- Race round information
- Penalty data
- Team standings

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete API reference.

## Configuration

### Network Security
The app uses HTTP to connect to iraceresults.co.uk. Network security configuration is defined in:
```
app/src/main/res/xml/network_security_config.xml
```

### Custom Font
The app uses the ADDCN font for branding consistency with iraceresults.co.uk:
```
app/src/main/res/font/addcn.ttf
```

## Development

### Building
```bash
./gradlew assembleDebug
```

### Running Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### Code Style
This project follows [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

## Roadmap

See [CLAUDE.md](CLAUDE.md) for detailed development progress and roadmap.

### Planned Features
- [ ] Real-time league data synchronization
- [ ] Pull-to-refresh for latest standings
- [ ] Offline caching with Room database
- [ ] Push notifications for race results
- [ ] Driver profile pages
- [ ] Race calendar view
- [ ] Lap time analysis
- [ ] Dark mode support
- [ ] Multi-league support

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Data provided by [iraceresults.co.uk](https://iraceresults.co.uk)
- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Icons from [Material Design Icons](https://material.io/resources/icons/)

## Contact

For questions or support, please open an issue on GitHub.

---

**Note**: This app is not officially affiliated with iRacing or iraceresults.co.uk. It is a third-party application built for personal use and the community.
