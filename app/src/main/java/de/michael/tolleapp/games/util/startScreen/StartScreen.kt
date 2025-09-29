package de.michael.tolleapp.games.util.startScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.PausedGame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.Locale

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
        DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()
    )

    var showCreatePlayerDialog by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }
    var pendingRowIndex by remember { mutableStateOf<Int?>(null) }
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
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = modifier
                .padding(innerPadding)
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
                                    delay(2000)
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
                                        delay(2000)
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
                                onClick = {
                                    onAction(StartAction.SelectPreset(presetWithPlayers.preset.id))
                                    presetExpanded = false
                                },
                                trailingIcon = {
                                    var resetPressedDelete by remember { mutableStateOf(false) }
                                    LaunchedEffect(resetPressedDelete) {
                                        if (resetPressedDelete) {
                                            delay(2000)
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

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = {
                                if (index == 0 || state.selectedPlayers[index - 1] != null) expanded = !expanded
                            },
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            TextField(
                                value = player?.name ?: "Spieler auswählen",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Spieler") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryEditable)
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Neuen Spieler erstellen…") },
                                    onClick = {
                                        expanded = false
                                        pendingRowIndex = index
                                        showCreatePlayerDialog = true
                                    }
                                )
                                state.allPlayers.sortedBy{ it.name }.filter { it !in state.selectedPlayers }
                                    .forEach { (id, name) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                onAction(StartAction.SelectPlayer(index, id))
                                                expanded = false
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