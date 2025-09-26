package de.michael.tolleapp.games.skyjo.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.util.table.Table
import de.michael.tolleapp.games.util.table.TableStrokeOptions
import de.michael.tolleapp.games.util.table.TableStrokes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.collections.getOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyjoEndScreen(
    navigateToGameScreen: () -> Unit,
    navigateToMainMenu: () -> Unit,
    viewModel: SkyjoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        scrollState.animateScrollTo(
            scrollState.maxValue,
            animationSpec = tween (
                durationMillis = 1000,
                easing = LinearEasing
            )
        )
    }
    BackHandler {
        navigateToMainMenu()
        viewModel.resetGame()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    "Skyjo",
                    color = MaterialTheme.colorScheme.onSurface
                ) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier
                    .clip(
                        shape = MaterialTheme.shapes.extraLarge.copy(
                            topStart = CornerSize(0.dp),
                            topEnd = CornerSize(0.dp),
                        )
                    ),
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
                    val scope = rememberCoroutineScope()
                    IconButton(
                        onClick = {
                            scope.launch {
                                val undone = viewModel.undoLastRound()
                                if (undone) navigateToGameScreen()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text("Spiel beendet!", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))


            Box(
                Modifier
                    .height(intrinsicSize = IntrinsicSize.Min)
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

                val rows = (1..state.currentGameRounds).map { roundIndex ->
                    val row = mutableListOf<@Composable () -> Unit>()
                    row.add { Text(roundIndex.toString()) }
                    val players = state.selectedPlayerIds.filterNotNull()
                    players.forEach { playerId ->
                        val list = state.perPlayerRounds[playerId]
                        val value = list?.getOrNull(roundIndex - 1)?.toString() ?: ""
                        row.add { Text(value) }
                    }
                    row
                }

                val weights = if(state.selectedPlayerIds.size <= 4) listOf(1f) + List(state.selectedPlayerIds.filterNotNull().size) { 2f } else listOf(1f) + List(state.selectedPlayerIds.filterNotNull().size) { 1f }
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
                    weights = weights
                )
            }
//            Row(modifier = Modifier
//                .fillMaxWidth()
//                .height(intrinsicSize = IntrinsicSize.Min)
//            ) {
//                Column(
//                    modifier = Modifier.width(intrinsicSize = IntrinsicSize.Min),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Row(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
//                        Box(
//                            modifier = Modifier.padding(4.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "Runde",
//                                style = MaterialTheme.typography.labelLarge,
//                                maxLines = 1,
//                            )
//                        }
//                    }
//
//                    HorizontalDivider()
//
//                    val maxRounds = state.perPlayerRounds.values.maxOfOrNull { it.size } ?: 0
//                    for (roundIndex in 1..maxRounds) {
//                        Row (modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)){
//                            Box(
//                                modifier = Modifier.padding(4.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    roundIndex.toString(),
//                                    style = MaterialTheme.typography.labelLarge,
//                                    maxLines = 1,
//                                )
//                            }
//                        }
//                    }
//
//                    HorizontalDivider()
//
//                    Row(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
//                        Box(
//                            modifier = Modifier.padding(4.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "Σ",
//                                style = MaterialTheme.typography.labelLarge,
//                                maxLines = 1,
//                            )
//                        }
//                    }
//                }
//
//                VerticalDivider(modifier = Modifier.fillMaxHeight())
//
//                // === ROUNDS GRID ===
//                Row {
//                    state.selectedPlayerIds.filterNotNull().forEach {
//                        SkyjoEndScreenPlayerColumn(
//                            playerId = it,
//                            state = state,
//                            modifier = Modifier.width(40.dp)
//                        )
//                    }
//                }
//            }

            Spacer(modifier = Modifier.height(24.dp))

            // Winner
            val winnerNames = state.winnerId.mapNotNull { id -> state.playerNames[id] }
                .ifEmpty { listOf("Niemand") }
                .joinToString(", ")
            Text("🏆 Gewinner: $winnerNames", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(12.dp))

            // Ranking
            Text("Rangliste:", style = MaterialTheme.typography.titleMedium)
            state.ranking.forEachIndexed { index, playerId ->
                val playerName = state.playerNames[playerId] ?: playerId
                val score = state.totalPoints[playerId] ?: 0
                Text("${index + 1}. $playerName - $score Punkte")
            }

            Spacer(modifier = Modifier.height(24.dp))
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