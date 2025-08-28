package de.michael.tolleapp.presentation.app1

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.*

@Composable
fun SkyjoEndScreen(
    modifier: Modifier = Modifier,
    navigateTo: (Route) -> Unit,
    viewModel: SkyjoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scrollState.animateScrollTo(
            scrollState.maxValue,
            animationSpec = tween (
                durationMillis = 1000,
                easing = LinearEasing
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text("Spiel beendet!", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        // === ROUNDS GRID ===
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header row
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Runde", style = MaterialTheme.typography.labelLarge)
                }
                state.selectedPlayerIds.filterNotNull().forEach { playerId ->
                    val player = state.players.firstOrNull { it.id == playerId } ?: return@forEach
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(player.name.take(2), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Round rows
            val maxRounds = state.perPlayerRounds.values.maxOfOrNull { it.size } ?: 0
            for (roundIndex in 1..maxRounds) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Round number cell
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(roundIndex.toString())
                    }

                    // Score cells
                    state.selectedPlayerIds.filterNotNull().forEach { playerId ->
                        val list = state.perPlayerRounds[playerId]
                        val value = list?.getOrNull(roundIndex - 1)?.toString() ?: ""
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(value)
                        }
                    }
                }
            }
            HorizontalDivider()
            // Totals row
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Σ", style = MaterialTheme.typography.labelLarge)
                }
                state.selectedPlayerIds.filterNotNull().forEach { playerId ->
                    val total = state.totalPoints[playerId] ?: 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(total.toString(), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Winner
        val winnerNames = state.players
            .filter { state.winnerId.contains(it.id) }
            .map { it.name }
            .ifEmpty { listOf("Niemand") }
            .joinToString(", ")
        Text("🏆 Gewinner: $winnerNames", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(12.dp))

        // Ranking
        Text("Rangliste:", style = MaterialTheme.typography.titleMedium)
        state.ranking.forEachIndexed { index, playerId ->
            val playerName = state.players.firstOrNull { it.id == playerId }?.name ?: playerId
            val score = state.totalPoints[playerId] ?: 0
            Text("${index + 1}. $playerName - $score Punkte")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button (
            onClick = {
                navigateTo(Route.Main)
                viewModel.resetGame()
            }
        )
        {
            Text("Zurück zum Hauptmenü")
        }
    }
}
