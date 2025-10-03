package de.michael.tolleapp

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import de.michael.tolleapp.games.dart.presentation.DartGameScreen
import de.michael.tolleapp.games.dart.presentation.DartStartScreen
import de.michael.tolleapp.games.dart.presentation.DartViewModel
import de.michael.tolleapp.games.flip7.presentation.Flip7Action
import de.michael.tolleapp.games.flip7.presentation.Flip7GameScreen
import de.michael.tolleapp.games.flip7.presentation.Flip7ViewModel
import de.michael.tolleapp.games.flip7.presentation.toStartState
import de.michael.tolleapp.games.randomizer.presentation.RandomizerScreen
import de.michael.tolleapp.games.randomizer.presentation.RandomizerViewModel
import de.michael.tolleapp.games.romme.presentation.RommeAction
import de.michael.tolleapp.games.romme.presentation.RommeGameScreen
import de.michael.tolleapp.games.romme.presentation.RommeViewModel
import de.michael.tolleapp.games.schwimmen.data.game.GameScreenType
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenGameScreen
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenStartScreen
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenViewModel
import de.michael.tolleapp.games.skyjo.presentation.SkyjoAction
import de.michael.tolleapp.games.skyjo.presentation.SkyjoGameScreen
import de.michael.tolleapp.games.skyjo.presentation.SkyjoViewModel
import de.michael.tolleapp.games.skyjo.presentation.toStartState
import de.michael.tolleapp.games.util.endScreen.EndScreen
import de.michael.tolleapp.games.util.startScreen.StartAction
import de.michael.tolleapp.games.util.startScreen.StartScreen
import de.michael.tolleapp.games.util.startScreen.StartState
import de.michael.tolleapp.games.wizard.presentation.WizardAction
import de.michael.tolleapp.games.wizard.presentation.WizardGameScreen
import de.michael.tolleapp.games.wizard.presentation.WizardViewModel
import de.michael.tolleapp.games.wizard.presentation.toStartState
import de.michael.tolleapp.main.MainScreen
import de.michael.tolleapp.settings.presentation.SettingsScreen
import de.michael.tolleapp.settings.presentation.SettingsViewModel
import de.michael.tolleapp.settings.presentation.screens.PlayerDeleteScreen
import de.michael.tolleapp.statistics.StatScreen
import de.michael.tolleapp.statistics.StatViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
) {
    val statViewModel = koinViewModel<StatViewModel>()
    val settingsViewModel = koinViewModel<SettingsViewModel>()

    NavHost(
        navController = navController,
        startDestination = Route.BeforeNav,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(400)
            )
        },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(400)
            )
        },
    ) {
        navigation<Route.BeforeNav>(
            startDestination = Route.Before.StartScreen,
        ) {
            // --- Start aka Main ---
            composable<Route.Before.StartScreen> {
                MainScreen(
                    navigateTo = { route ->
                        navController.navigate(route)
                    }
                )
            }

            // --- Statistics ---
            composable<Route.Before.Statistics> {
                StatScreen(
                    navigateBack = { navController.popBackStack() },
                    viewModel = statViewModel
                )
            }

            // --- Settings ---
            composable<Route.Before.Settings>(
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(400)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(400)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(400)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(400)
                    )
                },
            ) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    navigateBack = { navController.popBackStack() },
                    navigateTo = { route ->
                        navController.navigate(route)
                    },
                )
            }

            composable<Route.Before.PlayerDeleteScreen>(
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(400)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(400)
                    )
                },
            ) {
                PlayerDeleteScreen(
                    viewModel = settingsViewModel,
                    navigateBack = { navController.popBackStack() },
                )
            }
        }

        navigation<Route.SkyjoNav>(
            startDestination = Route.Skyjo.Start
        ) {
            composableStartScreen<Route.Skyjo.Start> {
                val viewModel = it.sharedViewModel<SkyjoViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                StartScreen(
                    minPlayers = 2,
                    maxPlayers = 8,
                    state = state.toStartState(),
                    onAction = { action ->
                        when (action) {
                            StartAction.NavigateToMainMenu -> navController.popToRoute(Route.Before.StartScreen)
                            StartAction.NavigateToGame -> navController.navigate(Route.Skyjo.Game)
                            else -> viewModel.onStartAction(action)
                        }
                    }
                )
            }
            composableGameScreen<Route.Skyjo.Game> {
                val viewModel = it.sharedViewModel<SkyjoViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                SkyjoGameScreen(
                    onAction = { action ->
                        when (action) {
                            SkyjoAction.NavigateToMainMenu -> navController.popToRoute(Route.Before.StartScreen)
                            SkyjoAction.EndGame -> navController.navigate(Route.Skyjo.End)
                            else -> viewModel.onAction(action)
                        }
                    },
                    state = state,
                )
            }
            composableGameScreen<Route.Skyjo.End> {
                val viewModel = it.sharedViewModel<SkyjoViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()

                val sortedPlayers = state.selectedPlayers.filterNotNull().sortedBy { player ->
                    state.rounds.last().scores[player.id]
                }
                EndScreen(
                    titleValue = "Skyjo",
                    sortedPlayerNames = sortedPlayers.map { it.name },
                    sortedScoreValues = state.rounds
                        .map { roundData ->
                            sortedPlayers.map { "${roundData.scores[it.id]}" }
                        },
                    sortedTotalValues = sortedPlayers.map { state.totalPoints[it.id].toString() },
                    navigateToMainMenu = { navController.popToRoute(Route.Before.StartScreen) },
                )
            }
        }

        navigation<Route.SchwimmenNav>(
            startDestination = Route.Schwimmen.Start
        ) {
            composableStartScreen<Route.Schwimmen.Start> {
                val viewModel = it.sharedViewModel<SchwimmenViewModel>(navController)
                SchwimmenStartScreen(
                    navigateToGame = { canvas -> navController.navigate(Route.Schwimmen.Game(canvas)) },
                    navigateBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
            composableGameScreen<Route.Schwimmen.Game> {
                val canvas = it.toRoute<Route.Schwimmen.Game>().canvas
                val viewModel = it.sharedViewModel<SchwimmenViewModel>(navController)

                SchwimmenGameScreen(
                    gameScreenType = if (canvas) GameScreenType.CANVAS else GameScreenType.CIRCLE,
                    viewModel = viewModel,
                    navigateToMainMenu = { navController.popToRoute(Route.Before.StartScreen) },
                )
            }
        }

        navigation<Route.DartNav>(
            startDestination = Route.Dart.Start
        ) {
            composableStartScreen<Route.Dart.Start> {
                val viewModel = it.sharedViewModel<DartViewModel>(navController)
                DartStartScreen(
                    viewModel = viewModel,
                    navigateToGameScreen = { navController.navigate(Route.Dart.Game) },
                    navigateToMainMenu = { navController.popToRoute(Route.Before.StartScreen) },
                )
            }
            composableGameScreen<Route.Dart.Game> {
                val viewModel = it.sharedViewModel<DartViewModel>(navController)
                DartGameScreen(
                    viewModel = viewModel,
                    navigateToMainMenu = { navController.popToRoute(Route.Before.StartScreen) }
                )
            }
        }

        navigation<Route.RandomizerNav>(
            startDestination = Route.Randomizer.Start,
        ) {
            composableStartScreen<Route.Randomizer.Start> {
                val viewModel = it.sharedViewModel<RandomizerViewModel>(navController)
                RandomizerScreen(
                    viewModel = viewModel,
                    navigateToMainMenu = { navController.popToRoute(Route.Before.StartScreen) }
                )
            }
        }

        navigation<Route.WizardNav>(
            startDestination = Route.Wizard.Start
        ) {
            composableStartScreen<Route.Wizard.Start> {
                val viewModel = it.sharedViewModel<WizardViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                StartScreen(
                    minPlayers = 3,
                    maxPlayers = 6,
                    state = state.toStartState(),
                    onAction = { action ->
                        when (action) {
                            StartAction.NavigateToMainMenu -> navController.popToRoute(Route.Before.StartScreen)
                            StartAction.NavigateToGame -> navController.navigate(Route.Wizard.Game)
                            else -> viewModel.onStartAction(action)
                        }
                    }
                )
            }
            composableGameScreen<Route.Wizard.Game> {
                val viewModel = it.sharedViewModel<WizardViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                WizardGameScreen(
                    state = state,
                    onAction = { action ->
                        when (action) {
                            WizardAction.NavigateToMainMenu -> navController.popToRoute(Route.Before.StartScreen)
                            WizardAction.OnGameFinished -> {
                                navController.navigate(Route.Wizard.End)
                            }
                            else -> viewModel.onAction(action)
                        }
                    }
                )
            }
            composableGameScreen<Route.Wizard.End> { backStackEntry ->
                val viewModel = backStackEntry.sharedViewModel<WizardViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()

                val sortedPlayers = state.selectedPlayers.filterNotNull().sortedBy {
                    state.rounds.last().scores[it.id]
                }
                EndScreen(
                    titleValue = "Wizard",
                    sortedPlayerNames = sortedPlayers.map { it.name },
                    sortedScoreValues = state.rounds
                        .map { roundData ->
                            sortedPlayers.map { "${roundData.scores[it.id]} | ${roundData.bids[it.id]}" }
                        },
                    sortedTotalValues = sortedPlayers.map { state.rounds.last().scores[it.id].toString() },
                    navigateToMainMenu = { navController.popToRoute(Route.Before.StartScreen) },
                )
            }
        }
        navigation<Route.RommeNav>(
            startDestination = Route.Romme.Start
        ) {
            composableStartScreen<Route.Romme.Start> {
                val viewModel = it.sharedViewModel<RommeViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                StartScreen(
                    minPlayers = 2,
                    state = state as StartState,
                    onAction = { action ->
                        when (action) {
                            StartAction.NavigateToMainMenu -> navController.popToRoute(Route.Before.StartScreen)
                            StartAction.NavigateToGame -> navController.navigate(Route.Romme.Game)
                            else -> viewModel.onStartAction(action)
                        }
                    }
                )
            }
            composableGameScreen<Route.Romme.Game> {
                val viewModel = it.sharedViewModel<RommeViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                RommeGameScreen(
                    state = state,
                    onAction = { action ->
                        when (action) {
                            RommeAction.NavigateToMainMenu -> navController.popToRoute(Route.Before.StartScreen)
                            RommeAction.OnGameFinished -> {
                                navController.navigate(Route.Romme.End)
                                viewModel.onAction(action)
                            }
                            else -> viewModel.onAction(action)
                        }
                    }
                )
            }
            composableGameScreen<Route.Romme.End> { backStackEntry ->
                val viewModel = backStackEntry.sharedViewModel<RommeViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()

                val sortedPlayers = state.selectedPlayers.filterNotNull().sortedBy {
                    state.rounds.last().finalScores[it.id]
                }
                EndScreen(
                    titleValue = "Rommé",
                    sortedPlayerNames = sortedPlayers.map { it.name },
                    sortedScoreValues = state.rounds
                        .let { rounds -> if (rounds.last().roundScores.values.any { it == null }) rounds.dropLast(1) else rounds }
                        .map { roundData ->
                            sortedPlayers.map { roundData.roundScores[it.id]?.toString() ?: "" }
                        },
                    sortedTotalValues = sortedPlayers.map { state.rounds.last().finalScores[it.id].toString() },
                    navigateToMainMenu = { navController.popToRoute(Route.Before.StartScreen) },
                )
            }
        }

        navigation<Route.Flip7Nav>(
            startDestination = Route.Flip7.Start
        ) {
            composableStartScreen<Route.Flip7.Start>{
                val viewModel = it.sharedViewModel<Flip7ViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                StartScreen(
                    minPlayers = 2,
                    state = state.toStartState(),
                    onAction = { action ->
                        when (action) {
                            StartAction.NavigateToMainMenu -> navController.popToRoute(Route.Before.StartScreen)
                            StartAction.NavigateToGame -> navController.navigate(Route.Flip7.Game)
                            else -> viewModel.onStartAction(action)
                        }
                    }
                )
            }
            composableGameScreen<Route.Flip7.Game> {
                val viewModel = it.sharedViewModel<Flip7ViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                Flip7GameScreen(
                    state = state,
                    onAction = { action ->
                        when (action) {
                            Flip7Action.NavigateToMainMenu -> navController.popToRoute(Route.Before.StartScreen)
                            Flip7Action.EndGame -> {
                                navController.navigate(Route.Flip7.End)
                                viewModel.onAction(action)
                            }
                            else -> viewModel.onAction(action)
                        }
                    }
                )
            }
            composableGameScreen <Route.Flip7.End> {
                val viewModel = it.sharedViewModel<Flip7ViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()

                val sortedPlayers = state.selectedPlayers.filterNotNull().sortedBy { player ->
                    state.rounds.last().scores[player.id]
                }
                EndScreen(
                    titleValue = "Flip7",
                    sortedPlayerNames = sortedPlayers.map { it.name },
                    sortedScoreValues = state.rounds
                        .map { roundData ->
                            sortedPlayers.map { "${roundData.scores[it.id]}" }
                        },
                    sortedTotalValues = sortedPlayers.map { state.totalPoints[it.id].toString() },
                    navigateToMainMenu = { navController.popToRoute(Route.Before.StartScreen) },
                )
            }
        }
    }
}

inline fun <reified T: Route> NavController.navigateWithPop(route: Route) {
    this.navigate(route) {
        popUpTo(T::class) {
            inclusive = true
        }
    }
}

private inline fun <reified T : Any> NavGraphBuilder.composableStartScreen(
    noinline content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) = composable <T>(
    content = content,
    enterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Up,
            animationSpec = tween(400)
        )
    },
    exitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(400)
        )
    },
    popExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = tween(400)
        )
    },
)

private inline fun <reified T : Any> NavGraphBuilder.composableGameScreen(
    noinline content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) = composable <T>(
    content = content,
    enterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(400)
        )
    },
    exitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(400)
        )
    },
    popExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Down,
            animationSpec = tween(400)
        )
    },
)

fun NavController.popToRoute(route: Route) {
    while (currentBackStackEntry?.destination?.route != route::class.qualifiedName) {
        if (!popBackStack()) break
    }
}

@Composable
inline fun <reified T: ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(viewModelStoreOwner = parentEntry)
}