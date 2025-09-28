package de.michael.tolleapp.games.skyjo.presentation

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import de.michael.tolleapp.games.skyjo.presentation.components.SkyjoPlayerDisplayRow
import de.michael.tolleapp.games.skyjo.presentation.components.keyboards.SkyjoKeyboardSwitcher
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.DividedScreen
import de.michael.tolleapp.games.util.OnHomeDialog
import de.michael.tolleapp.games.util.table.SortDirection
import de.michael.tolleapp.games.util.table.Table
import de.michael.tolleapp.games.util.table.TableStrokeOptions
import de.michael.tolleapp.games.util.table.TableStrokes
import de.michael.tolleapp.games.util.table.getSortDirectionButtonComposableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyjoGameScreen(
    navigateToMainMenu: () -> Unit,
    navigateToEnd: () -> Unit,
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

    val allInputsFilled by remember {
        derivedStateOf {
            state.selectedPlayerIds
                .filterNotNull()
                .all { id -> !points[id].isNullOrEmpty() }
        }
    }

    var keyboardExpanded by remember { mutableStateOf(false) }
    var activePlayerId by remember { mutableStateOf<String?>(null) }

    val playerListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var showOnHomeDialog by remember { mutableStateOf(false) }
    if (showOnHomeDialog) {
        OnHomeDialog(
            onSave = {
                viewModel.pauseCurrentGame()
                navigateToMainMenu()
                showOnHomeDialog = false
            },
            saveEnabled = state.perPlayerRounds.values.any { it.isNotEmpty() },
            onDiscard = {
                viewModel.deleteGame(null)
                navigateToMainMenu()
                showOnHomeDialog = false
            },
            onDismissRequest = {
                showOnHomeDialog = false
            },
        )
    }

    BackHandler {
        showOnHomeDialog = true
    }

    LaunchedEffect(state.selectedPlayerIds) {
        val selected = state.selectedPlayerIds.filterNotNull().toSet()
        val stale = points.keys - selected
        stale.forEach { points.remove(it) }
        selected.forEach { id -> points.getOrPut(id) { "" } }
    }

    LaunchedEffect(state.isGameEnded) {
        if (state.isGameEnded) {
            viewModel.endGame()
            navigateToEnd()
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Skyjo",
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
                    val hasAtLeastOneRound = state.perPlayerRounds.values.any { it.isNotEmpty() }
                    val scope = rememberCoroutineScope()
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.undoLastRound()
                            }
                        },
                        enabled = !state.isGameEnded && hasAtLeastOneRound
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
                            items(state.selectedPlayerIds.filterNotNull()) { playerId ->
                                val isActivePlayer = playerId == activePlayerId
                                SkyjoPlayerDisplayRow(
                                    playerId = playerId,
                                    state = state,
                                    isActivePlayer = isActivePlayer,
                                    points = points,
                                    totalPoints = totalPoints,
                                    onClick = {
                                        activePlayerId = playerId
                                        keyboardExpanded = true
                                    }
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
                                onClick = { viewModel.advanceDealer() },
                                modifier = Modifier.weight(1f)
                            ) { Text("Dealer") }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    viewModel.endRound(points)
                                    points.keys.forEach { id -> points[id] = "" }
                                    focusManager.moveFocus(FocusDirection.Down)
                                    keyboardManager?.hide()
                                },
                                modifier = Modifier.weight(2f),
                                enabled = allInputsFilled
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
                            onDirectionChange = { viewModel.setSortDirection(it) }
                        )
                        header = header.plus(state.selectedPlayerIds.filterNotNull().map { id ->
                            val playerName = state.playerNames[id] ?: ""
                            { Text(
                                playerName.take(2),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            ) }
                        })

                        val rows = (1..visibleRoundRows)
                            .let { if (state.sortDirection == SortDirection.ASCENDING) it else it.reversed() }
                            .map { roundIndex ->
                                val row = mutableListOf<@Composable () -> Unit>()
                                row.add { Text(roundIndex.toString()) }
                                val players = state.selectedPlayerIds.filterNotNull()
                                players.forEach { playerId ->
                                    val list = perPlayerRounds[playerId]
                                    val value = list?.getOrNull(roundIndex - 1)?.toString() ?: ""
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
                SkyjoKeyboardSwitcher(
                    activePlayerId = activePlayerId,
                    onActivePlayerChange = { newId ->
                        activePlayerId = newId
                        keyboardExpanded = newId != null
                    },
                    points = points,
                    state = state,
                    viewModel = viewModel,
                    onClose = {
                        activePlayerId = null
                        keyboardExpanded = false
                        scope.launch {
                            playerListState.animateScrollToItem(0)
                        }
                    }
                )
            }
        }
    }
}
