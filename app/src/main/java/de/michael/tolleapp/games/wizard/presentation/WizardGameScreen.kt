package de.michael.tolleapp.games.wizard.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.DividedScreen
import de.michael.tolleapp.games.util.OnHomeDialog
import de.michael.tolleapp.games.util.table.SortDirection
import de.michael.tolleapp.games.util.table.Table
import de.michael.tolleapp.games.util.table.TableStrokeOptions
import de.michael.tolleapp.games.util.table.TableStrokes
import de.michael.tolleapp.games.util.table.getSortDirectionButtonComposableList
import de.michael.tolleapp.games.util.table.toTableHeader
import de.michael.tolleapp.games.util.table.toTableRowCell
import de.michael.tolleapp.games.util.table.toTableTotalCell
import de.michael.tolleapp.games.wizard.presentation.components.WizardPlayerItem

@Composable
fun WizardGameScreen(
    state: WizardState,
    onAction: (WizardAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showOnHomeDialog by remember { mutableStateOf(false) }
    if (showOnHomeDialog) {
        OnHomeDialog(
            onSave = {
                onAction(WizardAction.NavigateToMainMenu)
                showOnHomeDialog = false
            },
            onDiscard = {
                onAction(WizardAction.NavigateToMainMenu)
                onAction(WizardAction.DeleteGame)
                showOnHomeDialog = false
            },
            onDismissRequest = {
                showOnHomeDialog = false
            }
        )
    }

    BackHandler {
        showOnHomeDialog = true
    }

    LaunchedEffect(state.finished) {
        if (state.finished) {
            onAction(WizardAction.OnGameFinished)
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Wizard (${state.rounds.size}/${state.roundsToPlay})",
                navigationIcon = {
                    IconButton({ showOnHomeDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Navigate to main menu",
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { pad ->
        DividedScreen(
            modifier = modifier
                .fillMaxSize()
                .padding(pad)
                .padding(top = 8.dp),
            topPart = {
                val currentRound by remember(state.rounds) { mutableStateOf(state.rounds.lastOrNull()) }
                val highlightPlayerId by remember(currentRound) {
                    mutableStateOf(
                        (currentRound?.bids.takeIf { currentRound?.bidsFinal == false }
                            ?: currentRound?.tricksWon)?.let { inputMap ->
                            val dealerIndex =
                                state.selectedPlayers.indexOfFirst { it?.id == currentRound?.dealerId }
                            (state.selectedPlayers.subList((dealerIndex + 1) % state.selectedPlayers.size, state.selectedPlayers.size)
                                    + (state.selectedPlayers.subList(0, (dealerIndex + 1) % state.selectedPlayers.size)))
                                .firstOrNull { player -> inputMap[player?.id] == null }?.id
                        }
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        state.selectedPlayers.forEach { player ->
                            WizardPlayerItem(
                                playerName = player?.name ?: "?",
                                bidValue = currentRound?.bids[player?.id]?.toString() ?: "",
                                wonTricksValue = currentRound?.tricksWon[player?.id]?.toString()
                                    ?: "",
                                onBidChange = { newValue ->
                                    val intValue = newValue.toIntOrNull()
                                    if (player != null
                                        && (newValue == "" || (intValue?.let { it >= 0 } ?: false))
                                    ) {
                                        onAction(
                                            WizardAction.OnBidChange(
                                                playerId = player.id,
                                                newValue = intValue
                                            )
                                        )
                                    }
                                },
                                onWonTricksChange = { newValue ->
                                    val intValue = newValue.toIntOrNull()
                                    if (player != null
                                        && (newValue == "" || (intValue?.let { it >= 0 } ?: false))
                                    ) {
                                        onAction(
                                            WizardAction.OnTricksWonChange(
                                                playerId = player.id,
                                                newValue = intValue
                                            )
                                        )
                                    }
                                },
                                isDealer = currentRound?.dealerId == player?.id,
                                inputEnabled = Pair(
                                    currentRound?.bidsFinal == false,
                                    currentRound?.bidsFinal == true
                                ),
                                highlight = highlightPlayerId == player?.id,
                                keyboardAction = if (highlightPlayerId == null) {
                                    when (currentRound?.bidsFinal) {
                                        false -> {
                                            { onAction(WizardAction.FinishBidding) }
                                        }

                                        true -> {
                                            {
                                                if (currentRound?.tricksWon?.filter { it.value != null }?.size == state.selectedPlayers.size
                                                        && currentRound?.tricksWon?.values?.sumOf { it ?: 0 } == currentRound?.roundNumber)
                                                    onAction(WizardAction.FinishRound)
                                            }
                                        }

                                        else -> null
                                    }
                                } else null,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = when {
                                (currentRound?.bidsFinal == false
                                        && currentRound?.bids?.filter { it.value != null }?.size != state.selectedPlayers.size)
                                        -> stringResource(R.string.enter_bids_desc)
                                (currentRound?.bidsFinal == true
                                        && currentRound?.tricksWon?.filter { it.value != null }?.size != state.selectedPlayers.size)
                                        -> stringResource(R.string.enter_tricks_won_desc)
                                (currentRound?.bidsFinal == true
                                        && currentRound?.tricksWon?.values?.sumOf { it ?: 0 } != currentRound?.roundNumber)
                                        -> stringResource(R.string.total_tricks_won_error, currentRound?.roundNumber ?: "?")
                                else -> ""
                            },
                            color = if (currentRound?.tricksWon?.filter { it.value != null }?.size == state.selectedPlayers.size
                                && currentRound?.tricksWon?.values?.sumOf { it ?: 0 } != currentRound?.roundNumber)
                                MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        Button(
                            onClick = {
                                onAction(
                                    if (currentRound?.bidsFinal == false) WizardAction.FinishBidding
                                    else WizardAction.FinishRound
                                )
                            },
                            enabled = if (currentRound?.bidsFinal == false) currentRound?.bids?.filter { it.value != null }?.size == state.selectedPlayers.size
                                else (currentRound?.tricksWon?.filter { it.value != null }?.size == state.selectedPlayers.size
                                    && currentRound?.tricksWon?.values?.sumOf { it ?: 0 } == currentRound?.roundNumber)
                        ) {
                            Text(
                                text = if (currentRound?.bidsFinal == false) stringResource(R.string.finish_bidding)
                                else stringResource(R.string.finish_round)
                            )
                        }
                    }
                }
            },
            bottomPart = {
                Table(
                    headers = getSortDirectionButtonComposableList(
                        currentDirection = state.sortDirection,
                        onDirectionChange = { onAction(WizardAction.OnSortDirectionChange(it)) }
                    )
                        .plus(state.selectedPlayers.filterNotNull().map { it.name.take(2)}
                            .map { it.toTableHeader() }),
                    rows = state.rounds.dropLast(1)
                        .let { if (state.sortDirection == SortDirection.ASCENDING) it else it.reversed() }
                        .map { round ->
                            listOf(round.roundNumber.toString())
                                .plus(state.selectedPlayers.filterNotNull().map { player -> round.scores[player.id]?.toString() ?: "" })
                                .map { it.toTableRowCell() }
                        },
                    totalRow = listOf("∑")
                        .plus(state.selectedPlayers.map { player ->
                            state.rounds.getOrNull(state.rounds.size - 2)?.scores?.get(player?.id)?.toString()
                                ?: ""
                        })
                        .map { it.toTableTotalCell() },
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
                    headerBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                )
            }
        )
    }
}