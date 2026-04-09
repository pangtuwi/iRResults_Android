# Distribution Instructions

Follow these steps before distributing or publishing a new build of the iRaceResults app.

### 1. Revert to the Production URL
Before creating a release build, ensure the app points back to the live production server instead of your local emulator loopback address.

- Open `app/src/main/java/com/tudorsoft/iraceresults/data/api/RetrofitClient.kt`
- Change `BASE_URL` back to the production address:
```kotlin
// Change this:
private const val BASE_URL = "http://10.0.2.2:4000/"

// Back to this:
private const val BASE_URL = "http://iraceresults.co.uk/"
```

### 2. Update Version and Build Numbers
Bump the version strings so the Google Play Store (or local devices) register the app as a new update.

- Open `app/build.gradle.kts`
- Locate the `defaultConfig` block.
- Increment the `versionCode` by 1. *(This must be an integer and must always go up).*
- Update the `versionName` to your target release version string.
```kotlin
defaultConfig {
    applicationId = "com.tudorsoft.iraceresults"
    minSdk = 24
    targetSdk = 36
    
    // Increment these:
    versionCode = 5 
    versionName = "1.0.5" 
    ...
}
```

### 3. Run All Tests
Ensure that the code is stable and passes all existing test suites before building the release APK/AAB.

Open your terminal in the root directory of the project and run:

**Run Unit Tests:**
```bash
./gradlew test
```

**Run Connected UI/Instrumentation Tests:**  
*(Requires an Android Emulator to be running, or a physical device connected via ADB)*
```bash
./gradlew connectedAndroidTest
```

### 4. Build the Release
Once the tests pass:
- In Android Studio, go to **Build** > **Generate Signed Bundle / APK...** 
- Follow the wizard to generate a signed `.aab` for the Google Play Console or an `.apk` for direct distribution.
