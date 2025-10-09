@file:Suppress("UNCHECKED_CAST")

package de.michael.tolleapp.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.schwimmen.data.stats.SchwimmenStats
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.GameType
import de.michael.tolleapp.statistics.gameStats.DartStats
import de.michael.tolleapp.statistics.gameStats.Flip7Stats
import de.michael.tolleapp.statistics.gameStats.SkyjoStats
import de.michael.tolleapp.statistics.screens.*
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatScreen(
    viewModel: StatViewModel = koinViewModel(),
    navigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val players = when (state.selectedGame) {
        GameType.SKYJO -> state.playerNames.keys.map { playerId ->
            state.playersSkyjo.find { it.playerId == playerId }
                ?: SkyjoStats(playerId = playerId) // default stats
        }.sortedByDescending { it.roundsPlayed + it.totalGames }

        GameType.SCHWIMMEN -> state.playerNames.keys.map { playerId ->
            state.playersSchwimmen.find { it.playerId == playerId }
                ?: SchwimmenStats(playerId = playerId)
        }.sortedByDescending { it.roundsPlayedSchwimmen + it.totalGamesPlayedSchwimmen }

        GameType.FLIP7 -> state.playerNames.keys.map { playerId ->
            state.playersFlip7.find { it.playerId == playerId }
                ?: Flip7Stats(playerId = playerId)
        }.sortedByDescending { it.roundsPlayed + it.totalGames }

        GameType.DART -> state.playerNames.keys.map { playerId ->
            state.playersDart.find { it.playerId == playerId }
                ?: DartStats(playerId = playerId)
        }.sortedByDescending { it.roundsPlayed + it.gamesPlayed }

        else -> null
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Statistik",
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    var resetPressed by remember { mutableStateOf(false) }
                    LaunchedEffect(resetPressed) {
                        if (resetPressed) {
                            delay(2000)
                            resetPressed = false
                        }
                    }
                    IconButton(
                        onClick = {
                            if (!resetPressed) resetPressed = true
                            else {
                                viewModel.resetCurrentGameStats()
                                resetPressed = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (!resetPressed) Icons.Default.Delete
                            else Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = if (!resetPressed) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
            //.verticalScroll(rememberScrollState())
        ) {
            // --- Dropdown for game selection ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                val str = when (state.selectedGame) {
                    GameType.SKYJO -> "Skyjo"
                    GameType.SCHWIMMEN -> "Schwimmen"
                    GameType.FLIP7 -> "Flip7"
                    GameType.DART -> "Dart"
                    else -> "Kein Spiel ausgewählt"
                }
                Button(onClick = { expanded = true }) {
                    Text("Ausgewähltes Spiel: $str")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Skyjo") },
                        onClick = {
                            viewModel.selectGame(GameType.SKYJO)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Schwimmen") },
                        onClick = {
                            viewModel.selectGame(GameType.SCHWIMMEN)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Flip7") },
                        onClick = {
                            viewModel.selectGame(GameType.FLIP7)
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Dart") },
                        onClick = {
                            viewModel.selectGame(GameType.DART)
                            expanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (players.isNullOrEmpty()) {
                Text("Keine Spieler vorhanden")
            } else {
                when (state.selectedGame) {
                    GameType.SCHWIMMEN -> {
                        // If these tables aren't lazy/scrollable themselves,
                        // wrap them in a verticalScroll *here*, not at the root.
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            @Suppress("UNCHECKED_CAST")
                            SchwimmenStatsTable(players as List<SchwimmenStats>, state.playerNames)
                        }
                    }

                    GameType.SKYJO -> {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            @Suppress("UNCHECKED_CAST")
                            SkyjoStatsTable(players as List<SkyjoStats>, state.playerNames)
                        }
                    }

                    GameType.FLIP7 -> {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            @Suppress("UNCHECKED_CAST")
                            Flip7StatsTable(players as List<Flip7Stats>, state.playerNames)
                        }
                    }

                    GameType.DART -> {
                        Column(Modifier.weight(1f).fillMaxWidth()) {
                            var showCompare by remember { mutableStateOf(false) }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { showCompare = false }, enabled = showCompare) {
                                    Text("Übersicht")
                                }
                                Button(onClick = { showCompare = true }, enabled = !showCompare) {
                                    Text("Vergleich")
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            if (showCompare) {
                                DartCompareSection(
                                    allPlayers = players as List<DartStats>,
                                    playerNames = state.playerNames,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                PlayerStatsList(
                                    players = players as List<DartStats>,
                                    playerNames = state.playerNames,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}
