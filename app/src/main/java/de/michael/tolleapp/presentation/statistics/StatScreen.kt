package de.michael.tolleapp.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import de.michael.tolleapp.data.games.schwimmen.stats.SchwimmenStats
import de.michael.tolleapp.data.games.skyjo.SkyjoStats
import de.michael.tolleapp.presentation.statistics.screens.SchwimmenStatsTable
import de.michael.tolleapp.presentation.statistics.screens.SkyjoStatsTable
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatScreen(
    viewModel: StatViewModel = koinViewModel(),
    navigateTo: (Route) -> Unit,
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
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Statistik") },
                navigationIcon = {
                    IconButton(onClick = { navigateTo(Route.Main) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
                .padding(start = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // --- Dropdown for game selection ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                val str = when (state.selectedGame) {
                    GameType.SKYJO -> "Skyjo"
                    GameType.SCHWIMMEN -> "Schwimmen"
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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (players.isEmpty()) {
                Text("Keine Spieler vorhanden")
            } else {
                if (state.selectedGame == GameType.SKYJO) {
                    SkyjoStatsTable(players as List<SkyjoStats>, state.playerNames)
                } else {
                    SchwimmenStatsTable(players as List<SchwimmenStats>, state.playerNames)
                }
            }
        }
    }
}
