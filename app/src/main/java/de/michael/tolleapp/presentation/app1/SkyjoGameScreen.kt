package de.michael.tolleapp.presentation.app1

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import de.michael.tolleapp.data.RoundResult
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyjoGameScreen(
    modifier: Modifier = Modifier,
    navigateTo: (Route) -> Unit,
    viewModel: SkyjoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Local UI state to hold per-player current round input
    val points = remember { mutableStateMapOf<String, String>() }

    // Local state: list of round scores per player (do NOT persist to DB)
    val perPlayerRounds = remember { mutableStateMapOf<String, MutableList<Int>>() }

    // Local totals (derived from perPlayerRounds, but kept here for quick UI updates)
    val totalPoints = remember { mutableStateMapOf<String, Int>() }

    // Visible row cap for the rounds grid: starts at 5 and grows in steps of 5 as needed
    var visibleRoundRows by remember { mutableIntStateOf(5) }

    // Keep state maps in sync with selected players (fixed after startGame, but safe to handle)
    LaunchedEffect(state.selectedPlayerIds) {
        val selected = state.selectedPlayerIds.filterNotNull().toSet()

        // Remove no-longer-selected players
        (points.keys - selected).forEach { points.remove(it) }
        (perPlayerRounds.keys - selected).forEach { perPlayerRounds.remove(it) }
        (totalPoints.keys - selected).forEach { totalPoints.remove(it) }

        // Ensure all selected players have entries
        selected.forEach { id ->
            points.putIfAbsent(id, "")
            perPlayerRounds.putIfAbsent(id, mutableListOf())
            totalPoints.putIfAbsent(id, 0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Spieler:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(1.dp))

        // Input row per player
        state.selectedPlayerIds.filterNotNull().forEach { playerId ->
            val player = state.players.firstOrNull { it.id == playerId } ?: return@forEach
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp)
            ) {
                // Name (readonly)
                OutlinedTextField(
                    value = player.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Spieler") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Points input (numeric)
                OutlinedTextField(
                    value = points[playerId] ?: "",
                    onValueChange = { new ->
                        if (new.isEmpty() || new == "-" || new.toIntOrNull() in -17..140) {
                            points[playerId] = new
                        }
                    },
                    label = { Text("Punkte") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Total display (readonly)
                OutlinedTextField(
                    value = (totalPoints[playerId] ?: 0).toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gesamt") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // End round button
        Button(
            onClick = {
                // Append current inputs as a new round for each player
                var anyRoundAdded = false
                state.selectedPlayerIds.filterNotNull().forEach { playerId ->
                    val score = points[playerId]?.toIntOrNull() ?: 0
                    perPlayerRounds[playerId]?.add(score)
                    totalPoints[playerId] = (totalPoints[playerId] ?: 0) + score
                    anyRoundAdded = true

                    // Persist aggregates as before; DB round row is created here,
                    // but the grid display is purely local state (perPlayerRounds).
                    RoundResult(
                        playerId = playerId,
                        gameId = state.currentGameId,
                        roundScore = score
                    )
                    viewModel.recordRound(playerId, score)

                    // End game when a player reaches >= 100
                    if ((totalPoints[playerId] ?: 0) >= 100) {
                        viewModel.endGame()
                    }
                }

                // Grow visible rows in steps of 5 when needed
                if (anyRoundAdded) {
                    val maxRoundsNow =
                        perPlayerRounds.values.maxOfOrNull { it.size } ?: 0
                    while (maxRoundsNow > visibleRoundRows) {
                        visibleRoundRows += 5
                    }
                }

                // Clear inputs after submission
                points.keys.toList().forEach { id -> points[id] = "" }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Runde beenden")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === ROUNDS GRID ===
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
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
            for (roundIndex in 1..visibleRoundRows) {
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

                    // Player score cells
                    state.selectedPlayerIds.filterNotNull().forEach { playerId ->
                        val list = perPlayerRounds[playerId]
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
        }
    }
}
