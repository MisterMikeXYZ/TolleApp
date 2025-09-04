package de.michael.tolleapp.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import de.michael.tolleapp.Route
import de.michael.tolleapp.presentation.settings.screens.PlayerDeleteScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen (
    viewModel: SettingsViewModel = koinViewModel(),
    navigateBack: () -> Unit,
    navigateTo : (Route) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
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
                .padding(16.dp)
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Darkmode", modifier = Modifier.weight(1f))
                Switch(
                    checked = state.isDarkmode,
                    onCheckedChange = { viewModel.toggleDarkMode() }
                )
            }

            HorizontalDivider()

            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Spieler löschen", modifier = Modifier.weight(1f))
            }
            Button(
                onClick = { navigateTo(Route.PlayerDeleteScreen) }
            ) {
                Text("Hallo")
            }
        }
    }
}