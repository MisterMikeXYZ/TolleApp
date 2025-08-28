package de.michael.tolleapp.presentation.statistics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatScreen(
    viewModel: StatViewModel = koinViewModel(),
    navigateTo: (Route) -> Unit,
) {
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
                                viewModel.resetAllGameStats()
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
        ) {
            //get the player List from the database according to the game that is selected
            LaunchedEffect(Unit) {
                viewModel.getPlayers()
            }
            val state by viewModel.state.collectAsState()


            if (state.players.isEmpty()) {
                Text("Keine Spieler vorhanden")
            } else {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Column(
                        verticalArrangement = spacedBy(8.dp)
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
                        state.players.forEach { player ->
                            Column(
                                verticalArrangement = spacedBy(8.dp)
                            ) {
                                listOf(
                                    player.name,
                                    player.wonGames,
                                    player.lostGames,
                                    player.totalGamesPlayedSkyjo,
                                    player.roundsPlayedSkyjo,
                                    player.totalEndScoreSkyjo / (if (player.roundsPlayedSkyjo == 0) 1 else player.roundsPlayedSkyjo),
                                    player.bestRoundScoreSkyjo,
                                    player.worstRoundScoreSkyjo?.let { "\n$it" } ?: "\n—",
                                    player.bestEndScoreSkyjo,
                                    player.worstEndScoreSkyjo?.let { "\n$it" } ?: "\n—",
                                    player.totalEndScoreSkyjo
                                ).forEachIndexed { index, value ->
                                    val multiline = value?.toString()?.contains("\n") == true
                                    Text(
                                        text = value?.toString() ?: "—",
                                        maxLines = if (multiline) 2 else 1,
                                        overflow = TextOverflow.Clip,
                                        style = if (index == 0) MaterialTheme.typography.labelLarge
                                        else MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .height(
                                                if (multiline) 42.dp
                                                else 18.dp
                                            )
                                            .width(60.dp),
                                    )
                                    //if (index == 0) { HorizontalDivider() }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }
        }
    }
}