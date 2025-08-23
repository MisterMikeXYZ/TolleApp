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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import de.michael.tolleapp.presentation.components.BetterOutlinedTextField
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyjoGameScreen(
    modifier: Modifier = Modifier,
    navigateTo: (Route) -> Unit,
    viewModel: SkyjoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val keyboardManager = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

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
            viewModel.navigateToEndScreen()
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
        state.selectedPlayerIds.filterNotNull().forEachIndexed { index, playerId ->
            val player = state.players.firstOrNull { it.id == playerId } ?: return@forEachIndexed
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            ) {
                // Name
                BetterOutlinedTextField(
                    value = player.name,
                    label = { Text("Spieler") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Points input
                BetterOutlinedTextField(
                    value = points[playerId] ?: "",
                    onValueChange = { new ->
                        if (new.isEmpty() || new == "-" || new.toIntOrNull() in -17..140) {
                            points[playerId] = new
                        }
                    },
                    label = { Text("Punkte") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = if (index == state.selectedPlayerIds.filterNotNull().size - 1) ImeAction.Done
                            else ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardManager?.hide()
                        },
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    ),
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Total display
                BetterOutlinedTextField(
                    value = (totalPoints[playerId] ?: 0).toString(),
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
        val scrollState = rememberScrollState()

        LaunchedEffect(visibleRoundRows) {
            scrollState.animateScrollTo(
                scrollState.maxValue,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearEasing
                )
            )
        }

        Row {
            Column {

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                // Header row
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Runde",
                            style = MaterialTheme.typography.labelLarge
                        )
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
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
        }
    }
}
