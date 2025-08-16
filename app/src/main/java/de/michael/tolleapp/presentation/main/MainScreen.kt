package de.michael.tolleapp.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import de.michael.tolleapp.Route
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue

/**
 * MainScreen is the entry point of the app.
 * It displays a simple button to navigate to the [SkyjoScreen][de.michael.tolleapp.presentation.app1.SkyjoScreen].
 * The MainViewModel is injected using Koin. []
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = koinViewModel(),
    navigateTo : (Route) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    Column(
        verticalArrangement = Arrangement.Center,

        modifier = Modifier.fillMaxSize()
    ) {
        Text("main")
        Button(
            onClick = { navigateTo(Route.Skyjo) },
        ) { Text("Go to Skyjo") }
    }

}
