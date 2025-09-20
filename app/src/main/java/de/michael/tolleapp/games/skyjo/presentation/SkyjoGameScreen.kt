package de.michael.tolleapp.games.skyjo.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.michael.tolleapp.Route
import de.michael.tolleapp.games.skyjo.presentation.components.BetterOutlinedTextField
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyjoGameScreen(
    modifier: Modifier = Modifier,
    navigateTo: (Route) -> Unit,
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

    BackHandler {
    }

    LaunchedEffect(state.selectedPlayerIds) {
        val selected = state.selectedPlayerIds.filterNotNull().toSet()
        val stale = (points.keys - selected).toList()
        stale.forEach { points.remove(it) }
        selected.forEach { id ->
            points.getOrPut(id) { "" }
        }
    }

    LaunchedEffect(state.isGameEnded) {
        if (state.isGameEnded) {
            viewModel.endGame()
            navigateTo(Route.SkyjoEnd)
        }
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
                    val hasAtLeastOneRound = state.perPlayerRounds.values.any { it.isNotEmpty() }
                    IconButton(
                        onClick = { viewModel.undoLastRound() },
                        enabled = !state.isGameEnded && hasAtLeastOneRound
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Undo",
                            modifier = Modifier.size(24.dp)
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Column (
                modifier = modifier
                    .fillMaxWidth()
                    .requiredHeight(260.dp)
                    .verticalScroll(rememberScrollState())
            ){
                // Input row per player
                state.selectedPlayerIds.filterNotNull().forEachIndexed { index, playerId ->
                    val playerName = state.playerNames[playerId] ?: "Spieler auswählen"
                    val isDealer = playerId == state.currentDealerId
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        // Name
                        BetterOutlinedTextField(
                            value = playerName, //THIS
                            label = { Text("Spieler") },
                            modifier = Modifier.weight(1f),
                            textColor = if (isDealer) Color(0xFFF44336)
                            else MaterialTheme.colorScheme.onSurface,
                            textStyle = if (isDealer) MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp)
                            else LocalTextStyle.current,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        // Points input
                        BetterOutlinedTextField(
                            value = points[playerId] ?: "",
                            onValueChange = { new ->
                                if (new.isEmpty() || new == "-" || new.toIntOrNull() in -17..140) {
                                    points[playerId] = new
                                }
                            },
                            label = { Text("Punkte") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = if (index == state.selectedPlayerIds.filterNotNull().size - 1) ImeAction.Done
                                else ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardManager?.hide()
                                },
                                onNext = {
                                    focusManager.moveFocus(FocusDirection.Down)
                                }
                            ),
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        // Total display
                        BetterOutlinedTextField(
                            value = (totalPoints[playerId] ?: 0).toString(),
                            label = { Text("Gesamt") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Row {
                Button(
                    onClick = { viewModel.advanceDealer() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Dealer")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.endRound(points)
                        points.keys.toList().forEach { id -> points[id] = "" }
                        focusManager.moveFocus(FocusDirection.Down)
                        keyboardManager?.hide()
                    },
                    modifier = Modifier.fillMaxWidth().weight(2f),
                    enabled = allInputsFilled
                ) {
                    Text("Runde beenden")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // === ROUNDS GRID ===
            val scrollState = rememberScrollState()

            LaunchedEffect(visibleRoundRows) {
                scrollState.animateScrollTo(
                    scrollState.maxValue,
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = LinearEasing
                    )
                )
            }
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Spacer(modifier = Modifier.width(17.dp))

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

                    // Divider only between items
                    if (index < players.lastIndex) {
                        VerticalDivider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp),
                        )
                    }
                }
            }

            HorizontalDivider()
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                Row {
                    //Column with the index of every played round starting with 5
                    Column {
                        for (roundIndex in 1..visibleRoundRows)
                        // Round number cell
                            Box(
                                modifier = Modifier
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(roundIndex.toString())
                            }
                    }

                    Column {
                        // Round rows
                        for (roundIndex in 1..visibleRoundRows)
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                // SkyjoPlayer score cells
                                state.selectedPlayerIds.filterNotNull().forEach { playerId ->
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
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}