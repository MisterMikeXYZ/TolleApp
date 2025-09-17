package de.michael.tolleapp.presentation.dart

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import de.michael.tolleapp.presentation.dart.components.PlayerScoreDisplays
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DartGameScreen(
    viewModel: DartViewModel = koinViewModel(),
    navigateTo: (Route) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var testRounds by remember { mutableStateOf(state.perPlayerRounds) }

    BackHandler { }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.padding(top = 20.dp))
        state.selectedPlayerIds.forEach { playerId ->
            if (playerId != null) {
                val playerName = state.playerNames[playerId] ?: "Player"
                val playerRounds = testRounds[playerId] ?: emptyList()

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlayerScoreDisplays(
                        playerId = playerId,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val updatedRounds = playerRounds + listOf(listOf(20, 5, 1))
                            testRounds = testRounds.toMutableMap().apply {
                                put(playerId, updatedRounds)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Round for $playerName")
                    }
                }
            }
        }
    }
}