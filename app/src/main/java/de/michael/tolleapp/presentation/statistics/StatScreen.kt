package de.michael.tolleapp.presentation.statistics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatScreen(
    modifier: Modifier = Modifier,
    viewModel: StatViewModel = koinViewModel(),
    navigateTo: (Route) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
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
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Column(

                ) {
                    Box ( ) {
                        Text(
                            text = "Spieler:",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box ( ) {
                        Text(
                            text = "Beste Runde:",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box ( ) {
                        Text(
                            text = ("Schlechteste" + "\n" + "Runde:"),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box ( ) {
                        Text(
                            text = "Bestes Ende:",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box ( ) {
                        Text(
                            text = ("Schlechtestes" + "\n" + "Ende:"),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box ( ) {
                        Text(
                            text = "Runden ges.:",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box ( ) {
                        Text(
                            text = "Spiele ges.:",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box ( ) {
                        Text(
                            text = "Ergebnisse ges.:",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                state.players.forEach { player ->
                    Column ()
                    {
                        Box ( ) {
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box ( ) {
                            Text(
                                text = player.bestRoundScoreSkyjo.toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box ( ) {
                            Text(
                                text = "\n" + player.worstRoundScoreSkyjo.toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box ( ) {
                            Text(
                                text = player.bestEndScoreSkyjo.toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box ( ) {
                            Text(
                                text = "\n" + player.worstEndScoreSkyjo.toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box ( ) {
                            Text(
                                text = player.roundsPlayedSkyjo.toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box ( ) {
                            Text(
                                text = player.totalGamesPlayedSkyjo.toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Box ( ) {
                            Text(
                                text = player.totalEndScoreSkyjo.toString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
        Button (
            onClick = { viewModel.resetAllGameStats() })
        {
            Text("Zurücksetzen")
        }
    }
}