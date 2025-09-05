package de.michael.tolleapp.presentation.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import de.michael.tolleapp.Route
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

/**
 * MainScreen is the entry point of the app.
 * It displays a simple button to navigate to the [SkyjoScreen][de.michael.tolleapp.presentation.skyjo.SkyjoStartScreen].
 * The MainViewModel is injected using Koin. []
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    navigateTo : (Route) -> Unit,
) {
    val modifier = Modifier.width(200.dp)

    BackHandler {  }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("main")
        Button(
            onClick = { navigateTo(Route.Skyjo) },
            modifier = modifier
        ) { Text("Go to Skyjo") }
        Button(
            onClick = { navigateTo(Route.Schwimmen) },
            modifier = modifier
        ) { Text("Go to Schwimmen") }
        Button(
            onClick = { navigateTo(Route.Statistics) },
            modifier = modifier
        ) { Text("Statistik") }
        Button(
            onClick = { navigateTo(Route.Settings) },
            modifier = modifier
        ) { Text("Settings")}
        Spacer (modifier = Modifier.height(20.dp))
        Text("Durchschnitt✓\nGewinner✓\nEinstellungen\nDarkmode\nSpieler löschen\nDart\nSchwimmen\nVerbesserung/Verschlechterung\n" )
    }
}
