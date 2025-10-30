package com.tudorsoft.iraceresults

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.sp
import com.tudorsoft.iraceresults.data.Driver
import com.tudorsoft.iraceresults.data.League
import com.tudorsoft.iraceresults.data.RacingClass
import com.tudorsoft.iraceresults.data.StandingEntry
import com.tudorsoft.iraceresults.data.api.RetrofitClient
import com.tudorsoft.iraceresults.data.preferences.PreferencesManager
import com.tudorsoft.iraceresults.data.preferences.UserPreferences
import com.tudorsoft.iraceresults.ui.navigation.AppDrawer
import com.tudorsoft.iraceresults.ui.navigation.DrawerMenuItem
import com.tudorsoft.iraceresults.ui.screens.HomeScreen
import com.tudorsoft.iraceresults.ui.screens.SetupScreen
import com.tudorsoft.iraceresults.ui.screens.SettingsScreen
import com.tudorsoft.iraceresults.ui.theme.AddcnFontFamily
import com.tudorsoft.iraceresults.ui.theme.IRaceResultsTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IRaceResultsTheme {
                IRaceResultsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun IRaceResultsApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    var userPreferences by remember { mutableStateOf<UserPreferences?>(null) }
    var isLoadingSetup by remember { mutableStateOf(false) }
    var setupErrorMessage by remember { mutableStateOf<String?>(null) }

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    var currentDrawerRoute by rememberSaveable { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Load user preferences on startup
    LaunchedEffect(Unit) {
        val prefs = preferencesManager.userPreferencesFlow.first()

        // Migration: If setup is complete but leagueName or className need updating
        if (prefs.isSetupComplete && prefs.leagueId.isNotEmpty()) {
            val needsLeagueName = prefs.leagueName.isEmpty()
            // Check if driverClass is empty or appears to be a number (class number instead of name)
            // Also check if it's a single digit or short string that could be a class number
            val needsClassName = prefs.driverClass.isEmpty() ||
                                 prefs.driverClass.toIntOrNull() != null ||
                                 prefs.driverClass.length <= 2

            if (needsLeagueName || needsClassName) {
                try {
                    var leagueName = prefs.leagueName
                    var className = prefs.driverClass

                    // Fetch league name if needed
                    if (needsLeagueName) {
                        val leagueNameResponse = RetrofitClient.api.getLeagueName(prefs.leagueId)
                        if (leagueNameResponse.isSuccessful) {
                            leagueName = leagueNameResponse.body()?.leagueName ?: prefs.leagueId
                        }
                    }

                    // Fetch class name if needed
                    if (needsClassName && prefs.custId.isNotEmpty()) {
                        // Re-fetch driver data to get the current class number
                        val driversResponse = RetrofitClient.api.getDrivers(prefs.leagueId)
                        if (driversResponse.isSuccessful) {
                            val drivers = driversResponse.body()
                            val driver = drivers?.find { it.custId.toString() == prefs.custId }
                            val classNumber = driver?.driverClass

                            android.util.Log.d("MainActivity", "Migration - Driver class number: $classNumber")

                            if (classNumber != null) {
                                // Now fetch classes and look up the name
                                val classesResponse = RetrofitClient.api.getClasses(prefs.leagueId)
                                if (classesResponse.isSuccessful) {
                                    val classes = classesResponse.body()
                                    val matchedClass = classes?.find { it.classNumber == classNumber }
                                    className = matchedClass?.className ?: classNumber.toString()

                                    android.util.Log.d("MainActivity", "Migration - Available classes: ${classes?.map { "${it.classNumber}:${it.className}" }}")
                                    android.util.Log.d("MainActivity", "Migration - Matched class name: $className")
                                }
                            }
                        }
                    }

                    // Save updated info
                    preferencesManager.saveUserInfo(
                        leagueId = prefs.leagueId,
                        leagueName = leagueName,
                        custId = prefs.custId,
                        displayName = prefs.displayName,
                        driverClass = className
                    )
                    userPreferences = preferencesManager.userPreferencesFlow.first()
                } catch (e: Exception) {
                    // If migration fails, just use existing preferences
                    userPreferences = prefs
                }
            } else {
                userPreferences = prefs
            }
        } else {
            userPreferences = prefs
        }
    }

    // Handle reset setup
    fun handleResetSetup() {
        scope.launch {
            preferencesManager.clearUserInfo()
            userPreferences = preferencesManager.userPreferencesFlow.first()
            currentDrawerRoute = ""
        }
    }

    // Handle setup completion
    fun handleSetupComplete(leagueId: String, custId: String) {
        isLoadingSetup = true
        setupErrorMessage = null

        scope.launch {
            try {
                // Call API to get drivers list
                val driversResponse = RetrofitClient.api.getDrivers(leagueId)

                if (driversResponse.isSuccessful) {
                    val drivers = driversResponse.body()
                    val driver = drivers?.find { it.custId.toString() == custId }

                    if (driver != null) {
                        // Call API to get league name
                        val leagueNameResponse = RetrofitClient.api.getLeagueName(leagueId)
                        val leagueName = if (leagueNameResponse.isSuccessful) {
                            leagueNameResponse.body()?.leagueName ?: leagueId
                        } else {
                            leagueId // Fallback to league ID if API call fails
                        }

                        // Call API to get classes and find the class name
                        var className = ""
                        val driverClassNumber = driver.driverClass

                        if (driverClassNumber != null) {
                            val classesResponse = RetrofitClient.api.getClasses(leagueId)
                            if (classesResponse.isSuccessful) {
                                val classes = classesResponse.body()
                                // Try to find matching class by classNumber
                                val matchedClass = classes?.find { it.classNumber == driverClassNumber }
                                className = matchedClass?.className ?: driverClassNumber.toString()

                                // Log for debugging
                                android.util.Log.d("MainActivity", "Driver class number: $driverClassNumber")
                                android.util.Log.d("MainActivity", "Available classes: ${classes?.map { "${it.classNumber}:${it.className}" }}")
                                android.util.Log.d("MainActivity", "Matched class name: $className")
                            } else {
                                className = driverClassNumber.toString()
                                android.util.Log.e("MainActivity", "Failed to fetch classes: ${classesResponse.code()}")
                            }
                        } else {
                            android.util.Log.w("MainActivity", "Driver class number is null for ${driver.displayName}")
                        }

                        // Save user info
                        preferencesManager.saveUserInfo(
                            leagueId = leagueId,
                            leagueName = leagueName,
                            custId = custId,
                            displayName = driver.displayName,
                            driverClass = className
                        )

                        // Reload preferences
                        userPreferences = preferencesManager.userPreferencesFlow.first()
                        isLoadingSetup = false
                    } else {
                        setupErrorMessage = "Customer ID not found in this league"
                        isLoadingSetup = false
                    }
                } else {
                    setupErrorMessage = "League not found. Please check the League ID."
                    isLoadingSetup = false
                }
            } catch (e: Exception) {
                setupErrorMessage = "Connection error: ${e.message}"
                isLoadingSetup = false
            }
        }
    }

    // Show setup screen if not completed
    if (userPreferences == null) {
        // Loading state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (!userPreferences!!.isSetupComplete) {
        SetupScreen(
            onSetupComplete = ::handleSetupComplete,
            isLoading = isLoadingSetup,
            errorMessage = setupErrorMessage
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentDrawerRoute,
                onMenuItemClick = { item ->
                    // If Tables is clicked, clear the drawer route to show home page
                    if (item.route == "tables") {
                        currentDrawerRoute = ""
                    } else {
                        currentDrawerRoute = item.route
                    }
                },
                onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = it.label
                            )
                        },
                        label = { Text(it.label) },
                        selected = it == currentDestination,
                        onClick = {
                            currentDestination = it
                            // Reset drawer route when navigating to Home
                            if (it == AppDestinations.HOME) {
                                currentDrawerRoute = ""
                            }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                        TopAppBar(
                        title = {
                            Text(
                                text = "iRaceResults",
                                fontFamily = AddcnFontFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            ) { innerPadding ->
                when (currentDestination) {
                    AppDestinations.HOME -> {
                        if (currentDrawerRoute.isEmpty()) {
                            HomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                league = League(
                                    leagueId = userPreferences!!.leagueId,
                                    leagueName = userPreferences!!.leagueName
                                ),
                                driver = Driver(
                                    displayName = userPreferences!!.displayName,
                                    className = userPreferences!!.driverClass,
                                    custId = userPreferences!!.custId.toIntOrNull() ?: 0
                                ),
                                classes = getSampleClasses(),
                                standings = getSampleStandings()
                            )
                        } else {
                            DrawerContent(
                                route = currentDrawerRoute,
                                modifier = Modifier.padding(innerPadding),
                                onResetSetup = ::handleResetSetup
                            )
                        }
                    }
                    AppDestinations.FAVORITES -> {
                        Greeting(
                            name = "Favorites",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    AppDestinations.PROFILE -> {
                        Greeting(
                            name = "Profile",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Tables", Icons.Default.List),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun DrawerContent(
    route: String,
    modifier: Modifier = Modifier,
    onResetSetup: () -> Unit = {}
) {
    when (route) {
        "settings" -> {
            SettingsScreen(
                modifier = modifier,
                onResetSetup = onResetSetup
            )
        }
        else -> {
            val title = when (route) {
                "tables" -> "Tables"
                "rounds" -> "Rounds"
                "penalties" -> "Penalties"
                "my_penalties" -> "My Penalties"
                "licence_points" -> "Licence Points"
                "teams" -> "Teams"
                "about" -> "About"
                else -> "Unknown"
            }

            // Placeholder content for each drawer screen
            androidx.compose.foundation.layout.Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "Content coming soon...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// Sample data functions for development
fun getSampleLeague() = League(
    leagueId = "NXTGT3S8",
    leagueName = "NXTGen GT3 Season 8"
)

fun getSampleDriver() = Driver(
    displayName = "Paul N T Williams",
    className = "Silver",
    custId = 12345
)

fun getSampleClasses() = listOf(
    RacingClass("Gold", "GOLD"),
    RacingClass("Silver", "SILVER"),
    RacingClass("Bronze", "BRONZE"),
    RacingClass("Unclassified", "UNCLASSIFIED")
)

fun getSampleStandings() = listOf(
    StandingEntry(1, "Benjamin Mccluskey", 228, "SILVER"),
    StandingEntry(2, "Harry Langford", 188, "SILVER"),
    StandingEntry(3, "Stephan Wessels", 177, "SILVER"),
    StandingEntry(4, "Dominik Pester", 164, "SILVER"),
    StandingEntry(5, "Jack Pittas", 163, "SILVER"),
    StandingEntry(6, "William Guthrie", 145, "SILVER"),
    StandingEntry(7, "Gareth Williams7", 137, "SILVER"),
    StandingEntry(8, "Brandon Schmidt3", 109, "SILVER"),
    StandingEntry(9, "Adam Wilson-Weir", 85, "SILVER"),
    StandingEntry(10, "Gavan Gardener", 84, "SILVER"),
    StandingEntry(11, "Kyle Griffiths", 76, "SILVER"),
    StandingEntry(12, "Lukasz Sidor", 66, "SILVER"),
    StandingEntry(13, "James Buchanan", 57, "SILVER"),
    StandingEntry(1, "Michael Johnson", 286, "GOLD"),
    StandingEntry(2, "Sarah Williams", 265, "GOLD"),
    StandingEntry(3, "David Brown", 248, "GOLD"),
    StandingEntry(1, "Robert Taylor", 198, "BRONZE"),
    StandingEntry(2, "Jennifer Moore", 187, "BRONZE"),
    StandingEntry(3, "Thomas Anderson", 175, "BRONZE"),
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IRaceResultsTheme {
        Greeting("Android")
    }
}