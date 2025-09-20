package de.michael.tolleapp.games.skyjo.presentation

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import kotlin.collections.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyjoEndScreen(
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
    BackHandler {
        navigateTo(Route.Main)
        viewModel.resetGame()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    "Skyjo",
                    color = MaterialTheme.colorScheme.onSurface
                ) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier
                    .clip(
                        shape = MaterialTheme.shapes.extraLarge.copy(
                            topStart = CornerSize(0.dp),
                            topEnd = CornerSize(0.dp),
                        )
                    ),
                navigationIcon = {
                    var resetPressedDelete by remember { mutableStateOf(false) }
                    LaunchedEffect(resetPressedDelete) {
                        if (resetPressedDelete) {
                            delay(2000)
                            resetPressedDelete = false
                        }
                    }
                    IconButton(
                        onClick = {
                            if (!resetPressedDelete) resetPressedDelete = true
                            else {
                                viewModel.deleteGame(null)
                                navigateTo(Route.Main)
                                resetPressedDelete = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (!resetPressedDelete) Icons.Default.Delete
                            else Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = if (!resetPressedDelete) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.error
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (viewModel.undoLastRound()) navigateTo(Route.SkyjoGame)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Undo",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        val name = state.playerNames[playerId] ?: "?"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name.take(2), style = MaterialTheme.typography.labelLarge)
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
            val winnerNames = state.winnerId.mapNotNull { id -> state.playerNames[id] }
                .ifEmpty { listOf("Niemand") }
                .joinToString(", ")
            Text("🏆 Gewinner: $winnerNames", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            // Ranking
            Text("Rangliste:", style = MaterialTheme.typography.titleMedium)
            state.ranking.forEachIndexed { index, playerId ->
                val playerName = state.playerNames[playerId] ?: playerId
                val score = state.totalPoints[playerId] ?: 0
                Text("${index + 1}. $playerName - $score Punkte")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
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
}
