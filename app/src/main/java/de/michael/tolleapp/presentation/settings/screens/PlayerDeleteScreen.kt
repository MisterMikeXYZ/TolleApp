package de.michael.tolleapp.presentation.settings.screens

import de.michael.tolleapp.presentation.settings.SettingsViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDeleteScreen (
    viewModel: SettingsViewModel = koinViewModel(),
    navigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Spieler löschen") },
                navigationIcon = {
                    IconButton(onClick = {
                        navigateBack()
                        val players = state.playersToDelete
                        players.forEach { player ->
                            viewModel.deselectPlayer(player)
                            }
                        }
                    ) {
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
                .padding(start = 16.dp, end = 16.dp)
        ) {
            Box (modifier = Modifier.weight(1f)) {
                Column {
                    if (state.players.isEmpty()) {
                        Text("Keine Spieler vorhanden")
                    } else {
                        Column(
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            val players = state.players
                            players.forEach { player ->

                                val isSelected = state.playersToDelete.contains(player)

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(
                                        text = player.name,
                                        fontSize = 24.sp,
                                        modifier = Modifier.weight(1f),
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = {
                                            if (isSelected) {
                                                viewModel.deselectPlayer(player)
                                            } else {
                                                viewModel.selectPlayer(player)
                                            }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Spieler löschen",
                                            tint = if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ){
                    Button(
                        onClick = { viewModel.deleteSelectedPlayers() },
                        modifier = Modifier.weight(1f),
                        enabled = !state.playersToDelete.isEmpty()
                    ) { Text("Löschen") }

                    Button(
                        onClick = {
                            val players = state.playersToDelete
                            players.forEach { player ->
                                viewModel.deselectPlayer(player) }
                            },
                        modifier = Modifier.weight(1f),
                        enabled = !state.players.isEmpty()
                    ) { Text("Abbrechen") }
                }
            }
        }
    }
}