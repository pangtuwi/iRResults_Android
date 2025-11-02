package com.tudorsoft.iraceresults.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tudorsoft.iraceresults.data.LicencePointsEntry
import com.tudorsoft.iraceresults.data.RacingClass
import com.tudorsoft.iraceresults.ui.theme.BronzeButton
import com.tudorsoft.iraceresults.ui.theme.GoldButton
import com.tudorsoft.iraceresults.ui.theme.SilverButton
import com.tudorsoft.iraceresults.ui.theme.UnclassifiedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicencePointsScreen(
    modifier: Modifier = Modifier,
    licencePoints: List<LicencePointsEntry> = emptyList(),
    classes: List<RacingClass> = emptyList(),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    var selectedClass by remember { mutableStateOf(classes.firstOrNull()?.id ?: "") }

    // Update selected class when classes change
    LaunchedEffect(classes) {
        if (selectedClass.isEmpty() && classes.isNotEmpty()) {
            selectedClass = classes.firstOrNull()?.id ?: ""
        }
    }

    // Filter licence points by selected class
    val filteredLicencePoints = licencePoints.filter {
        selectedClass.isEmpty() || it.className == selectedClass
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Licence Points",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Class Selection
            if (classes.isNotEmpty()) {
                ClassSelectionSection(
                    classes = classes,
                    selectedClass = selectedClass,
                    onClassSelected = { selectedClass = it }
                )
            }

            // Licence Points Table
            LicencePointsTable(
                licencePoints = filteredLicencePoints
            )
        }
    }
}

@Composable
fun ClassSelectionSection(
    classes: List<RacingClass>,
    selectedClass: String,
    onClassSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(classes) { racingClass ->
                ClassFilterButton(
                    racingClass = racingClass,
                    isSelected = selectedClass == racingClass.id,
                    onClick = { onClassSelected(racingClass.id) }
                )
            }
        }
    }
}

@Composable
fun ClassFilterButton(
    racingClass: RacingClass,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonColor = when (racingClass.name.lowercase()) {
        "gold" -> GoldButton
        "silver" -> SilverButton
        "bronze" -> BronzeButton
        "unclassified" -> UnclassifiedButton
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val textColor = Color.Black

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = textColor
        ),
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    ) {
        Text(
            text = racingClass.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun LicencePointsTable(
    licencePoints: List<LicencePointsEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        if (licencePoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No licence points data available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pos",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.width(40.dp)
                )
                Text(
                    text = "Driver",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Points",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(60.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

            // Table Rows
            LazyColumn {
                itemsIndexed(licencePoints) { index, entry ->
                    LicencePointsRow(entry = entry)
                    if (index < licencePoints.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    }
                }
            }
        }
    }
}

@Composable
fun LicencePointsRow(
    entry: LicencePointsEntry,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = entry.position.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = entry.driverName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = entry.licencePoints.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp)
        )
    }
}
