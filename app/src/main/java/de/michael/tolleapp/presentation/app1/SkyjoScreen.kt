package de.michael.tolleapp.presentation.app1

import android.R.attr.onClick
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.michael.tolleapp.Route
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkyjoScreen(
    modifier: Modifier = Modifier,
    viewModel: SkyjoViewModel = koinViewModel(),
    navigateTo: (Route) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf("") }
    var pendingRowIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Spieler:", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        state.selectedPlayerIds.forEachIndexed { index, selectedId ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                var expanded by remember { mutableStateOf(false) }
                val selectedPlayer =
                    state.players.firstOrNull { it.id == selectedId }?.name ?: "Spieler auswählen"

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
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
                                showDialog = true
                            }
                        )
                        state.players.forEach { player ->
                            DropdownMenuItem(
                                text = { Text(player.name) },
                                onClick = {
                                    viewModel.selectPlayer(index, player.id)
                                    expanded = false
                                }
                            )
                        }

                    }
                }

                //X Button to remove player
                if (index >= 2) {
                    IconButton(
                        onClick = { viewModel.removePlayer(index) }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Entfernen")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        val distinctSelected =
            state.selectedPlayerIds.filterNotNull().distinct().size >= 2

        //Button to start the game
        Button(
            onClick = {
                viewModel.startGame()
                navigateTo(Route.SkyjoGame)
                      },
            enabled = distinctSelected,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Spiel starten")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
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
                        showDialog = false
                    }
                ) {
                    Text("Erstellen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        newPlayerName = ""
                        showDialog = false
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}