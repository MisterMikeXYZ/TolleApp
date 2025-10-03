package de.michael.tolleapp.games.flip7.presentation

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.util.PlayerDisplayRow
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.DividedScreen
import de.michael.tolleapp.games.util.OnHomeDialog
import de.michael.tolleapp.games.util.keyboards.KeyboardSwitcher
import de.michael.tolleapp.games.util.keyboards.util.Keyboard
import de.michael.tolleapp.games.util.table.SortDirection
import de.michael.tolleapp.games.util.table.Table
import de.michael.tolleapp.games.util.table.TableStrokeOptions
import de.michael.tolleapp.games.util.table.TableStrokes
import de.michael.tolleapp.games.util.table.getSortDirectionButtonComposableList
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Flip7GameScreen(
    state: Flip7State,
    onAction: (Flip7Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showOnHomeDialog by remember { mutableStateOf(false) }
    if (showOnHomeDialog) {
        OnHomeDialog(
            onSave = {
                onAction(Flip7Action.NavigateToMainMenu)
                showOnHomeDialog = false
            },
            saveEnabled = state.rounds.isNotEmpty(),
            onDiscard = {
                onAction(Flip7Action.NavigateToMainMenu)
                onAction(Flip7Action.DeleteGame(state.gameId))
                showOnHomeDialog = false
            },
            onDismissRequest = {
                showOnHomeDialog = false
            },
        )
    }

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            state.rounds.dropLast(1)
            onAction(Flip7Action.EndGame)
        }
    }

    val keyboardManager = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var keyboardExpanded by remember { mutableStateOf(false) }
    var activePlayerId by remember { mutableStateOf<String?>(null) }

    val playerListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    BackHandler {
        if (activePlayerId != null) {
            keyboardExpanded = false
            activePlayerId = null
        } else {
            showOnHomeDialog = true
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Flip7",
                navigationIcon = {
                    IconButton(
                        onClick = {
                            showOnHomeDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    val hasAtLeastOneRound = !state.rounds.firstOrNull()?.scores.isNullOrEmpty()
                    IconButton(
                        onClick = { onAction(Flip7Action.UndoLastRound) },
                        enabled = hasAtLeastOneRound
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint =  MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (!hasAtLeastOneRound) 0.3f else 1f
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DividedScreen(
                modifier = Modifier.fillMaxSize(),
                startTopFraction = 0.6f,
                topPart = {
                    Column(Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            state = playerListState
                        ) {
                            items(state.selectedPlayers.filterNotNull()) { player ->
                                PlayerDisplayRow(
                                    player = player,
                                    isDealer = state.currentDealerId == player.id,
                                    roundScore = state.rounds.lastOrNull()?.scores?.get(player.id),
                                    totalScore = state.totalPoints[player.id],
                                    onClick = {
                                        activePlayerId = player.id
                                        keyboardExpanded = true
                                    },
                                    isActivePlayer = activePlayerId == player.id,
                                )
                            }
                        }

                        LaunchedEffect(activePlayerId) {
                            val players = state.selectedPlayerIds.filterNotNull()
                            val index = players.indexOf(activePlayerId)
                            if (index >= 0) {
                                scope.launch {
                                    playerListState.animateScrollToItem(index)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 8.dp,
                                    end = 8.dp,
                                    bottom = 4.dp,
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onAction(Flip7Action.AdvanceDealer) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Dealer") }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    onAction(Flip7Action.EndRound)
                                    focusManager.moveFocus(FocusDirection.Down)
                                    keyboardManager?.hide()
                                },
                                modifier = Modifier.weight(2f),
                                enabled = state.rounds.lastOrNull()?.scores?.size
                                        == state.selectedPlayers.filterNotNull().size
                            ) { Text("Runde beenden") }
                        }
                    }
                },
                bottomPart = {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        var header = getSortDirectionButtonComposableList(
                            currentDirection = state.sortDirection,
                            onDirectionChange = { onAction(Flip7Action.OnSortDirectionChange(it)) }
                        )
                        header = header.plus(state.selectedPlayerIds.filterNotNull().map { id ->
                            val playerName = state.selectedPlayers.filterNotNull().find { it.id == id }?.name
                                ?: "?"
                            { Text(
                                playerName.take(2),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            ) }
                        })

                        val rows = (1..state.visibleRoundRows)
                            .let { if (state.sortDirection == SortDirection.ASCENDING) it else it.reversed() }
                            .map { roundIndex ->
                                val row = mutableListOf<@Composable () -> Unit>()
                                row.add { Text(roundIndex.toString()) }
                                val players = state.selectedPlayerIds.filterNotNull()
                                players.forEach { playerId ->
                                    val value = state.rounds.dropLast(1)
                                        .getOrNull(roundIndex - 1)
                                        ?.scores?.get(playerId)
                                        ?.toString() ?: ""
                                    row.add { Text(value) }
                                }
                                row
                            }

                        val weights = if(state.selectedPlayerIds.filterNotNull().size <= 4) {
                            listOf(1f) + List(state.selectedPlayerIds.filterNotNull().size) { 2f }
                        } else {
                            listOf(1f) + List(state.selectedPlayerIds.filterNotNull().size) { 1f }
                        }

                        Table(
                            headers = header,
                            rows = rows,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                            cellPadding = 4.dp,
                            tableStrokes = TableStrokes(
                                vertical = TableStrokeOptions.ALL,
                                horizontal = TableStrokeOptions.START,
                                outer = false,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                width = 2.dp
                            ),
                            headerBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            weights = weights
                        )
                    }
                }
            )
            AnimatedVisibility(
                visible = keyboardExpanded,
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
                    keyboards = listOf(Keyboard.NUMBER, Keyboard.FLIP7),
                    onSubmit = { newScore ->
                        activePlayerId?.let { id ->
                            onAction(Flip7Action.InputScore(id, newScore))
                            // Increment playerId
                            val currentIndex = state.selectedPlayerIds.indexOf(id)
                            val nextId = state.selectedPlayerIds.getOrNull((currentIndex + 1) % state.selectedPlayerIds.size)
                            if (nextId != null && state.rounds.lastOrNull()?.scores[nextId] == null) {
                                activePlayerId = nextId
                            } else {
                                activePlayerId = null
                                keyboardExpanded = false
                            }
                        } ?: run {
                            keyboardExpanded = false
                        }
                    },
                    onHideKeyboard = {
                        keyboardExpanded = false
                        activePlayerId = null
                    }
                )
            }
        }
    }
}
