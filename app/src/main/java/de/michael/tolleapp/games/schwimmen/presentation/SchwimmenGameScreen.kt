package de.michael.tolleapp.games.schwimmen.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import de.michael.tolleapp.games.schwimmen.data.game.GameScreenType
import de.michael.tolleapp.games.util.CustomTopBar
import de.michael.tolleapp.games.util.OnHomeDialog
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.collections.isNotEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchwimmenGameScreen(
    viewModel: SchwimmenViewModel = koinViewModel(),
    gameScreenType: GameScreenType,
    navigateToMainMenu: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    var showOnHomeDialog by remember { mutableStateOf(false) }
    if (showOnHomeDialog) {
        OnHomeDialog(
            onSave = {
                viewModel.pauseCurrentGame()
                showOnHomeDialog = false
                navigateToMainMenu()
            },
            saveEnabled = state.perPlayerRounds.values.any { it < 4 } && !state.isGameEnded,
            onDiscard = {
                viewModel.deleteGame()
                showOnHomeDialog = false
                navigateToMainMenu()
            },
            onDismissRequest = {
                showOnHomeDialog = false
            }
        )
    }

    BackHandler {
        showOnHomeDialog = true
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Schwimmen",
                navigationIcon = {
                    if (!state.isGameEnded) {
                        IconButton(
                            onClick = { showOnHomeDialog = true },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                            )
                        }
                    }
                },
                actions = {
                    val hasAtLeastOneRound = state.perPlayerRounds.isNotEmpty()
                    val scope = rememberCoroutineScope()
                    IconButton(
                        onClick = {
                            scope.launch {
                                //viewModel.undoLastRound()
                            }
                        },
                        enabled = !state.isGameEnded && hasAtLeastOneRound
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint =  MaterialTheme.colorScheme.onSurface.copy(
                                alpha = if (!hasAtLeastOneRound) 0.3f else 1f
                            )
                        )
                    }
                }
                // TODO: Implement Undo button
            )
        },
    ) { innerPadding ->
        when (gameScreenType) {
            GameScreenType.CIRCLE -> {
                SchwimmenCircleContent(
                    viewModel = viewModel,
                    navigateToMainMenu = navigateToMainMenu,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            GameScreenType.CANVAS -> {
                SchwimmenCanvasContent(
                    viewModel = viewModel,
                    navigateToMainMenu = navigateToMainMenu,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}