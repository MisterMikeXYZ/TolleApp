package de.michael.tolleapp.statistics.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.statistics.gameStats.DartStats
import kotlin.math.roundToInt

@Composable
fun PlayerStatsList(
    players: List<DartStats>,
    playerNames: Map<String, String>,
    modifier: Modifier = Modifier
) {
    // remember which players are expanded (survives recomposition & config changes)
    val expandedIds = remember { mutableStateSetOf<String>() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = players,
            key = { it.playerId }
        ) { p ->
            val expanded = p.playerId in expandedIds
            PlayerStatsCard(
                name = playerNames[p.playerId] ?: "Unbekannt",
                stats = p,
                expanded = expanded,
                onToggle = {
                    if (expanded) expandedIds.remove(p.playerId) else expandedIds.add(p.playerId)
                }
            )
        }
    }
}

@Composable
private fun PlayerStatsCard(
    name: String,
    stats: DartStats,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevronRotation")

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Collapsible header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Divider()

                    // Sections (same as before)
                    StatSection(
                        title = "Allgemein",
                        rows = listOf(
                            "Runden gespielt" to stats.roundsPlayed.toString(),
                            "Höchster Wurf" to fmtInt(stats.highestThrow)
                        )
                    )

                    StatSection(
                        title = "Spiele",
                        rows = listOf(
                            "Spiele gespielt" to stats.gamesPlayed.toString(),
                            "Spiele gewonnen" to stats.gamesWon.toString(),
                            "Siegquote" to fmtDoublePct(stats.winRate)
                        )
                    )

                    StatSection(
                        title = "High Rounds",
                        rows = listOf(
                            "60+ Runden" to fmtInt(stats.over60Rounds),
                            "100+ Runden" to fmtInt(stats.over100Rounds),
                            "140+ Runden" to fmtInt(stats.over140Rounds),
                            "Perfekte Runden (180)" to fmtInt(stats.perfectRounds),
                        )
                    )

                    StatSection(
                        title = "Würfe",
                        rows = listOf(
                            "Geworfene Darts" to stats.dartsThrown.toString(),
                            "Ø 3 Darts" to fmtAvgInt(stats.average3Darts),
                            "First-9 Ø" to fmtAvgInt(stats.first9Average),
                            "Höchste Runde" to fmtInt(stats.highestRound),
                            "Gesamtpunkte" to stats.allPoints.toString(),
                            "Dart 1 Ø" to fmtAvgInt(stats.dart1Average),
                            "Dart 2 Ø" to fmtAvgInt(stats.dart2Average),
                            "Dart 3 Ø" to fmtAvgInt(stats.dart3Average),
                        )
                    )

                    StatSection(
                        title = "Trefferraten",
                        rows = listOf(
                            "Triple-Rate" to fmtDoublePct(stats.tripleRate),
                            "Double-Rate" to fmtDoublePct(stats.doubleRate),
                            "Triple-10-Rate" to fmtDoublePct(stats.triple10Rate),
                            "Bulls-Rate" to fmtDoublePct(stats.bullsRate),
                            "Double-Bulls-Rate" to fmtDoublePct(stats.doubleBullsRate),
                        )
                    )

                    StatSection(
                        title = "Verteilung (Einzelwerte)",
                        rows = listOf(
                            "Miss (0)" to fmtDoublePct(stats.miss),
                            "Eins" to fmtDoublePct(stats.one),
                            "Zwei" to fmtDoublePct(stats.two),
                            "Drei" to fmtDoublePct(stats.three),
                            "Vier" to fmtDoublePct(stats.four),
                            "Fünf" to fmtDoublePct(stats.five),
                            "Sechs" to fmtDoublePct(stats.six),
                            "Sieben" to fmtDoublePct(stats.seven),
                            "Acht" to fmtDoublePct(stats.eight),
                            "Neun" to fmtDoublePct(stats.nine),
                            "Zehn" to fmtDoublePct(stats.ten),
                            "Elf" to fmtDoublePct(stats.eleven),
                            "Zwölf" to fmtDoublePct(stats.twelve),
                            "Dreizehn" to fmtDoublePct(stats.thirteen),
                            "Vierzehn" to fmtDoublePct(stats.fourteen),
                            "Fünfzehn" to fmtDoublePct(stats.fifteen),
                            "Sechzehn" to fmtDoublePct(stats.sixteen),
                            "Siebzehn" to fmtDoublePct(stats.seventeen),
                            "Achtzehn" to fmtDoublePct(stats.eighteen),
                            "Neunzehn" to fmtDoublePct(stats.nineteen),
                            "Zwanzig" to fmtDoublePct(stats.twenty),
                            "Bull (25)" to fmtDoublePct(stats.bull),
                        )
                    )

                    StatSection(
                        title = "Checkout",
                        rows = listOf(
                            "Höchstes Checkout" to fmtInt(stats.highestCheckout),
                            "Min. Darts" to fmtInt(stats.minDarts),
                        )
                    )
                }
            }
        }
    }
}


@Composable
private fun StatSection(
    title: String,
    rows: List<Pair<String, String>>,
) {
    // Section title
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
    )

    // Zebra rows
    Column {
        rows.forEachIndexed { index, (label, value) ->
            val bg =
                if (index % 2 == 0) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // subtle divider after each section
    Spacer(Modifier.height(8.dp))
    Divider()
}

/* ---------- tiny format helpers ---------- */

private fun fmtInt(v: Int?): String = v?.toString() ?: "—"
private fun fmtAvgInt(v: Int?): String = v?.toString() ?: "—"
private fun fmtDoublePct(v: Double?): String =
    v?.let { "${(it * 100).let { p -> (p * 10).roundToInt() / 10.0 }}%" } ?: "—"
