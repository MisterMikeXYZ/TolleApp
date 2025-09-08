package de.michael.tolleapp.presentation.statistics.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.data.games.skyjo.SkyjoStats

@Composable
fun SkyjoStatsTable(
    players: List<SkyjoStats>,
    playerNames: Map<String, String>
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Start,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "",
                "Gewonnen",
                "Verloren",
                "∑ Spiele",
                "∑ Runden",
                "Ø pro Runde",
                "Beste Runde",
                "Schlechteste\nRunde",
                "Bestes Ende",
                "Schlechtestes\nEnde",
                "∑ Ergebnisse"
            ).forEach { name ->
                Text(
                    text = if (name.isBlank()) "" else "$name:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .height(if (name.contains("\n")) 42.dp else 18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            players.forEach { player ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(50.dp),
                ) {
                    val avg = if (player.roundsPlayed > 0 && player.avgRound != null)
                        player.avgRound else 0.0

                    listOf(
                        playerNames[player.playerId] ?: "?",
                        player.gamesWon,
                        player.gamesLost,
                        player.totalGames,
                        player.roundsPlayed,
                        String.format("%.1f", avg),
                        player.bestRound ?: "—",
                        player.worstRound?.let { "\n$it" } ?: "\n—",
                        player.bestEnd ?: "—",
                        player.worstEnd?.let { "\n$it" } ?: "\n—",
                        player.totalEnd ?: "—"
                    ).forEachIndexed { index, value ->
                        val strValue = value.toString()
                        val multiline = strValue.contains("\n")
                        Text(
                            text = strValue,
                            maxLines = if (multiline) 2 else 1,
                            overflow = TextOverflow.Clip,
                            style = if (index == 0) MaterialTheme.typography.labelLarge
                            else MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .height(if (multiline) 42.dp else 18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(1.dp))
            }
        }
    }
}
