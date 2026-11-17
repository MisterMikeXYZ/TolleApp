package de.michael.tolleapp.games.util.startScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.PausedGame
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    minPlayers: Int = 2,
    maxPlayers: Int = Int.MAX_VALUE,
    state: StartState,
    onAction: (StartAction) -> Unit,
    settingsItem: @Composable (() -> Unit)? = null,
    @SuppressLint("ModifierParameter")
    modifier: Modifier = Modifier,
) {
    val dateFormatter = DateFormat.getDateTimeInstance(
        DateFormat.SHORT, DateFormat.SHORT, LocalLocale.current.platformLocale
    )

    var showCreatePlayerDialog by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }
    var pendingRowIndex by remember { mutableStateOf<Int?>(null) }
    var focusIndex by remember { mutableStateOf<Int?>(null) }
    if (showCreatePlayerDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlayerDialog = false },
            title = { Text("Neuen Spieler erstellen") },
            text = {
                TextField(
                    value = newPlayerName,
                    onValueChange = { newPlayerName = it },
                    label = { Text("Spielername") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions {
                        if (newPlayerName.isNotEmpty() && pendingRowIndex != null)
                            onAction(StartAction.CreatePlayer(newPlayerName.trim(), pendingRowIndex!!))
                        newPlayerName = ""
                        showCreatePlayerDialog = false
                    }
                )
            },
            confirmButton = {
                TextButton({
                        if (newPlayerName.isNotEmpty() && pendingRowIndex != null)
                            onAction(StartAction.CreatePlayer(newPlayerName.trim(), pendingRowIndex!!))
                        newPlayerName = ""
                        showCreatePlayerDialog = false
                }) {
                    Text("Erstellen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newPlayerName = ""
                        showCreatePlayerDialog = false
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    var presetExpanded by remember { mutableStateOf(false) }
    var showPresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = { Text("Neues Preset erstellen") },
            text = {
                TextField(
                    value = newPresetName,
                    onValueChange = { newPresetName = it },
                    label = { Text("Preset Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPresetName.isNotEmpty())
                        onAction(StartAction.CreatePreset(newPresetName.trim(), state.selectedPlayers.filterNotNull().map { it.id }))
                    newPresetName = ""
                    showPresetDialog = false
                }) {
                    Text("Erstellen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CustomTopBar(
                title = state.gameType.toString(),
                navigationIcon = {
                    IconButton({ onAction(StartAction.NavigateToMainMenu) }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Back"
                        )
                    }
                },
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures { focusManager.clearFocus() }
                    }
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = modifier
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures { focusManager.clearFocus() }
                }
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Row (modifier = Modifier.fillMaxWidth()) {
                //Paused games
                var pausedGamesExpanded by remember { mutableStateOf(false)}
                Box (modifier = Modifier
                    .weight(1.5f)
                    .fillMaxWidth()
                ) {
                    Button(
                        onClick = { pausedGamesExpanded = true },
                    ) {
                        Text("Pausierte Spiele laden")
                    }
                    DropdownMenu(
                        expanded = pausedGamesExpanded,
                        onDismissRequest = { pausedGamesExpanded = false }
                    ) {
                        if (state.pausedGames.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Keine pausierten Spiele") },
                                onClick = { pausedGamesExpanded = false }
                            )
                        } else {
                            var deleteAllPressed by remember { mutableStateOf(false) }
                            LaunchedEffect(deleteAllPressed) {
                                if (deleteAllPressed) {
                                    delay(2.seconds)
                                    deleteAllPressed = false
                                }
                            }
                            DropdownMenuItem(
                                text = { if(!deleteAllPressed) Text("Alle löschen") else Text("Sicher?") },
                                onClick = { if(!deleteAllPressed) deleteAllPressed = true else state.pausedGames.forEach{ onAction(StartAction.DeleteGame(it.id)) } }
                            )

                            state.pausedGames.forEach { pausedGame: PausedGame ->
                                var resetPressedDelete by remember { mutableStateOf(false) }
                                LaunchedEffect(resetPressedDelete) {
                                    if (resetPressedDelete) {
                                        delay(2.seconds)
                                        resetPressedDelete = false
                                    }
                                }

                                DropdownMenuItem(
                                    text = { Text("Spiel gestartet am ${dateFormatter.format(Date(pausedGame.createdAt))}") },
                                    onClick = {
                                        onAction(StartAction.ResumeGame(pausedGame.id))
                                        pausedGamesExpanded = false
                                        onAction(StartAction.NavigateToGame)
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                if (!resetPressedDelete) resetPressedDelete = true
                                                else {
                                                    onAction(StartAction.DeleteGame(pausedGame.id))
                                                    pausedGamesExpanded = false
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
                                    }
                                )
                            }
                        }
                    }
                }

                //Presets
                Box (modifier = Modifier
                    .weight(1.1f)
                    .fillMaxWidth()
                ) {
                    Button(
                        onClick = { presetExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Presets")
                    }
                    DropdownMenu(
                        expanded = presetExpanded,
                        onDismissRequest = { presetExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Neues Preset erstellen") },
                            onClick = {
                                presetExpanded = false
                                showPresetDialog = true
                            },
                        )
                        state.presets.forEach { presetWithPlayers ->
                            DropdownMenuItem(
                                text = { Text(presetWithPlayers.preset.name) },
                                enabled = presetWithPlayers.players.size <= maxPlayers,
                                onClick = {
                                    onAction(StartAction.SelectPreset(presetWithPlayers.preset.id))
                                    presetExpanded = false
                                },
                                trailingIcon = {
                                    var resetPressedDelete by remember { mutableStateOf(false) }
                                    LaunchedEffect(resetPressedDelete) {
                                        if (resetPressedDelete) {
                                            delay(2.seconds)
                                            resetPressedDelete = false
                                        }
                                    }
                                    IconButton(onClick = {
                                        if (!resetPressedDelete) resetPressedDelete = true
                                        else {
                                            onAction(StartAction.DeletePreset(presetWithPlayers.preset.id))
                                            presetExpanded = false
                                            resetPressedDelete = false
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (!resetPressedDelete) Icons.Default.Delete
                                            else Icons.Default.DeleteForever,
                                            contentDescription = null,
                                            tint = if (!resetPressedDelete) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            settingsItem?.invoke()

            Spacer(modifier = Modifier.height(3.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ){
                itemsIndexed(state.selectedPlayers) { index, player ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                        //.verticalScroll(rememberScrollState())
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        val focusRequester = remember { FocusRequester() }
                        val canOpen = index == 0 || state.selectedPlayers[index - 1] != null
                        LaunchedEffect(focusIndex, canOpen) {
                            if (focusIndex == index && canOpen) {
                                // wait for the previous dropdown popup to close before moving focus
                                delay(150.milliseconds)
                                focusRequester.requestFocus()
                                expanded = true
                                focusIndex = null
                            }
                        }
                        var searchQuery by remember { mutableStateOf("") }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = {
                                if (index == 0 || state.selectedPlayers[index - 1] != null) {
                                    expanded = !expanded
                                    searchQuery = ""
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            TextField(
                                value = if (expanded) searchQuery else player?.name ?: "Spieler auswählen",
                                onValueChange = {
                                    searchQuery = it
                                    expanded = true
                                },
                                enabled = canOpen || player != null,
                                singleLine = true,
                                label = { Text("Spieler") },
                                placeholder = { Text("Suchen…") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                                    .focusRequester(focusRequester)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = {
                                    expanded = false
                                    searchQuery = ""
                                    focusManager.clearFocus()
                                }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Neuen Spieler erstellen…") },
                                    onClick = {
                                        expanded = false
                                        searchQuery = ""
                                        pendingRowIndex = index
                                        showCreatePlayerDialog = true
                                    }
                                )
                                state.allPlayers
                                    .sortedBy { it.name }
                                    .filter { it !in state.selectedPlayers }
                                    .filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
                                    .forEach { (id, name) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                onAction(StartAction.SelectPlayer(index, id))
                                                if (index + 1 < maxPlayers && state.selectedPlayers.getOrNull(index + 1) == null) focusIndex = index + 1
                                                expanded = false
                                                searchQuery = ""
                                            }
                                        )
                                    }

                            }
                        }

                        //X Button to remove player
                        if (index >= minPlayers) {
                            IconButton(
                                onClick = { onAction(StartAction.UnselectPlayer(index)) },
                                enabled = state.selectedPlayers[index] != null,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Entfernen",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = if (state.selectedPlayers[index] != null) 1f else 0.3f
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            //Button to start the game
            Button(
                onClick = {
                    onAction(StartAction.StartGame)
                    onAction(StartAction.NavigateToGame)
                },
                enabled = state.selectedPlayers.filterNotNull().size >= minPlayers,
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(50.dp)
            ) {
                Text("Spiel starten")
            }
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}