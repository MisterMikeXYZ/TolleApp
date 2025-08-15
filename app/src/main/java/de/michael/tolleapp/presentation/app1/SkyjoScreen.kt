package de.michael.tolleapp.presentation.app1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import de.michael.tolleapp.Route
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.getValue

@Composable
fun SkyjoScreen(
    viewModel: SkyjoViewModel = koinViewModel(),
    navigateTo : (Route) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    Column(
        verticalArrangement = Arrangement.Center
    ) {
        Text("Malte ist ein geiler Hengst <3")
    }
}
