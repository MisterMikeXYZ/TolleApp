package de.michael.tolleapp.games.skyjo.presentation

import android.graphics.fonts.Font
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.papercode.composecourse.stuff.table.Table
import com.papercode.composecourse.stuff.table.TableStrokeOptions
import com.papercode.composecourse.stuff.table.TableStrokes
import de.michael.tolleapp.games.skyjo.presentation.components.SkyjoKeyboardSwitcher
import de.michael.tolleapp.games.skyjo.presentation.components.SkyjoPlayerDisplayRow
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.DividedScreen
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

    BackHandler {}

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
                            if (!resetPressedDelete) resetPressedDelete = true
                            else {
                                viewModel.deleteGame(null)
                                navigateToMainMenu()
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
                    var resetPressedSave by remember { mutableStateOf(false) }
                    LaunchedEffect(resetPressedSave) {
                        if (resetPressedSave) {
                            delay(2000)
                            resetPressedSave = false
                        }
                    }
                    IconButton(
                        onClick = {
                            if (!resetPressedSave) resetPressedSave = true
                            else {
                                viewModel.pauseCurrentGame()
                                navigateToMainMenu()
                                resetPressedSave = false
                            }
                        },
                        enabled = !state.isGameEnded && hasAtLeastOneRound
                    ) {
                        Icon(
                            imageVector = if (!resetPressedSave) Icons.Default.Save
                            else Icons.Default.SaveAs,
                            contentDescription = null,
                            tint = if (!hasAtLeastOneRound) MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.3f
                            )
                            else if (!resetPressedSave) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.primary
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
                        var header = listOf<@Composable () -> Unit>(
                            { Text(
                                "",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            ) }
                        )
                        header = header.plus(state.selectedPlayerIds.filterNotNull().map { id ->
                            val playerName = state.playerNames[id] ?: ""
                            { Text(
                                playerName.take(2),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            ) }
                        })

                        val rows = (1..visibleRoundRows).map { roundIndex ->
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

                        Table(
                            headers = header,
                            rows = rows,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
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
                            weights = listOf(0.5f) + List(state.selectedPlayerIds.filterNotNull().size) { 1f }
                        )

                        /*Column(
                            modifier = Modifier
                                .fillMaxSize()
                        )
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ){
                                Spacer(modifier = Modifier.width(20.dp))

                                VerticalDivider(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(3.dp),
                                )

                                val players = state.selectedPlayerIds.filterNotNull()
                                players.forEachIndexed { index, playerId ->
                                    val playerName = state.playerNames[playerId] ?: ""
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            playerName.take(2),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                    if (index < players.size - 1) {
                                        VerticalDivider(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(3.dp),
                                        )
                                    }
                                }
                            }

                            HorizontalDivider()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                    Column (
                                        modifier = Modifier.width(20.dp)
                                    ){
                                        for (roundIndex in 1..visibleRoundRows)
                                            Box(
                                                modifier = Modifier
                                                    .padding(4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(roundIndex.toString())
                                            }
                                    }
                                    VerticalDivider(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(3.dp),
                                    )

                                    Column {
                                        // Round rows
                                        for (roundIndex in 1..visibleRoundRows) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(IntrinsicSize.Min)
                                            ) {
                                                val players = state.selectedPlayerIds.filterNotNull()
                                                players.forEachIndexed { index, playerId ->
                                                    val list = perPlayerRounds[playerId]
                                                    val value =
                                                        list?.getOrNull(roundIndex - 1)?.toString()
                                                            ?: ""
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(4.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(value)
                                                    }

                                                    if (index < players.lastIndex) {
                                                        VerticalDivider(
                                                            modifier = Modifier
                                                                .fillMaxHeight()
                                                                .width(3.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }*/
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
