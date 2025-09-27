package de.michael.tolleapp.games.romme.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.R
import de.michael.tolleapp.games.romme.presentation.components.RommePlayerItem
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.DividedScreen
import de.michael.tolleapp.games.util.OnHomeDialog
import de.michael.tolleapp.games.util.keyboards.KeyboardSwitcher
import de.michael.tolleapp.games.util.keyboards.util.Keyboard
import de.michael.tolleapp.games.util.table.Table
import de.michael.tolleapp.games.util.table.TableStrokeOptions
import de.michael.tolleapp.games.util.table.TableStrokes
import de.michael.tolleapp.games.util.table.toTableRowCell
import de.michael.tolleapp.games.util.table.toTableHeader

@Composable
fun RommeGameScreen(
    state: RommeState,
    onAction: (RommeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showOnHomeDialog by remember { mutableStateOf(false) }
    if (showOnHomeDialog) {
        OnHomeDialog(
            onSave = {
                onAction(RommeAction.NavigateToMainMenu)
                showOnHomeDialog = false
            },
            onDiscard = {
                onAction(RommeAction.NavigateToMainMenu)
                onAction(RommeAction.DeleteGame)
                showOnHomeDialog = false
            },
            onDismissRequest = {
                showOnHomeDialog = false
            }
        )
    }

    val lazyListState = rememberLazyListState()
    var currentlyEditingScoreForPlayerId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentlyEditingScoreForPlayerId) {
        if (currentlyEditingScoreForPlayerId != null) {
            val index = state.selectedPlayers.indexOfFirst { it?.id == currentlyEditingScoreForPlayerId }
            if (index >= 0) lazyListState.animateScrollToItem(index)
        }
    }

    BackHandler {
        if (currentlyEditingScoreForPlayerId != null)
            currentlyEditingScoreForPlayerId = null
        else
            showOnHomeDialog = true
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Rommé",
                navigationIcon = {
                    IconButton({ showOnHomeDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Navigate to main menu",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onAction(RommeAction.OnGameFinished) },
                        enabled = state.rounds.firstOrNull()?.roundScores?.values?.filterNotNull()?.size
                                == state.selectedPlayers.filterNotNull().size
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowCircleRight,
                            contentDescription = "Finish game",
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { pad ->
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(pad)
                .fillMaxSize()
        ) {
            DividedScreen(
                topPart = {
                    val currentRound by remember(state.rounds) { mutableStateOf(state.rounds.lastOrNull()) }
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                    ) {
                        LazyColumn (
                            state = lazyListState,
                            modifier = modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(state.selectedPlayers) { player ->
                                RommePlayerItem(
                                    playerName = player?.name ?: "?",
                                    roundScore = currentRound?.roundScores[player?.id],
                                    totalScore = currentRound?.finalScores[player?.id],
                                    highlighted = currentlyEditingScoreForPlayerId == player?.id,
                                    onClick = {
                                        currentlyEditingScoreForPlayerId = player?.id
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "",
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .weight(1f)
                            )
                            Button(
                                onClick = { onAction(RommeAction.FinishRound) },
                                enabled = state.rounds.lastOrNull()?.roundScores?.values?.filterNotNull()?.size
                                        == state.selectedPlayers.size
                            ) {
                                Text(text = stringResource(R.string.finish_round))
                            }
                        }
                    }
                },
                bottomPart = {
                    Table(
                        headers = (listOf("") + state.selectedPlayers.filterNotNull().map { it.name.take(2) }).map { it.toTableHeader() },
                        rows = state.rounds.reversed().map { roundData ->
                            (listOf(roundData.roundNumber.toString()) + roundData.roundScores.entries.toList().sortedBy { (key, _) ->
                                state.selectedPlayers.indexOfFirst { it?.id == key }
                            }.map { it.value?.toString() ?: "" }).map { it.toTableRowCell() }
                        },
                        totalRow = state.rounds.lastOrNull()?.finalScores?.entries?.toList()?.sortedBy { (key, _) ->
                            state.selectedPlayers.indexOfFirst { it?.id == key }
                        }?.let { finalScores ->
                            (listOf("∑") + finalScores.map { it.value?.toString() ?: "" }).map { it.toTableRowCell() }
                        },
                        frozenStartColumns = 1,
                        weights = listOf(1f) + List(state.selectedPlayers.filterNotNull().size) { 1.5f },
                        minCellWidth = 32.dp,
                        cellPadding = 4.dp,
                        tableStrokes = TableStrokes(
                            vertical = TableStrokeOptions.ALL,
                            horizontal = TableStrokeOptions.START_END,
                            outer = false,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            width = 2.dp
                        ),
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
            AnimatedVisibility(
                visible = currentlyEditingScoreForPlayerId != null,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                KeyboardSwitcher(
                    keyboards = listOf(Keyboard.NUMBER_WITH_2X, Keyboard.ROMME),
                    onSubmit = { newValue ->
                        currentlyEditingScoreForPlayerId?.let { playerId ->
                            onAction(RommeAction.OnRoundScoreChange(playerId, newValue))
                        }
                        val selectedPlayers = state.selectedPlayers.filterNotNull()
                        currentlyEditingScoreForPlayerId = selectedPlayers
                            .get((selectedPlayers.indexOfFirst { it.id == currentlyEditingScoreForPlayerId } + 1) % selectedPlayers.size)
                            .id
                    },
                    onHideKeyboard = { currentlyEditingScoreForPlayerId = null }
                )
            }
        }
    }
}