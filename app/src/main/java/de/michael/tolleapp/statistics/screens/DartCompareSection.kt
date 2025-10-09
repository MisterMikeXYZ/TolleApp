package de.michael.tolleapp.statistics.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.statistics.gameStats.DartStats
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DartCompareSection(
    allPlayers: List<DartStats>,
    playerNames: Map<String, String>,
    modifier: Modifier = Modifier
) {
    // --- selection state (in-memory; put in VM if you want it persisted) -----
    val selected = remember { mutableStateSetOf<String>() }

    Column(modifier = modifier.fillMaxSize()) {

        // ====== Player multi-select chips ======
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allPlayers.forEach { p ->
                val isSelected = p.playerId in selected
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selected.remove(p.playerId) else selected.add(p.playerId)
                    },
                    label = { Text(playerNames[p.playerId] ?: "?", maxLines = 1) }
                )
            }
        }

        // ====== Table or hint ======
        val chosen = allPlayers.filter { it.playerId in selected }
        if (chosen.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Spieler auswählen, um zu vergleichen")
            }
        } else {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                DartCompareTable(players = chosen, playerNames = playerNames)
            }
        }
    }
}

@Composable
private fun DartCompareTable(
    players: List<DartStats>,
    playerNames: Map<String, String>,
) {
    // layout config
    val labelColWidth = 160.dp
    val playerColWidth = 80.dp
    val headerHeight = 32.dp
    val hScroll = rememberScrollState()

    // rows spec: extractor -> Double? for ranking, formatter -> String, lowerIsBetter if smaller is better
    data class RowSpec(
        val label: String,
        val extractor: (DartStats) -> Double?,
        val formatter: (Double?) -> String = { defaultFormat(it) },
        val lowerIsBetter: Boolean = false
    )

    fun i(v: Int?) = v?.toDouble()
    fun d(v: Double?) = v

    val rows: List<RowSpec> = listOf(
        RowSpec("Runden gespielt", { i(it.roundsPlayed) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("Höchster Wurf", { i(it.highestThrow) }, { it?.toInt()?.toString() ?: "—" }),

        RowSpec("Spiele gespielt", { i(it.gamesPlayed) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("Spiele gewonnen", { i(it.gamesWon) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("Siegquote", { d(it.winRate) }, { pct1(it) }),

        RowSpec("60+ Runden", { i(it.over60Rounds) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("100+ Runden", { i(it.over100Rounds) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("140+ Runden", { i(it.over140Rounds) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("Perfekte Runden (180)", { i(it.perfectRounds) }, { it?.toInt()?.toString() ?: "—" }),

        RowSpec("Geworfene Darts", { i(it.dartsThrown) }, { it?.toInt()?.toString() ?: "—" }, lowerIsBetter = true),
        RowSpec("Ø 3 Darts", { i(it.average3Darts) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("First-9 Ø", { i(it.first9Average) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("Höchste Runde", { i(it.highestRound) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("Gesamtpunkte", { i(it.allPoints) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("Dart 1 Ø", { i(it.dart1Average) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("Dart 2 Ø", { i(it.dart2Average) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("Dart 3 Ø", { i(it.dart3Average) }, { it?.toInt()?.toString() ?: "—" }),

        RowSpec("Triple-Rate", { d(it.tripleRate) }, { pct1(it) }),
        RowSpec("Double-Rate", { d(it.doubleRate) }, { pct1(it) }),
        RowSpec("Triple-10-Rate", { d(it.triple10Rate) }, { pct1(it) }),
        RowSpec("Bulls-Rate", { d(it.bullsRate) }, { pct1(it) }),
        RowSpec("Double-Bulls-Rate", { d(it.doubleBullsRate) }, { pct1(it) }),

        RowSpec("Miss (0)", { d(it.miss) }, { pct1(it) }, lowerIsBetter = true),
        RowSpec("Bull (25)", { d(it.bull) }, { pct1(it) }),

        RowSpec("Höchstes Checkout", { i(it.highestCheckout) }, { it?.toInt()?.toString() ?: "—" }),
        RowSpec("Min. Darts", { i(it.minDarts) }, { it?.toInt()?.toString() ?: "—" }, lowerIsBetter = true),
    )

    // ==== UI (LazyColumn vertical; shared horizontal for header + rows) =======
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // header
        item {
            CompareHeaderRow(
                labelWidth = labelColWidth,
                playerColWidth = playerColWidth,
                headerHeight = headerHeight,
                hScroll = hScroll,
                players = players,
                playerNames = playerNames
            )
        }

        // data rows
        items(rows) { spec ->
            // compute best/worst among NON-null values
            val values = players.associate { it.playerId to spec.extractor(it) }
            val nonNull = values.values.filterNotNull()
            val best = nonNull.maxOrNull()?.takeIf { !spec.lowerIsBetter } ?: nonNull.minOrNull()
            val worst = nonNull.minOrNull()?.takeIf { !spec.lowerIsBetter } ?: nonNull.maxOrNull()

            CompareDataRow(
                label = spec.label,
                labelWidth = labelColWidth,
                playerColWidth = playerColWidth,
                hScroll = hScroll,
                players = players,
                values = values.mapValues { spec.formatter(it.value) },
                rawValues = values,
                best = best,
                worst = worst
            )
        }
    }
}

@Composable
private fun CompareHeaderRow(
    labelWidth: Dp,
    playerColWidth: Dp,
    headerHeight: Dp,
    hScroll: ScrollState,
    players: List<DartStats>,
    playerNames: Map<String, String>
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(labelWidth)
                .fillMaxHeight()
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                "Stat",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .horizontalScroll(hScroll)
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            players.forEach { p ->
                Text(
                    text = playerNames[p.playerId] ?: "?",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .width(playerColWidth)
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CompareDataRow(
    label: String,
    labelWidth: Dp,
    playerColWidth: Dp,
    hScroll: ScrollState,
    players: List<DartStats>,
    values: Map<String, String>,               // formatted
    rawValues: Map<String, Double?>,           // for comparisons
    best: Double?,
    worst: Double?,
) {
    val baseBgA = MaterialTheme.colorScheme.surface
    val baseBgB = MaterialTheme.colorScheme.surfaceVariant
    // simple zebra: hash on label to alternate
    val zebra = if (label.hashCode() and 1 == 0) baseBgA else baseBgB

    val bestBg = Color(0x334CAF50)   // translucent green
    val worstBg = Color(0x33F44336)  // translucent red

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 28.dp)
            .background(zebra),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(labelWidth)
                .fillMaxHeight()
                .padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .horizontalScroll(hScroll)
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            players.forEach { p ->
                val raw = rawValues[p.playerId]
                val isBest = best != null && raw != null && approxEq(raw, best)
                val isWorst = worst != null && raw != null && approxEq(raw, worst)

                val cellBg = when {
                    isBest -> bestBg
                    isWorst -> worstBg
                    else -> Color.Transparent
                }

                Box(
                    modifier = Modifier
                        .width(playerColWidth)
                        .background(cellBg)
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = values[p.playerId] ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/* ---------- Formatting helpers ---------- */

private fun defaultFormat(v: Double?): String = v?.let { it.toInt().toString() } ?: "—"

private fun pct1(v: Double?): String =
    v?.let { "${((it * 100.0 * 10).roundToInt() / 10.0)}%" } ?: "—"

private fun approxEq(a: Double, b: Double, eps: Double = 1e-9): Boolean = abs(a - b) <= eps
