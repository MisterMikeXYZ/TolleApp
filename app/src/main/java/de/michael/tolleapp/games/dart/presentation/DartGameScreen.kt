package de.michael.tolleapp.games.dart.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.dart.presentation.components.DartKeyboard
import de.michael.tolleapp.games.dart.presentation.components.PlayerScoreDisplays
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.OnHomeDialog
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DartGameScreen(
    viewModel: DartViewModel = koinViewModel(),
    navigateToMainMenu: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val players = state.selectedPlayerIds.filterNotNull()
    val listState = rememberLazyListState()

    LaunchedEffect(state.activePlayerIndex) {
        listState.animateScrollToItem(state.activePlayerIndex)
    }

    var showOnHomeDialog by remember { mutableStateOf(false) }
    if (showOnHomeDialog) {
        OnHomeDialog(
            onSave = {
                viewModel.saveCurrentGame()
                showOnHomeDialog = false
                navigateToMainMenu()
            },
            saveEnabled = state.perPlayerRounds.values.any { it.isNotEmpty() } && !state.isGameEnded,
            onDiscard = {
                viewModel.deleteGame()
                showOnHomeDialog = false
                navigateToMainMenu()
            },
            onDismissRequest = {
                showOnHomeDialog = false
            }
        )
    }

    BackHandler {
        showOnHomeDialog = true
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Dart",
                navigationIcon = {
                    IconButton(
                        onClick = { showOnHomeDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    val hasAtLeastOneRound = state.perPlayerRounds.values.any { it.isNotEmpty() }
                    IconButton(
                        onClick = { viewModel.undoThrow() },
                        enabled = !state.isGameEnded && hasAtLeastOneRound
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (!state.isGameEnded && hasAtLeastOneRound) 1f else 0.6f
                            )
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column (
            Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            LazyColumn (
                state = listState,
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(players, key = { _, playerId -> playerId }) { index, playerId ->
                    PlayerScoreDisplays(
                        startValue = state.gameStyle,
                        isActive = state.selectedPlayerIds.getOrNull(state.activePlayerIndex) == playerId,
                        playerState = state.toPlayerState(playerId),
                        modifier = Modifier.height(60.dp)
                    )

                    if (index < players.lastIndex) {
                        HorizontalDivider(
                            thickness = 3.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            if (!state.isGameEnded) {
                DartKeyboard(
                    onThrow = { value, multiplier ->
                        val activePlayerId = players[state.activePlayerIndex]
                        viewModel.insertThrow(activePlayerId, value, multiplier)
                    },
                    onBack = {
                        viewModel.undoThrow()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = {
                            navigateToMainMenu()
                            viewModel.resetGame()
                        }
                    )
                    {
                        Text("Zurück zum Hauptmenü")
                    }
                }
            }
        }
    }
}