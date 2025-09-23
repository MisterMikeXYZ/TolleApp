package de.michael.tolleapp.games.skyjo.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardHide
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
import androidx.compose.runtime.rememberCoroutineScope
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
import de.michael.tolleapp.games.skyjo.presentation.components.SkyjoKeyboard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    val points = remember { mutableStateMapOf<String, String>() }
    val perPlayerRounds = state.perPlayerRounds
    val totalPoints = state.totalPoints
    val visibleRoundRows = state.visibleRoundRows

    val allInputsFilled by remember {
        derivedStateOf {
            state.selectedPlayerIds.filterNotNull().all { id -> !points[id].isNullOrEmpty() }
        }
    }

    val neleModus = state.neleModus
    var keyboardExpanded by remember { mutableStateOf(false) }
    var activePlayerId by remember { mutableStateOf<String?>(null) }

    var showPlayerInputs by remember { mutableStateOf(true) }
    var showRoundsGrid by remember { mutableStateOf(true) }

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
            navigateTo(Route.SkyjoEnd)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Skyjo", color = MaterialTheme.colorScheme.onSurface) },
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
                    val scope = rememberCoroutineScope()
                    IconButton(
                        onClick = {
                            scope.launch {
                                val undone = viewModel.undoLastRound()
                                if (undone) navigateTo(Route.SkyjoGame)
                            }
                        },
                        enabled = !state.isGameEnded && hasAtLeastOneRound
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Undo",
                            tint = if (!hasAtLeastOneRound) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.onSurface
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
                            tint = if (!hasAtLeastOneRound) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            else if (!resetPressedSave) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // === SCROLLABLE GAME CONTENT ===
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPlayerInputs = !showPlayerInputs }
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Spieler",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (showPlayerInputs) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Spieler Inputs"
                    )
                }
                // --- Player Inputs ---
                if (showPlayerInputs) {
                    val playerScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(playerScrollState)
                    ) {
                        state.selectedPlayerIds.filterNotNull().forEach { playerId ->
                            val playerName = state.playerNames[playerId] ?: "Spieler auswählen"
                            val isDealer = playerId == state.currentDealerId

                            val isActivePlayer = playerId == activePlayerId
                            val backgroundColor =
                                if (isActivePlayer && neleModus) MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.2f
                                )
                                else Color.Transparent

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(backgroundColor)
                                    .clickable(enabled = neleModus) {
                                        activePlayerId = playerId
                                        keyboardExpanded = true
                                    }
                                    .padding(4.dp) // inner padding
                            ) {
                                BetterOutlinedTextField(
                                    value = playerName,
                                    label = { Text("Spieler") },
                                    modifier = Modifier.weight(1f),
                                    textColor = if (isDealer) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                                    textStyle = if (isDealer) MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 20.sp
                                    )
                                    else LocalTextStyle.current,
                                    readOnly = false
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                BetterOutlinedTextField(
                                    value = points[playerId] ?: "",
                                    onValueChange = { new ->
                                        if (!neleModus) {
                                            if (new.isEmpty() || new == "-" || new.toIntOrNull() in -17..140) {
                                                points[playerId] = new
                                            }
                                        }
                                    },
                                    readOnly = neleModus,
                                    label = { Text("Punkte") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = neleModus) {
                                            activePlayerId = playerId
                                            keyboardExpanded = true
                                        },
                                    keyboardOptions = if (!neleModus) KeyboardOptions(
                                        keyboardType = KeyboardType.Phone,
                                        imeAction = ImeAction.Next,
                                        showKeyboardOnFocus = true
                                    ) else KeyboardOptions.Default,
                                    keyboardActions = if (!neleModus) KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                        onDone = { keyboardManager?.hide() }
                                    ) else KeyboardActions.Default
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                BetterOutlinedTextField(
                                    value = (totalPoints[playerId] ?: 0).toString(),
                                    label = { Text("Gesamt") },
                                    modifier = Modifier.weight(1f),
                                    readOnly = true
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row {
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
                            modifier = Modifier.fillMaxWidth().weight(2f),
                            enabled = allInputsFilled
                        ) { Text("Runde beenden") }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // === ROUNDS GRID ===
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRoundsGrid = !showRoundsGrid }
                        .clip(RoundedCornerShape(12.dp)) // rounded corners
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        "Runden",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (showRoundsGrid) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Runden Grid"
                    )
                }
                Column(
                    modifier = Modifier
                        //.fillMaxWidth()
                        .weight(1f) // take the remaining space
                        .verticalScroll(rememberScrollState())
                ) {
                    if (showRoundsGrid) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(if (!showRoundsGrid) 0.1f else 0.4f)
                                .fillMaxHeight(1f)
                        ) {
                            Spacer(modifier = Modifier.width(20.dp))

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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                Column {
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
                                                            .width(1.dp),
                                                    )
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

            if (neleModus && activePlayerId != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                ) {
                    if (keyboardExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                                .padding(8.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            SkyjoKeyboard(
                                onSubmit = { total ->
                                    points[activePlayerId!!] = total
                                    activePlayerId = null
                                    keyboardExpanded = false
                                },
                                onBack = {
                                    activePlayerId = null
                                    keyboardExpanded = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            keyboardExpanded = !keyboardExpanded
                            if (!keyboardExpanded) {
                                activePlayerId = null
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = if (keyboardExpanded) Icons.Default.KeyboardHide
                            else Icons.Default.Keyboard,
                            contentDescription = "Toggle keyboard"
                        )
                    }
                }
            }
        }
    }
}
