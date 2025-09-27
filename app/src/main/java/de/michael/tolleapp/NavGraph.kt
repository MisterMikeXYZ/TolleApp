import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import de.michael.tolleapp.Route
import de.michael.tolleapp.games.dart.presentation.DartGameScreen
import de.michael.tolleapp.games.dart.presentation.DartStartScreen
import de.michael.tolleapp.games.dart.presentation.DartViewModel
import de.michael.tolleapp.games.randomizer.presentation.RandomizerScreen
import de.michael.tolleapp.games.randomizer.presentation.RandomizerViewModel
import de.michael.tolleapp.games.schwimmen.data.game.GameScreenType
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenGameScreen
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenStartScreen
import de.michael.tolleapp.games.schwimmen.presentation.SchwimmenViewModel
import de.michael.tolleapp.games.skyjo.presentation.SkyjoEndScreen
import de.michael.tolleapp.games.skyjo.presentation.SkyjoGameScreen
import de.michael.tolleapp.games.skyjo.presentation.SkyjoStartScreen
import de.michael.tolleapp.games.skyjo.presentation.SkyjoViewModel
import de.michael.tolleapp.games.util.startScreen.StartAction
import de.michael.tolleapp.games.util.startScreen.StartGameScreen
import de.michael.tolleapp.games.wizard.presentation.WizardAction
import de.michael.tolleapp.games.wizard.presentation.WizardEndScreen
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
            composable <Route.Before.Settings> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    navigateBack = { navController.popBackStack() },
                    navigateTo = { route ->
                        navController.navigate(route)
                    },
                )
            }

            composable <Route.Before.PlayerDeleteScreen> {
                PlayerDeleteScreen(
                    viewModel = settingsViewModel,
                    navigateBack = { navController.popBackStack() },
                )
            }
        }
        navigation<Route.SkyjoNav>(
            startDestination = Route.Skyjo.Start
        ) {
            composable<Route.Skyjo.Start> {
                val viewModel = it.sharedViewModel<SkyjoViewModel>(navController)
                SkyjoStartScreen(
                    navigateToGame = { navController.navigate(Route.Skyjo.Game) },
                    navigateBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
            composable<Route.Skyjo.Game> {
                val viewModel = it.sharedViewModel<SkyjoViewModel>(navController)
                SkyjoGameScreen(
                    viewModel = viewModel,
                    navigateToMainMenu = { navController.popToRoute(Route.Before.StartScreen) },
                    navigateToEnd = { navController.navigate(Route.Skyjo.End) },
                )
            }
            composable<Route.Skyjo.End> {
                val viewModel = it.sharedViewModel<SkyjoViewModel>(navController)
                SkyjoEndScreen(
                    navigateToGameScreen = { navController.popToRoute(Route.Skyjo.Game) },
                    navigateToMainMenu = { navController.popToRoute(Route.Before.StartScreen) },
                    viewModel = viewModel,
                )
            }
        }
        navigation<Route.SchwimmenNav>(
            startDestination = Route.Schwimmen.Start
        ) {
            composable<Route.Schwimmen.Start> {
                val viewModel = it.sharedViewModel<SchwimmenViewModel>(navController)
                SchwimmenStartScreen(
                    navigateToGame = { canvas -> navController.navigate(Route.Schwimmen.Game(canvas)) },
                    navigateBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
            composable<Route.Schwimmen.Game> {
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
            composable<Route.Dart.Start> {
                val viewModel = it.sharedViewModel<DartViewModel>(navController)
                DartStartScreen(
                    viewModel = viewModel,
                    navigateToGameScreen = { navController.navigate(Route.Dart.Game) },
                    navigateToMainMenu = { navController.popToRoute(Route.Before.StartScreen) },
                )
            }
            composable<Route.Dart.Game> {
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
            composable<Route.Randomizer.Start> {
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
            composable<Route.Wizard.Start> {
                val viewModel = it.sharedViewModel<WizardViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                StartGameScreen(
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
            composable<Route.Wizard.Game> {
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
            composable<Route.Wizard.End> {
                val viewModel = it.sharedViewModel<WizardViewModel>(navController)
                val state by viewModel.state.collectAsStateWithLifecycle()
                WizardEndScreen(
                    state = state,
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