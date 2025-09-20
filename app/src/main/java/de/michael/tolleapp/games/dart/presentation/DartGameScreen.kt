package de.michael.tolleapp.games.dart.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import de.michael.tolleapp.games.dart.presentation.components.Keyboard
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DartGameScreen(
    viewModel: DartViewModel = koinViewModel(),
    navigateTo: (Route) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val players = state.selectedPlayerIds.filterNotNull()
    val listState = rememberLazyListState()

    LaunchedEffect(state.activePlayerIndex) {
        listState.animateScrollToItem(state.activePlayerIndex)
    }

    BackHandler { }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    "Dart",
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
                                navigateTo(Route.Main)
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
                    var resetPressedSave by remember { mutableStateOf(false) }
                    val hasAtLeastOneRound = state.perPlayerRounds.values.any { it.isNotEmpty() }
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
                                viewModel.saveCurrentGame()
                                navigateTo(Route.Main)
                                resetPressedSave = false
                            }
                        },
                        enabled = !state.isGameEnded && hasAtLeastOneRound
                    ) {
                        Icon(
                            imageVector = if (!resetPressedSave) Icons.Default.Save
                            else Icons.Default.SaveAs,
                            contentDescription = null,
                            tint = if (!hasAtLeastOneRound)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) // greyed out
                            else if (!resetPressedSave)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column (Modifier.padding(innerPadding)) {
            LazyColumn (
                state = listState,
                modifier = Modifier
                    .weight(1f)
            ) {
                itemsIndexed(players, key = { _, playerId -> playerId }) { index, playerId ->
                    viewModel.PlayerScoreDisplayFor(playerId)

                    if (index < players.lastIndex) {
                        HorizontalDivider(
                            thickness = 3.dp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            if (!state.isGameEnded) {
                Keyboard(
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
            }
        }
    }
}