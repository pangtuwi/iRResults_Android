package com.tudorsoft.iraceresults.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.values.AxisValuesOverrider
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.tudorsoft.iraceresults.data.DriverResult
import com.tudorsoft.iraceresults.data.StandingEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LapTimeGraphScreen(
    leagueId: String,
    roundNo: Int, // for display purposes
    subsessionId: Int,
    sessionIndex: Int = 1,
    userCustId: Int,
    allSessionDrivers: List<DriverResult>,
    standings: List<StandingEntry> = emptyList(),
    perpetuatedOpponents: Set<Int>? = null,
    onOpponentsChange: (Set<Int>) -> Unit = {},
    isSyncingData: Boolean = false,
    onBack: () -> Unit,
    viewModel: LapTimesViewModel = viewModel()
) {
    val lapData by viewModel.lapData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showOpponentSelect by remember { mutableStateOf(false) }

    // Deduplicate drivers, remove any drivers without valid cust_id (e.g., 0)
    val validOpponents = remember(allSessionDrivers) {
        allSessionDrivers.filter { it.custId != 0 && it.custId != userCustId }.distinctBy { it.custId }
    }
    
    val userDriver = remember(allSessionDrivers) {
        allSessionDrivers.find { it.custId == userCustId }
    }

    // Determine the active opponents based on hoisted state and class leader logic
    val activeOpponents = remember(perpetuatedOpponents, allSessionDrivers.size) {
        if (allSessionDrivers.isEmpty()) {
            emptySet()
        } else if (perpetuatedOpponents == null) {
            // First time logic: Find Class Leader
            var leaderCustId: Int? = null
            if (userDriver != null) {
                val userClass = standings.find { it.driverName == userDriver.displayName }?.className
                if (userClass != null) {
                    val leaderName = standings.find { it.className == userClass && it.position == 1 }?.driverName
                    if (leaderName != null && leaderName != userDriver.displayName) {
                        leaderCustId = allSessionDrivers.find { it.displayName == leaderName }?.custId
                    }
                }
            }
            // If no leader found or user isn't in a class, default to an empty set.
            val initialSet = if (leaderCustId != null) setOf(leaderCustId) else emptySet()
            // Dispatch to parent to save this initial state
            onOpponentsChange(initialSet)
            initialSet
        } else {
            // Perpetuated state: Filter by those actually present in this session
            val survivingOpponents = perpetuatedOpponents.filter { custId ->
                validOpponents.any { it.custId == custId }
            }.toSet()
            
            // If the set changed due to filtering, update parent
            if (survivingOpponents.size != perpetuatedOpponents.size) {
                onOpponentsChange(survivingOpponents)
            }
            survivingOpponents
        }
    }

    // Every time selection or drivers load changes, refetch
    LaunchedEffect(leagueId, subsessionId, activeOpponents, allSessionDrivers.size) {
        if (allSessionDrivers.isEmpty()) {
            // Data is still loading from MainActivity API call, do not fetch yet
            return@LaunchedEffect
        }
        val driversToFetch = mutableListOf<DriverResult>()
        if (userDriver != null) driversToFetch.add(userDriver)
        
        validOpponents.filter { activeOpponents.contains(it.custId) }.forEach {
            driversToFetch.add(it)
        }
        
        if (driversToFetch.isEmpty() && validOpponents.isNotEmpty()) {
             // User has no valid custId or isn't in race, fetch top 3 instead
            val fallback = validOpponents.take(3)
            driversToFetch.addAll(fallback)
            onOpponentsChange(fallback.map { it.custId }.toSet())
        }
        
        viewModel.fetchLapTimes(leagueId, subsessionId, driversToFetch)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lap Times - Round $roundNo (Session $sessionIndex)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if ((isLoading || isSyncingData) && lapData.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null && lapData.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Error", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                }
            } else if (lapData.isEmpty() && !isLoading) {
                Text("No lap data available for this session.", modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Calculate Min and Max for 1-second interval Y-axis
                    val minLapTimeSeconds = remember(lapData) {
                        val allLaps = lapData.flatMap { it.lapTimes }
                        if (allLaps.isNotEmpty()) (allLaps.minOrNull() ?: 0L) / 1000f else 0f
                    }
                    val maxLapTimeSeconds = remember(lapData) {
                        val allLaps = lapData.flatMap { it.lapTimes }
                        if (allLaps.isNotEmpty()) (allLaps.maxOrNull() ?: 0L) / 1000f else 0f
                    }

                    var isScaledDown by remember { mutableStateOf(false) }

                    val floorMinRaw = kotlin.math.floor(minLapTimeSeconds.toDouble()).toFloat()
                    val floorMin = if (isScaledDown) floorMinRaw else floorMinRaw - 1f
                    val minYVal = if (floorMin > 0f) floorMin else 0f
                    val ceilMax = kotlin.math.ceil(maxLapTimeSeconds) + 1f
                    val maxYVal = if (isScaledDown) minYVal + 4f else if (ceilMax > minYVal) ceilMax else minYVal + 5f
                    val yRangeCount = (maxYVal - minYVal).toInt() + 1
                    
                    // Build Chart Model and enforce the physical scaling limits on the nodes
                    val producer = remember(lapData, isScaledDown) {
                        val seriesList = lapData.map { driverData ->
                            driverData.lapTimes.mapIndexed { index, timeMs ->
                                val seconds = timeMs / 1000f
                                FloatEntry(x = (index + 1).toFloat(), y = if (seconds > maxYVal) maxYVal else seconds)
                            }
                        }
                        ChartEntryModelProducer(seriesList)
                    }
                    
                    val chartColors = listOf(Color(0xFF2196F3), Color(0xFFF44336), Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF9C27B0))
                    val lineSpecs = lapData.mapIndexed { index, _ ->
                        lineSpec(
                            lineColor = chartColors[index % chartColors.size]
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lap Times (Seconds)", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (isScaledDown) "Reset Scale" else "Scale",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { isScaledDown = !isScaledDown }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Chart(
                        chart = lineChart(
                            lines = lineSpecs,
                            axisValuesOverrider = AxisValuesOverrider.fixed(
                                minY = minYVal,
                                maxY = maxYVal
                            )
                        ),
                        chartModelProducer = producer,
                        startAxis = rememberStartAxis(
                            itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = yRangeCount)
                        ),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Legend / Opponent Selector
                    Row(
                        modifier = Modifier
                            .clickable { showOpponentSelect = true }
                            .padding(bottom = 8.dp, top = 4.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select Opponents", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                            contentDescription = "Edit Selection",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    LazyColumn {
                        items(lapData.size) { index ->
                            val driver = lapData[index]
                            val color = chartColors[index % chartColors.size]
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Box(modifier = Modifier.size(16.dp).background(color).padding(end = 8.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = driver.displayName + if (driver.custId == userCustId) " (You)" else "")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOpponentSelect) {
        AlertDialog(
            onDismissRequest = { showOpponentSelect = false },
            title = { Text("Select up to 3 Opponents") },
            text = {
                LazyColumn {
                    items(validOpponents) { oppo ->
                        val isSelected = activeOpponents.contains(oppo.custId)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        onOpponentsChange(activeOpponents - oppo.custId)
                                    } else if (activeOpponents.size < 3) {
                                        onOpponentsChange(activeOpponents + oppo.custId)
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(oppo.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOpponentSelect = false }) {
                    Text("Done")
                }
            }
        )
    }
}
