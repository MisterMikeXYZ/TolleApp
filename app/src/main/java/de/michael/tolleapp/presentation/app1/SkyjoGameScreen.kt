package de.michael.tolleapp.presentation.app1

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    val perPlayerRounds = state.perPlayerRounds
    val totalPoints = state.totalPoints
    val visibleRoundRows = state.visibleRoundRows

    // Keep only the per-round input fields in sync with the selected players.
    // Do NOT touch perPlayerRounds/totalPoints here; read them from state.
    LaunchedEffect(state.selectedPlayerIds) {
        val selected = state.selectedPlayerIds.filterNotNull().toSet()

        // Remove inputs for de-selected players
        val stale = (points.keys - selected).toList()
        stale.forEach { points.remove(it) }

        // Ensure an input entry exists for every selected player
        selected.forEach { id ->
            points.getOrPut(id) { "" }
        }
    }

    LaunchedEffect(state.isGameEnded) {
        if (state.isGameEnded) {
            navigateTo(Route.SkyjoEnd)
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
                // Name
                OutlinedTextField(
                    value = player.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Spieler") },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Points input
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

                // Total display
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


        Button(
            onClick = {
                viewModel.endRound(points)
                points.keys.toList().forEach { id -> points[id] = "" } // clear inputs

                if (state.isGameEnded) {
                    navigateTo(Route.SkyjoEnd)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Runde beenden")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // === ROUNDS GRID ===
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
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

                    // SkyjoPlayer score cells
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
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                // End the game and navigate to the end screen
                viewModel.endGame()
                navigateTo(Route.SkyjoEnd)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Spiel beenden")
        }
    }
}
