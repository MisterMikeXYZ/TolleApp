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
import de.michael.tolleapp.data.schwimmen.stats.SchwimmenStats

@Composable
fun SchwimmenStatsTable(
    players: List<SchwimmenStats>,
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
                "Erste raus",
                "∑ Spiele",
                "∑ Runden",
                "Ø pro Runde",
                "Bestes Ende"
            ).forEach { name ->
                Text(
                    text = if (name.isBlank()) "" else "$name:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .height(18.dp)
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
                    modifier = Modifier.width(64.dp)
                ) {
                    listOf(
                        playerNames[player.playerId] ?: "?",
                        player.wonGames,
                        player.firstOutGames,
                        player.totalGamesPlayedSchwimmen,
                        player.roundsPlayedSchwimmen,
                        if (player.roundsPlayedSchwimmen > 0)
                            player.totalGamesPlayedSchwimmen / player.roundsPlayedSchwimmen
                        else 0,
                        player.bestEndScoreSchwimmen ?: "—"
                    ).forEachIndexed { index, value ->
                        Text(
                            text = value.toString(),
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            style = if (index == 0) MaterialTheme.typography.labelLarge
                            else MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .height(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
