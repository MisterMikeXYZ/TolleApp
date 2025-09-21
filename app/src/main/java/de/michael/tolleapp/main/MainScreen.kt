package de.michael.tolleapp.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.Route
import org.koin.compose.viewmodel.koinViewModel

/**
 * MainScreen is the entry point of the app.
 * It displays a simple button to navigate to the [SkyjoScreen][de.michael.tolleapp.skyjo.presentation.SkyjoStartScreen].
 * The MainViewModel is injected using Koin. []
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    navigateTo : (Route) -> Unit,
) {
    BackHandler {  }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    "Main",
                    color = MaterialTheme.colorScheme.onSurface
                ) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                modifier = Modifier.clip(
                    shape = MaterialTheme.shapes.extraLarge.copy(
                        topStart = CornerSize(0.dp),
                        topEnd = CornerSize(0.dp),
                    )
                ),
                actions = {
                    IconButton(
                        onClick = { navigateTo(Route.Settings) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val modifier = Modifier
                .weight(1f)
                .aspectRatio(1.5f)
            Spacer(modifier = Modifier.height(8.dp))
            Row (modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { navigateTo(Route.Skyjo) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = modifier
                ) { Text("Skyjo") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { navigateTo(Route.DartStartScreen) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = modifier
                ) { Text("Dart") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row (modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { navigateTo(Route.Schwimmen) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = modifier
                ) { Text("Schwimmen") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { navigateTo(Route.RandomizerScreen) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = modifier
                ) { Text("Randomizer") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row (modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { navigateTo(Route.Statistics) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = modifier
                ) { Text("Statistik") }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("Durchschnitt✓\nGewinner✓\nEinstellungen✓\nDarkmode✓\nSpieler löschen✓\nDart\nSchwimmen✓\nVerbesserung/Verschlechterung\n")
        }
    }
}
