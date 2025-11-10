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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
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
import com.tudorsoft.iraceresults.data.LicencePointsEntry
import com.tudorsoft.iraceresults.data.Penalty
import com.tudorsoft.iraceresults.data.RacingClass
import com.tudorsoft.iraceresults.data.Round
import com.tudorsoft.iraceresults.data.StandingEntry
import com.tudorsoft.iraceresults.data.TeamStanding
import com.tudorsoft.iraceresults.data.api.RetrofitClient
import com.tudorsoft.iraceresults.data.preferences.PreferencesManager
import com.tudorsoft.iraceresults.data.preferences.UserPreferences
import com.tudorsoft.iraceresults.ui.navigation.AppDrawer
import com.tudorsoft.iraceresults.ui.navigation.DrawerMenuItem
import com.tudorsoft.iraceresults.ui.screens.AboutScreen
import com.tudorsoft.iraceresults.ui.screens.AllPenaltiesScreen
import com.tudorsoft.iraceresults.ui.screens.HomeScreen
import com.tudorsoft.iraceresults.ui.screens.LicencePointsScreen
import com.tudorsoft.iraceresults.ui.screens.PenaltiesScreen
import com.tudorsoft.iraceresults.ui.screens.RoundDetailsScreen
import com.tudorsoft.iraceresults.ui.screens.RoundsScreen
import com.tudorsoft.iraceresults.ui.screens.SetupScreen
import com.tudorsoft.iraceresults.ui.screens.SettingsScreen
import com.tudorsoft.iraceresults.ui.screens.TeamStandingsScreen
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
    var standings by remember { mutableStateOf<List<StandingEntry>>(emptyList()) }
    var classes by remember { mutableStateOf<List<RacingClass>>(emptyList()) }
    var teamStandings by remember { mutableStateOf<List<TeamStanding>>(emptyList()) }
    var rounds by remember { mutableStateOf<List<Round>>(emptyList()) }
    var penalties by remember { mutableStateOf<List<Penalty>>(emptyList()) }
    var allPenalties by remember { mutableStateOf<List<Penalty>>(emptyList()) }
    var licencePoints by remember { mutableStateOf<List<LicencePointsEntry>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isRefreshingTeams by remember { mutableStateOf(false) }
    var isRefreshingRounds by remember { mutableStateOf(false) }
    var isRefreshingPenalties by remember { mutableStateOf(false) }
    var isRefreshingAllPenalties by remember { mutableStateOf(false) }
    var isRefreshingLicencePoints by remember { mutableStateOf(false) }
    var roundEvents by remember { mutableStateOf<List<com.tudorsoft.iraceresults.data.RoundEvent>>(emptyList()) }
    var isRefreshingRoundEvents by remember { mutableStateOf(false) }
    var selectedRound by remember { mutableStateOf<Round?>(null) }

    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.TABLES) }
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

    // Fetch team standings data
    fun fetchTeamStandings(leagueId: String) {
        scope.launch {
            isRefreshingTeams = true
            try {
                val response = RetrofitClient.api.getTeamStandings(leagueId)
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    teamStandings = data.mapNotNull { team ->
                        TeamStanding(
                            position = team.position ?: 0,
                            teamName = team.teamName ?: "",
                            driver1 = team.driver1 ?: "",
                            driver2 = team.driver2 ?: "",
                            driver3 = team.driver3 ?: "",
                            totalPoints = team.total ?: 0
                        )
                    }
                    android.util.Log.d("MainActivity", "Fetched ${teamStandings.size} team standings")
                } else {
                    android.util.Log.e("MainActivity", "Failed to fetch team standings: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error fetching team standings", e)
            } finally {
                isRefreshingTeams = false
            }
        }
    }

    // Fetch rounds data
    fun fetchRounds(leagueId: String) {
        scope.launch {
            isRefreshingRounds = true
            try {
                val response = RetrofitClient.api.getRounds(leagueId)
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    rounds = data.map { roundResponse ->
                        Round(
                            roundNo = roundResponse.roundNo,
                            trackName = roundResponse.trackName,
                            startTime = roundResponse.startTime
                        )
                    }
                    android.util.Log.d("MainActivity", "Fetched ${rounds.size} rounds")
                } else {
                    android.util.Log.e("MainActivity", "Failed to fetch rounds: ${response.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error fetching rounds", e)
            } finally {
                isRefreshingRounds = false
            }
        }
    }

    // Fetch penalties data (for current user only)
    fun fetchPenalties(leagueId: String, custId: String) {
        scope.launch {
            isRefreshingPenalties = true
            try {
                val response = RetrofitClient.api.getPenalties(leagueId)
                android.util.Log.d("MainActivity", "Penalties response code: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    val userCustId = custId.toIntOrNull() ?: 0

                    // Filter penalties for the current user only, handling null values
                    penalties = data
                        .filter { it.custId == userCustId }
                        .map { penaltyResponse ->
                            Penalty(
                                protestId = penaltyResponse.protestId,
                                roundName = penaltyResponse.roundName,
                                roundNo = penaltyResponse.roundNo,
                                scoreEvent = penaltyResponse.scoreEvent ?: "",
                                driverName = penaltyResponse.displayName,
                                stewardsDecision = penaltyResponse.stewardsDecision
                            )
                        }
                    android.util.Log.d("MainActivity", "Fetched ${penalties.size} penalties for user $custId (out of ${data.size} total)")
                } else {
                    android.util.Log.e("MainActivity", "Failed to fetch penalties: ${response.code()}")
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("MainActivity", "Error body: $errorBody")
                }
            } catch (e: com.google.gson.JsonSyntaxException) {
                android.util.Log.e("MainActivity", "JSON parsing error for penalties - API may have returned non-JSON response", e)
                // Keep penalties empty on error
                penalties = emptyList()
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error fetching penalties", e)
                penalties = emptyList()
            } finally {
                isRefreshingPenalties = false
            }
        }
    }

    // Fetch all penalties data (for all drivers)
    fun fetchAllPenalties(leagueId: String) {
        scope.launch {
            isRefreshingAllPenalties = true
            try {
                val response = RetrofitClient.api.getPenalties(leagueId)
                android.util.Log.d("MainActivity", "All penalties response code: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()

                    // Map all penalties without filtering by user, handling null values
                    allPenalties = data.map { penaltyResponse ->
                        Penalty(
                            protestId = penaltyResponse.protestId,
                            roundName = penaltyResponse.roundName,
                            roundNo = penaltyResponse.roundNo,
                            scoreEvent = penaltyResponse.scoreEvent ?: "",
                            driverName = penaltyResponse.displayName,
                            stewardsDecision = penaltyResponse.stewardsDecision
                        )
                    }
                    android.util.Log.d("MainActivity", "Fetched ${allPenalties.size} total penalties")
                } else {
                    android.util.Log.e("MainActivity", "Failed to fetch all penalties: ${response.code()}")
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("MainActivity", "Error body: $errorBody")
                }
            } catch (e: com.google.gson.JsonSyntaxException) {
                android.util.Log.e("MainActivity", "JSON parsing error for all penalties - API may have returned non-JSON response", e)
                allPenalties = emptyList()
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error fetching all penalties", e)
                allPenalties = emptyList()
            } finally {
                isRefreshingAllPenalties = false
            }
        }
    }

    // Fetch licence points data
    fun fetchLicencePoints(leagueId: String) {
        scope.launch {
            isRefreshingLicencePoints = true
            try {
                val response = RetrofitClient.api.getLicencePoints(leagueId)
                android.util.Log.d("MainActivity", "Licence points response code: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    android.util.Log.d("MainActivity", "Licence points data structure: ${data.size} class groups")

                    // Parse nested array structure similar to classtotals
                    licencePoints = data.flatMapIndexed { classIndex, classGroup ->
                        classGroup.mapNotNull { entry ->
                            try {
                                @Suppress("UNCHECKED_CAST")
                                val map = entry as? Map<String, Any> ?: return@mapNotNull null

                                val position = (map["Pos"] as? Double)?.toInt() ?: 0
                                val displayName = map["Name"] as? String ?: ""
                                val licencePointsValue = (map["Total"] as? Double)?.toInt() ?: 0
                                val classNumber = classIndex + 1

                                LicencePointsEntry(
                                    position = position,
                                    driverName = displayName,
                                    licencePoints = licencePointsValue,
                                    className = classNumber.toString()
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Error parsing licence points entry: $entry", e)
                                null
                            }
                        }
                    }
                    android.util.Log.d("MainActivity", "Fetched ${licencePoints.size} licence points entries")
                } else {
                    android.util.Log.e("MainActivity", "Failed to fetch licence points: ${response.code()}")
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("MainActivity", "Error body: $errorBody")
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error fetching licence points", e)
                licencePoints = emptyList()
            } finally {
                isRefreshingLicencePoints = false
            }
        }
    }

    // Fetch round events data
    fun fetchRoundEvents(leagueId: String, roundNo: Int) {
        scope.launch {
            isRefreshingRoundEvents = true
            try {
                val response = RetrofitClient.api.getFullResults(leagueId)
                android.util.Log.d("MainActivity", "Round events response code: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    android.util.Log.d("MainActivity", "Total events received: ${data.size}")

                    // Log all round numbers in the response
                    data.forEachIndexed { index, event ->
                        android.util.Log.d("MainActivity", "Event $index: roundNo=${event.roundNo}, scoreEvent=${event.scoreEvent}, trackName=${event.trackName}")
                        android.util.Log.d("MainActivity", "  Raw results: ${event.results}")
                    }

                    android.util.Log.d("MainActivity", "Looking for round $roundNo")

                    // Filter events for the selected round
                    val filteredEvents = data.filter { it.roundNo == roundNo }
                    android.util.Log.d("MainActivity", "Filtered events count: ${filteredEvents.size}")

                    // Map to domain models
                    roundEvents = filteredEvents.map { event ->
                        android.util.Log.d("MainActivity", "Mapping event: ${event.scoreEvent} with ${event.results.size} class results")

                        val mappedResults = event.results.mapNotNull { classResultMap ->
                            try {
                                // Extract class name - API uses "classname" not "class"
                                val className = classResultMap["classname"] as? String
                                if (className == null) {
                                    android.util.Log.d("MainActivity", "  Skipping result with null class name")
                                    return@mapNotNull null
                                }

                                // Extract drivers list - API uses "positions" not "drivers"
                                @Suppress("UNCHECKED_CAST")
                                val driversRaw = classResultMap["positions"] as? List<Map<String, Any>>
                                if (driversRaw == null || driversRaw.isEmpty()) {
                                    android.util.Log.d("MainActivity", "  Class $className has null/empty positions - skipping")
                                    return@mapNotNull null
                                }

                                android.util.Log.d("MainActivity", "  Class $className has ${driversRaw.size} drivers")

                                // Map drivers and sort them
                                val drivers = driversRaw.map { driverMap ->
                                    com.tudorsoft.iraceresults.data.DriverResult(
                                        position = (driverMap["finish_position_in_class_after_penalties"] as? Double)?.toInt() ?: 0,
                                        displayName = driverMap["display_name"] as? String ?: "",
                                        bestLapTime = (driverMap["best_lap_time"] as? Double)?.toLong(),
                                        lapsComplete = (driverMap["laps_complete"] as? Double)?.toInt() ?: 0,
                                        championshipPenalty = (driverMap["championship_penalty"] as? Double)?.toInt(),
                                        score = (driverMap["score"] as? Double)?.toInt() ?: 0,
                                        finishPosition = (driverMap["finish_position"] as? Double)?.toInt(),
                                        finishPositionInClass = (driverMap["finish_position_in_class"] as? Double)?.toInt(),
                                        finishPositionInClassAfterPenalties = (driverMap["finish_position_in_class_after_penalties"] as? Double)?.toInt()
                                    )
                                }.sortedWith(compareBy { driver ->
                                    // Move DQ (-1) to the end by treating as Int.MAX_VALUE
                                    val pos = driver.finishPositionInClassAfterPenalties ?: Int.MAX_VALUE
                                    if (pos == -1) Int.MAX_VALUE else pos
                                })

                                com.tudorsoft.iraceresults.data.ClassResults(
                                    className = className,
                                    drivers = drivers
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "  Error parsing class result", e)
                                null
                            }
                        }

                        android.util.Log.d("MainActivity", "  Final mapped results: ${mappedResults.size} classes")

                        com.tudorsoft.iraceresults.data.RoundEvent(
                            roundNo = event.roundNo,
                            trackName = event.trackName,
                            scoreEvent = event.scoreEvent,
                            results = mappedResults
                        )
                    }

                    android.util.Log.d("MainActivity", "Final roundEvents size: ${roundEvents.size}")
                } else {
                    android.util.Log.e("MainActivity", "Failed to fetch round events: ${response.code()}")
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("MainActivity", "Error body: $errorBody")
                    roundEvents = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error fetching round events", e)
                e.printStackTrace()
                roundEvents = emptyList()
            } finally {
                isRefreshingRoundEvents = false
            }
        }
    }

    // Fetch standings data
    fun fetchStandingsData(leagueId: String) {
        scope.launch {
            isRefreshing = true
            try {
                // Fetch classes
                val classesResponse = RetrofitClient.api.getClasses(leagueId)
                if (classesResponse.isSuccessful) {
                    val classesData = classesResponse.body() ?: emptyList()
                    classes = classesData.map {
                        RacingClass(it.className, it.classNumber.toString())
                    }
                }

                // Fetch class totals (standings)
                val standingsResponse = RetrofitClient.api.getClassTotals(leagueId)
                if (standingsResponse.isSuccessful) {
                    val standingsData = standingsResponse.body() ?: emptyList()
                    android.util.Log.d("MainActivity", "Number of class groups: ${standingsData.size}")

                    standings = standingsData.flatMapIndexed { classIndex, classGroup ->
                        classGroup.mapNotNull { entry ->
                            try {
                                // Each entry is a Map with keys like Pos, Name, Total, etc.
                                @Suppress("UNCHECKED_CAST")
                                val map = entry as? Map<String, Any> ?: return@mapNotNull null

                                val position = (map["Pos"] as? Double)?.toInt() ?: 0
                                val displayName = map["Name"] as? String ?: ""
                                val totalPoints = (map["Total"] as? Double)?.toInt() ?: 0
                                val dropPoints = (map["Drop"] as? Double)?.toInt() ?: 0
                                val penaltyPoints = (map["Penalties"] as? Double)?.toInt() ?: 0
                                // Use classIndex + 1 as the class number (1=Gold, 2=Silver, etc.)
                                val classNumber = classIndex + 1

                                // Extract round-by-round points
                                val roundPointsList = mutableListOf<com.tudorsoft.iraceresults.data.RoundPoints>()

                                // Known non-round keys to filter out
                                val nonRoundKeys = setOf("Pos", "Name", "Total", "Drop", "Penalties", "ID")

                                // Look for track name keys in the map and match them with rounds
                                map.keys.forEach { key ->
                                    if (key !in nonRoundKeys) {
                                        val points = (map[key] as? Double)?.toInt() ?: 0

                                        // Try to find matching round by comparing track names
                                        // The API uses abbreviated track names (e.g., "RBull" for "Red Bull Ring")
                                        val matchingRound = rounds.find { round ->
                                            // Check if the round's track name contains the key or vice versa
                                            round.trackName.contains(key, ignoreCase = true) ||
                                            key.contains(round.trackName, ignoreCase = true) ||
                                            // Also check if key matches start of track name
                                            round.trackName.startsWith(key, ignoreCase = true)
                                        }

                                        val roundData = if (matchingRound != null) {
                                            com.tudorsoft.iraceresults.data.RoundPoints(
                                                roundName = matchingRound.trackName,
                                                points = points
                                            )
                                        } else {
                                            // If no match found, use the key as the round name
                                            com.tudorsoft.iraceresults.data.RoundPoints(
                                                roundName = key,
                                                points = points
                                            )
                                        }

                                        roundPointsList.add(roundData)
                                    }
                                }

                                // Sort by round number if we can find matching rounds
                                roundPointsList.sortBy { roundPoint ->
                                    rounds.find { it.trackName == roundPoint.roundName }?.roundNo ?: 999
                                }

                                StandingEntry(
                                    position = position,
                                    driverName = displayName,
                                    points = totalPoints,
                                    className = classNumber.toString(),
                                    roundPoints = roundPointsList,
                                    dropPoints = dropPoints,
                                    penaltyPoints = penaltyPoints
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Error parsing standing entry: $entry", e)
                                null
                            }
                        }
                    }
                    android.util.Log.d("MainActivity", "Fetched ${standings.size} standings entries")
                    android.util.Log.d("MainActivity", "Sample standings: ${standings.take(3)}")
                    android.util.Log.d("MainActivity", "Classes: ${classes.map { "${it.name}:${it.id}" }}")
                } else {
                    android.util.Log.e("MainActivity", "Failed to fetch standings: ${standingsResponse.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error fetching standings", e)
            } finally {
                isRefreshing = false
            }
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

    // Fetch standings data when setup is complete
    LaunchedEffect(userPreferences!!.leagueId) {
        if (userPreferences!!.leagueId.isNotEmpty()) {
            fetchStandingsData(userPreferences!!.leagueId)
            fetchTeamStandings(userPreferences!!.leagueId)
            fetchRounds(userPreferences!!.leagueId)
            fetchPenalties(userPreferences!!.leagueId, userPreferences!!.custId)
            fetchAllPenalties(userPreferences!!.leagueId)
            fetchLicencePoints(userPreferences!!.leagueId)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentDrawerRoute,
                onMenuItemClick = { item ->
                    // Map drawer routes to bottom nav destinations
                    when (item.route) {
                        "tables" -> {
                            currentDestination = AppDestinations.TABLES
                            currentDrawerRoute = ""
                        }
                        "rounds" -> {
                            currentDestination = AppDestinations.ROUNDS
                            currentDrawerRoute = ""
                            selectedRound = null // Reset round selection
                        }
                        "teams" -> {
                            currentDestination = AppDestinations.TEAMS
                            currentDrawerRoute = ""
                        }
                        "penalties" -> {
                            currentDestination = AppDestinations.PENALTIES
                            currentDrawerRoute = ""
                        }
                        "licence_points" -> {
                            currentDestination = AppDestinations.LICENCE
                            currentDrawerRoute = ""
                        }
                        else -> {
                            // For drawer-only items (my_penalties, settings, about)
                            currentDrawerRoute = item.route
                        }
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
                            // Reset drawer route when navigating via bottom nav
                            currentDrawerRoute = ""
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
                                fontSize = 32.sp,
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
                // Check if drawer route overrides the current destination
                if (currentDrawerRoute.isNotEmpty()) {
                    DrawerContent(
                        route = currentDrawerRoute,
                        modifier = Modifier.padding(innerPadding),
                        onResetSetup = ::handleResetSetup,
                        teamStandings = teamStandings,
                        isRefreshingTeams = isRefreshingTeams,
                        onRefreshTeams = { fetchTeamStandings(userPreferences!!.leagueId) },
                        rounds = rounds,
                        isRefreshingRounds = isRefreshingRounds,
                        onRefreshRounds = { fetchRounds(userPreferences!!.leagueId) },
                        penalties = penalties,
                        isRefreshingPenalties = isRefreshingPenalties,
                        onRefreshPenalties = { fetchPenalties(userPreferences!!.leagueId, userPreferences!!.custId) },
                        allPenalties = allPenalties,
                        isRefreshingAllPenalties = isRefreshingAllPenalties,
                        onRefreshAllPenalties = { fetchAllPenalties(userPreferences!!.leagueId) },
                        licencePoints = licencePoints,
                        classes = classes,
                        isRefreshingLicencePoints = isRefreshingLicencePoints,
                        onRefreshLicencePoints = { fetchLicencePoints(userPreferences!!.leagueId) },
                        selectedRound = selectedRound,
                        roundEvents = roundEvents,
                        isRefreshingRoundEvents = isRefreshingRoundEvents,
                        onRoundClick = { round ->
                            selectedRound = round
                            fetchRoundEvents(userPreferences!!.leagueId, round.roundNo)
                        },
                        onRoundBack = { selectedRound = null },
                        onRefreshRoundEvents = {
                            if (selectedRound != null) {
                                fetchRoundEvents(userPreferences!!.leagueId, selectedRound!!.roundNo)
                            }
                        },
                        onClose = { currentDrawerRoute = "" }
                    )
                } else when (currentDestination) {
                    AppDestinations.TABLES -> {
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
                            classes = classes,
                            standings = standings,
                            isRefreshing = isRefreshing,
                            onRefresh = { fetchStandingsData(userPreferences!!.leagueId) }
                        )
                    }
                    AppDestinations.ROUNDS -> {
                        if (selectedRound != null) {
                            // Show round details screen
                            RoundDetailsScreen(
                                modifier = Modifier.padding(innerPadding),
                                roundNo = selectedRound!!.roundNo,
                                trackName = selectedRound!!.trackName,
                                events = roundEvents,
                                isRefreshing = isRefreshingRoundEvents,
                                onRefresh = { fetchRoundEvents(userPreferences!!.leagueId, selectedRound!!.roundNo) },
                                onBack = { selectedRound = null }
                            )
                        } else {
                            // Show rounds list screen
                            RoundsScreen(
                                modifier = Modifier.padding(innerPadding),
                                rounds = rounds,
                                isRefreshing = isRefreshingRounds,
                                onRefresh = { fetchRounds(userPreferences!!.leagueId) },
                                onRoundClick = { round ->
                                    selectedRound = round
                                    fetchRoundEvents(userPreferences!!.leagueId, round.roundNo)
                                }
                            )
                        }
                    }
                    AppDestinations.TEAMS -> {
                        TeamStandingsScreen(
                            modifier = Modifier.padding(innerPadding),
                            teamStandings = teamStandings,
                            isRefreshing = isRefreshingTeams,
                            onRefresh = { fetchTeamStandings(userPreferences!!.leagueId) }
                        )
                    }
                    AppDestinations.PENALTIES -> {
                        AllPenaltiesScreen(
                            modifier = Modifier.padding(innerPadding),
                            penalties = allPenalties,
                            isRefreshing = isRefreshingAllPenalties,
                            onRefresh = { fetchAllPenalties(userPreferences!!.leagueId) }
                        )
                    }
                    AppDestinations.LICENCE -> {
                        LicencePointsScreen(
                            modifier = Modifier.padding(innerPadding),
                            licencePoints = licencePoints,
                            classes = classes,
                            isRefreshing = isRefreshingLicencePoints,
                            onRefresh = { fetchLicencePoints(userPreferences!!.leagueId) }
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
    TABLES("Tables", Icons.AutoMirrored.Filled.List),
    ROUNDS("Rounds", Icons.Default.DateRange),
    TEAMS("Teams", Icons.Default.Person),
    PENALTIES("Penalties", Icons.Default.Warning),
    LICENCE("Licence", Icons.Default.Star),
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
    onResetSetup: () -> Unit = {},
    teamStandings: List<TeamStanding> = emptyList(),
    isRefreshingTeams: Boolean = false,
    onRefreshTeams: () -> Unit = {},
    rounds: List<Round> = emptyList(),
    isRefreshingRounds: Boolean = false,
    onRefreshRounds: () -> Unit = {},
    penalties: List<Penalty> = emptyList(),
    isRefreshingPenalties: Boolean = false,
    onRefreshPenalties: () -> Unit = {},
    allPenalties: List<Penalty> = emptyList(),
    isRefreshingAllPenalties: Boolean = false,
    onRefreshAllPenalties: () -> Unit = {},
    licencePoints: List<LicencePointsEntry> = emptyList(),
    classes: List<RacingClass> = emptyList(),
    isRefreshingLicencePoints: Boolean = false,
    onRefreshLicencePoints: () -> Unit = {},
    selectedRound: Round? = null,
    roundEvents: List<com.tudorsoft.iraceresults.data.RoundEvent> = emptyList(),
    isRefreshingRoundEvents: Boolean = false,
    onRoundClick: (Round) -> Unit = {},
    onRoundBack: () -> Unit = {},
    onRefreshRoundEvents: () -> Unit = {},
    onClose: () -> Unit = {}
) {
    when (route) {
        "rounds" -> {
            if (selectedRound != null) {
                // Show round details screen
                RoundDetailsScreen(
                    modifier = modifier,
                    roundNo = selectedRound.roundNo,
                    trackName = selectedRound.trackName,
                    events = roundEvents,
                    isRefreshing = isRefreshingRoundEvents,
                    onRefresh = onRefreshRoundEvents,
                    onBack = onRoundBack
                )
            } else {
                // Show rounds list screen
                RoundsScreen(
                    modifier = modifier,
                    rounds = rounds,
                    isRefreshing = isRefreshingRounds,
                    onRefresh = onRefreshRounds,
                    onRoundClick = onRoundClick
                )
            }
        }
        "penalties" -> {
            AllPenaltiesScreen(
                modifier = modifier,
                penalties = allPenalties,
                isRefreshing = isRefreshingAllPenalties,
                onRefresh = onRefreshAllPenalties
            )
        }
        "my_penalties" -> {
            PenaltiesScreen(
                modifier = modifier,
                penalties = penalties,
                isRefreshing = isRefreshingPenalties,
                onRefresh = onRefreshPenalties,
                onClose = onClose
            )
        }
        "licence_points" -> {
            LicencePointsScreen(
                modifier = modifier,
                licencePoints = licencePoints,
                classes = classes,
                isRefreshing = isRefreshingLicencePoints,
                onRefresh = onRefreshLicencePoints
            )
        }
        "teams" -> {
            TeamStandingsScreen(
                modifier = modifier,
                teamStandings = teamStandings,
                isRefreshing = isRefreshingTeams,
                onRefresh = onRefreshTeams
            )
        }
        "settings" -> {
            SettingsScreen(
                modifier = modifier,
                onResetSetup = onResetSetup
            )
        }
        "about" -> {
            AboutScreen(
                modifier = modifier
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