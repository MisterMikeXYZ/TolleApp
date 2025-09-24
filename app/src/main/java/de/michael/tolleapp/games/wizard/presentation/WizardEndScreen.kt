package de.michael.tolleapp.games.wizard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.michael.tolleapp.R
import de.michael.tolleapp.games.util.CustomTopBar

@Composable
fun WizardEndScreen(
    state: WizardState,
    navigateToMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Wizard",
            )
        }
    ) { pad ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            val scores = state.rounds.last().scores
            Column {
                state.selectedPlayers.sortedBy { scores[it?.id] }.reversed().forEachIndexed { index, player ->
                    Text(
                        text = "${index + 1}. ${player?.name} - ${scores[player?.id]}",
                        color = when(index + 1) {
                            1 -> MaterialTheme.colorScheme.primary
                            2 -> MaterialTheme.colorScheme.secondary
                            3 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            OutlinedButton(navigateToMainMenu) {
                Text(stringResource(R.string.main_menu))
            }
        }
    }
}