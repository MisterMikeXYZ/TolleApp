package de.michael.tolleapp.games.dart.presentation

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import de.michael.tolleapp.games.dart.data.DartGame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import org.koin.compose.viewmodel.koinViewModel
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DartStartScreen(
    viewModel: DartViewModel = koinViewModel(),
    navigateTo: (Route) -> Unit,
    navigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    val pausedGamesState = remember { mutableStateOf(emptyList<DartGame>()) }

    LaunchedEffect(viewModel) {
        viewModel.pausedGames
            .catch { emit(emptyList()) }
            .collect { pausedGamesState.value = it }
    }

    val pausedGames = pausedGamesState.value
    var expanded by remember { mutableStateOf(false) }
    val formatter = DateFormat.getDateTimeInstance(
        DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()
    )
    var showCreatePlayerDialog by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }
    var pendingRowIndex by remember { mutableStateOf<Int?>(null) }

    val presets by viewModel.presets.collectAsState(initial = emptyList())
    var presetExpanded by remember { mutableStateOf(false) }
    var showPresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    var gameChange by remember {mutableStateOf(false)}
    val startValue = if (gameChange) 501 else 301

    LaunchedEffect(Unit) {
        viewModel.resetGame()
    }

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
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newPlayerName.trim()
                        if (name.isNotEmpty() && pendingRowIndex != null) {
                            viewModel.addPlayer(name, pendingRowIndex!!)
                        }
                        newPlayerName = ""
                        showCreatePlayerDialog = false
                    }
                ) {
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
                    val name = newPresetName.trim()
                    if (name.isNotEmpty()) {
                        viewModel.createPreset(
                            "dart",
                            name,
                            state.selectedPlayerIds.filterNotNull()
                        )
                    }
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
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding( horizontal = 8.dp, vertical = 3.dp)
        ) {
            Row (modifier = Modifier.fillMaxWidth()) {
                //Paused games
                Box (modifier = Modifier
                    .weight(1.5f)
                    .fillMaxWidth()
                ) {
                    Button(
                        onClick = { expanded = true },
                    ) {
                        Text("Pausierte Spiele laden")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (pausedGames.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Keine pausierten Spiele") },
                                onClick = { expanded = false }
                            )
                        } else {
                            pausedGames.forEach { game: DartGame ->
                                val date = Date(
                                    if (game.createdAt < 10_000_000_000L) {
                                        game.createdAt * 1000
                                    } else {
                                        game.createdAt
                                    }
                                )
                                var resetPressedDelete by remember { mutableStateOf(false) }
                                LaunchedEffect(resetPressedDelete) {
                                    if (resetPressedDelete) {
                                        delay(2000)
                                        resetPressedDelete = false
                                    }
                                }

                                DropdownMenuItem(
                                    text = { Text("Spiel gestartet am ${formatter.format(date)}") },
                                    onClick = {
                                        viewModel.resumeGame(game.id)
                                        expanded = false
                                        navigateTo(Route.DartGameScreen)
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                if (!resetPressedDelete) resetPressedDelete = true
                                                else {
                                                    viewModel.deleteGame(game.id)
                                                    expanded = false
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
                        presets.forEach { presetWithPlayers ->
                            DropdownMenuItem(
                                text = { Text(presetWithPlayers.preset.name) },
                                onClick = {
                                    viewModel.resetSelectedPlayers()
                                    presetWithPlayers.players.forEachIndexed { index, presetPlayer ->
                                        viewModel.selectPlayer(index, presetPlayer.playerId)
                                    }
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
                                            viewModel.deletePreset(presetWithPlayers.preset.id)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    "301",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = gameChange,
                    onCheckedChange = { gameChange = !gameChange },
                    thumbContent = if (gameChange) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    "501",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            HorizontalDivider()

            Spacer(modifier = Modifier.height(8.dp))
            Text("Spieler:", style = MaterialTheme.typography.titleMedium)

            LazyColumn (
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ){
                itemsIndexed(state.selectedPlayerIds) { index, selectedId ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        var expanded by remember { mutableStateOf(false) }
                        val selectedPlayer = state.selectedPlayerIds[index]?.let { state.playerNames[it] } ?: "Spieler auswählen"

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = {
                                if (index == 0 || state.selectedPlayerIds[index - 1] != null) {
                                    expanded = !expanded
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            TextField(
                                value = selectedPlayer,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Spieler") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
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
                                state.playerNames.filter { (id, _) -> id !in state.selectedPlayerIds }
                                    .forEach { (id, name) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                viewModel.selectPlayer(index, id)
                                                expanded = false
                                            }
                                        )
                                    }

                            }
                        }

                        //X Button to remove player
                        if (index >= 1) {
                            IconButton(
                                onClick = { viewModel.removePlayer(index) },
                                enabled = index < state.selectedPlayerIds.size - 1
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Entfernen",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = if (index < state.selectedPlayerIds.size - 1) 1f else 0.3f
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            val distinctSelected =
                state.selectedPlayerIds.filterNotNull().distinct().isNotEmpty()

            //Button to start the game
            Button(
                onClick = {
                    viewModel.startGame(startValue)
                    navigateTo(Route.DartGameScreen)
                },
                enabled = distinctSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(50.dp)
            ) {
                Text("Spiel starten")
            }
        }
    }
}