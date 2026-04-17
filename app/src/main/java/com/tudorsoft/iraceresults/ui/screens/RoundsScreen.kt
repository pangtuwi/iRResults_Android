package com.tudorsoft.iraceresults.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tudorsoft.iraceresults.data.Round
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.tudorsoft.iraceresults.ui.theme.LeagueTheme
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundsScreen(
    modifier: Modifier = Modifier,
    title: String = "Rounds",
    theme: LeagueTheme,
    rounds: List<Round> = emptyList(),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    onRoundClick: (Round) -> Unit = {},
    onSubsessionClick: ((Round, Int) -> Unit)? = null,
    onReportClick: ((Round) -> Unit)? = null
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (rounds.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No rounds available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rounds) { round ->
                        RoundCard(
                            round = round,
                            theme = theme,
                            onClick = { onRoundClick(round) },
                            onSubsessionClick = if (onSubsessionClick != null) { 
                                { subsessionId -> onSubsessionClick(round, subsessionId) } 
                            } else null,
                            onReportClick = if (onReportClick != null) {
                                { onReportClick(round) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoundCard(
    round: Round,
    theme: LeagueTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onSubsessionClick: ((Int) -> Unit)? = null,
    onReportClick: (() -> Unit)? = null
) {
    // Check if round is completed (start time is in the past)
    val isCompleted = remember(round.startTime) {
        try {
            val roundTime = ZonedDateTime.parse(round.startTime)
            val now = ZonedDateTime.now()
            roundTime.isBefore(now)
        } catch (e: Exception) {
            false
        }
    }

    // Use light grey for incomplete rounds, normal surface color for completed rounds
    val backgroundColor = if (isCompleted) {
        MaterialTheme.colorScheme.surface
    } else {
        Color.LightGray.copy(alpha = 0.3f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isCompleted) Modifier.clickable { onClick() }
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(theme.primary)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            // Header row with round number and date/time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Round ${round.roundNo}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatDateTime(round.startTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track name with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = round.trackName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                if (onReportClick != null) {
                    OutlinedButton(
                        onClick = onReportClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Report", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            // Subsessions row
            if (onSubsessionClick != null && !round.subsessionIds.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lap times: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    round.subsessionIds.forEachIndexed { index, subsessionId ->
                        AssistChip(
                            onClick = { onSubsessionClick(subsessionId) },
                            label = { Text("Session ${index + 1}") }
                        )
                    }
                }
            }
        }
        }
    }
}

fun formatDateTime(isoDateTime: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(isoDateTime)
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy 'at' HH:mm", Locale.ENGLISH)
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        isoDateTime
    }
}
